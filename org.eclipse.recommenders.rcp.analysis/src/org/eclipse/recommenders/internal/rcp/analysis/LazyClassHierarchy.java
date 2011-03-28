/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.analysis;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnsupportedOperation;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaAnalysisUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils;

import com.google.common.collect.Iterators;
import com.ibm.wala.classLoader.ArrayClassLoader;
import com.ibm.wala.classLoader.ClassFileModule;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.FakeRootClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassSyntheticClassLoader;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.shrike.ShrikeClassReaderHandle;

@SuppressWarnings("restriction")
public class LazyClassHierarchy implements IClassHierarchy, IResourceChangeListener {
    public static final ClassLoaderReference SYNTETIC = new ClassLoaderReference(AnalysisScope.SYNTHETIC,
            ClassLoaderReference.Java, ClassLoaderReference.Application);

    public static IClassHierarchy make(final IJavaProject project, final AnalysisScope scope) {
        final LazyClassHierarchy res = new LazyClassHierarchy(project, scope);
        return res;
    }

    private static Logger log = Logger.getLogger(LazyClassHierarchy.class);
    private final IClassLoader extLoader;
    private final IClassLoader primLoader;
    private final IClassLoader bypassLoader;
    private final IClassLoader appLoader;
    private final AnalysisScope scope;
    private final IJavaProject project;
    private final HashMap<TypeName, IClass> clazzes = new HashMap<TypeName, IClass>();
    private final HashMap<IResource, TypeName> watchlist = new HashMap<IResource, TypeName>();

    public LazyClassHierarchy(final IJavaProject project, final AnalysisScope scope) {
        this.project = project;
        this.scope = scope;
        this.primLoader = new LazyProjectClassLoader(this, ClassLoaderReference.Primordial);
        this.extLoader = new LazyProjectClassLoader(this, ClassLoaderReference.Extension);
        this.appLoader = new LazyProjectClassLoader(this, ClassLoaderReference.Application);
        this.bypassLoader = new BypassSyntheticClassLoader(SYNTETIC, appLoader, null, this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public boolean addClass(final IClass clazz) {
        return false;
    }

    @Override
    public IClass lookupClass(final TypeReference typeRef) {
        final TypeName name = typeRef.getName();
        if (isClassAlreadyDefinedOrCurrentlyLoaded(typeRef)) {
            return ensureIsNotNull(clazzes.get(name));
        }
        if (typeRef.isArrayType()) {
            final ArrayClassLoader arrayClassLoader = scope.getArrayClassLoader();
            final IClass lookupClass = arrayClassLoader.lookupClass(name, appLoader, this);
            if (lookupClass == null) {
                System.err.println("failed to resolve " + name);
            }
            return lookupClass;
        }
        try {
            if (typeRef.getClassLoader().equals(SYNTETIC)) {
                // if bypassed then synthetic
                return ensureIsNotNull(loadSynteticType(typeRef));
            }
            final IType type = findEclipseHandle(typeRef);
            if (type instanceof BinaryType) {
                // then it is either primordial or extension.
                return ensureIsNotNull(loadBinaryClass((BinaryType) type));
            } else if (type instanceof SourceType) {
                // it's sourceMethod, i.e., application.
                return loadFromProjectOutputLocation((SourceType) type);
                // return loadSourceType((SourceType) type);
            }
        } catch (final JavaModelException e) {
            throwUnhandledException(e);
        }
        return null;
    }

    private IType findEclipseHandle(final TypeReference typeRef) throws JavaModelException {
        checkNotNull(typeRef);
        if (typeRef.isPrimitiveType() || FakeRootClass.FAKE_ROOT_CLASS == typeRef
                || WalaAnalysisUtils.isBypassSynteticClassReference(typeRef)) {
            return null;
        }
        String name = typeRef.getName().toString();
        name = name.replace('/', '.').substring(1);
        final int firstDollarIndex = name.indexOf('$');
        if (firstDollarIndex == -1) {
            final IType type = project.findType(name);
            return type;
        } else {
            final String primaryType = name.substring(0, firstDollarIndex);
            // XXX i know... this is not sufficient in all cases; experimental
            final IType type = project.findType(primaryType);
            final String innerTypeName = name.substring(firstDollarIndex + 1);
            final IType res = type.getType(innerTypeName);
            return res;
        }
    }

    private IClass loadSynteticType(final TypeReference typeRef) {
        final IClass lookupClass = bypassLoader.lookupClass(typeRef.getName());
        clazzes.put(typeRef.getName(), lookupClass);
        return lookupClass;
    }

    /**
     * Return a cached version if available.<b>Note, since the sourceMethod code
     * loader does some strange callbacks to the cha, we must handle them by
     * returning null here.</b>
     */
    private boolean isClassAlreadyDefinedOrCurrentlyLoaded(final TypeReference A) {
        return clazzes.containsKey(A.getName());
    }

    private IClass loadFromProjectOutputLocation(final SourceType type) throws JavaModelException {
        final IFile eclipseFile = createClassFileHandleForClassName(type);
        if (eclipseFile == null) {
            final String msg = format(
                    "Failed to lookup compiled class for type '%s' in project '%s'. Did the project build successfully?",
                    type.getFullyQualifiedName(), type.getJavaProject().getElementName());
            log.warn(msg);
            return null;
        }
        final File file = eclipseFile.getLocation().toFile();
        final ShrikeClassReaderHandle handle = new ShrikeClassReaderHandle(new ClassFileModule(file));
        ShrikeClass res;
        try {
            res = new ShrikeClass(handle, appLoader, this);
        } catch (final InvalidClassFileException e) {
            throw throwUnhandledException(e);
        }
        // System.out.println("loading from output folder: " + res.getName());
        clazzes.put(res.getName(), res);
        watchlist.put(eclipseFile, res.getName());
        res.getSuperclass();
        return res;
    }

    private IFile createClassFileHandleForClassName(final SourceType type) throws JavaModelException {
        final String className = type.getFullyQualifiedName().replace('.', '/');
        final IJavaProject javaProject = type.getJavaProject();
        final IPath outputLocation = javaProject.getOutputLocation();
        final IPath path = outputLocation.append(className + ".class");
        final IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        return pathDescribesAnExistingFileHandle(member) ? (IFile) member : null;
    }

    private boolean pathDescribesAnExistingFileHandle(final IResource member) {
        return member != null && member.exists() && member.getType() == IResource.FILE
                && member.getName().endsWith(".class");
    }

    private IClass loadBinaryClass(final BinaryType type) {
        try {
            final String fullyQualifiedName = type.getFullyQualifiedName();
            final IClassLoader cl = fullyQualifiedName.startsWith("java") ? primLoader : extLoader;
            final JDTBinaryTypeEntry entry = new JDTBinaryTypeEntry(type);
            final ShrikeClassReaderHandle handle = new ShrikeClassReaderHandle(entry);
            final ShrikeClass res = new ShrikeClass(handle, cl, this);
            clazzes.put(res.getName(), res);
            res.getSuperclass();
            return res;
        } catch (final InvalidClassFileException e) {
            throw throwUnhandledException(e);
        }
    }

    @Override
    public Collection<IClass> computeSubClasses(final TypeReference type) {
        throw throwUnsupportedOperation();
    }

    @Override
    public ClassLoaderFactory getFactory() {
        return null;
    }

    @Override
    public Collection<IClass> getImmediateSubclasses(final IClass klass) {
        throw throwUnsupportedOperation();
    }

    @Override
    public Set<IClass> getImplementors(final TypeReference type) {
        throw throwUnsupportedOperation();
    }

    @Override
    public Collection<TypeReference> getJavaLangErrorTypes() {
        // XXX Need a implementation here. I think, returning an empty list does
        // no harm in our lazy
        // setup, right?
        return Collections.emptyList();
    }

    @Override
    public Collection<TypeReference> getJavaLangRuntimeExceptionTypes() {
        // XXX Need a implementation here. I think, returning an empty list does
        // no harm in our lazy
        // setup, right?
        return Collections.emptyList();
    }

    @Override
    public IClass getLeastCommonSuperclass(final IClass A, final IClass B) {
        throw throwUnsupportedOperation();
    }

    @Override
    public TypeReference getLeastCommonSuperclass(final TypeReference A, final TypeReference B) {
        throw throwUnsupportedOperation();
    }

    @Override
    public IClassLoader getLoader(final ClassLoaderReference loaderRef) {
        if (ClassLoaderReference.Application == loaderRef) {
            return appLoader;
        } else if (ClassLoaderReference.Extension == loaderRef) {
            return extLoader;
        } else if (ClassLoaderReference.Primordial == loaderRef) {
            return primLoader;
        } else if ("Synthetic".equals(loaderRef.getName().toString())) {
            return bypassLoader;
        } else {
            throw throwUnreachable();
        }
    }

    @Override
    public IClassLoader[] getLoaders() {
        throw throwUnsupportedOperation();
    }

    @Override
    public int getNumber(final IClass c) {
        throw throwUnsupportedOperation();
    }

    @Override
    public int getNumberOfClasses() {
        return 0;
    }

    @Override
    public int getNumberOfImmediateSubclasses(final IClass klass) {
        throw throwUnsupportedOperation();
    }

    @Override
    public Collection<IMethod> getPossibleTargets(final MethodReference ref) {
        throw throwUnsupportedOperation();
    }

    @Override
    public Set<IMethod> getPossibleTargets(final IClass receiverClass, final MethodReference ref) {
        throw throwUnsupportedOperation();
    }

    @Override
    public IClass getRootClass() {
        return lookupClass(TypeReference.JavaLangObject);
    }

    @Override
    public AnalysisScope getScope() {
        return scope;
    }

    @Override
    public boolean implementsInterface(final IClass c, final IClass i) {
        throw throwUnsupportedOperation();
    }

    @Override
    public boolean isAssignableFrom(final IClass c1, final IClass c2) {
        return isSubclassOf(c2, c1);
    }

    @Override
    public boolean isInterface(final TypeReference type) {
        throw throwUnsupportedOperation();
    }

    @Override
    public boolean isRootClass(final IClass c) {
        return TypeReference.JavaLangObject.equals(c.getReference());
    }

    @Override
    public boolean isSubclassOf(final IClass c, final IClass T) {
        IClass current = c;
        while (null != current && !TypeReference.JavaLangObject.equals(current.getReference())) {
            if (current == T) {
                return true;
            }
            current = current.getSuperclass();
        }
        return false;
    }

    public boolean isSyntheticClass(final IClass c) {
        throw throwUnsupportedOperation();
    }

    @Override
    public IField resolveField(final FieldReference f) {
        final IClass clazz = lookupClass(f.getDeclaringClass());
        if (null == clazz) {
            return null;
        }
        return clazz.getField(f.getName());
    }

    @Override
    public IField resolveField(final IClass klass, final FieldReference f) {
        throw throwUnsupportedOperation();
    }

    @Override
    public IMethod resolveMethod(final MethodReference m) {
        final IClass clazz = lookupClass(m.getDeclaringClass());
        if (null == clazz) {
            return null;
        }
        return clazz.getMethod(m.getSelector());
    }

    @Override
    public IMethod resolveMethod(final IClass receiverClass, final Selector selector) {
        return receiverClass.getMethod(selector);
    }

    @Override
    public Iterator<IClass> iterator() {
        return Iterators.emptyIterator();
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                @Override
                public boolean visit(final IResourceDelta delta) throws CoreException {
                    switch (delta.getKind()) {
                    case IResourceDelta.ADDED:
                        // handle added resource
                        break;
                    case IResourceDelta.REMOVED:
                        break;
                    case IResourceDelta.CHANGED:
                        final IResource resource = delta.getResource();
                        if (pathDescribesAnExistingFileHandle(resource)) {
                            final TypeName typeName = watchlist.get(resource);
                            if (null != typeName) {
                                // System.out.println("removed wala class: " +
                                // typeName);
                                invalidateObsoleteMethodsInCache(typeName);
                                clazzes.remove(typeName);
                            }
                        }
                        // handle changed resource
                        break;
                    }
                    return true;
                }

            });
        } catch (final CoreException e) {
            throwUnhandledException(e);
        }
    }

    public void remove(final ITypeName recType) {
        ensureIsNotNull(recType);
        final TypeName name = WalaNameUtils.rec2walaType(recType).getName();
        invalidateObsoleteMethodsInCache(name);
        clazzes.remove(name);
    }

    private void invalidateObsoleteMethodsInCache(final TypeName typeName) {
        final AnalysisCache cache = InjectionService.getInstance().getInjector().getInstance(AnalysisCache.class);
        final IClass clazz = clazzes.get(typeName);
        if (clazz != null) {
            for (final IMethod m : clazz.getDeclaredMethods()) {
                cache.getSSACache().invalidate(m, Everywhere.EVERYWHERE);

            }
        }
    }
}

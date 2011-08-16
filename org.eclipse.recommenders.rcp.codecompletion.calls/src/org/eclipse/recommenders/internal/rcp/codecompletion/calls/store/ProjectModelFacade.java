/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.store;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.IObjectMethodCallsNet;
import org.eclipse.recommenders.rcp.utils.JdtUtils;

import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("restriction")
public class ProjectModelFacade implements IElementChangedListener, IProjectModelFacade {

    private final IJavaProject project;
    private IPackageFragmentRoot[] packageFragmentRoots;
    private final FragmentResolver fragmentResolver;
    private final ClasspathDependencyStore dependencyStore;
    private final ModelArchiveStore archiveStore;
    private File[] dependencyLocations;

    @Inject
    public ProjectModelFacade(final ModelArchiveStore archiveStore, final FragmentResolver fragmentResolver,
            final ClasspathDependencyStore dependencyStore, @Assisted final IJavaProject project) {
        this.archiveStore = archiveStore;
        this.fragmentResolver = fragmentResolver;
        this.dependencyStore = dependencyStore;
        this.project = project;
        JavaCore.addElementChangedListener(this);
        readClasspathDependencies();
    }

    private void readClasspathDependencies() {
        try {
            packageFragmentRoots = project.getAllPackageFragmentRoots();
            dependencyLocations = getLocations(packageFragmentRoots);
            fragmentResolver.resolve(dependencyLocations);
        } catch (final JavaModelException e) {
            Throws.throwUnhandledException(e, "Unable to resolve classpath dependencies for project %s", project);
        }
    }

    @Override
    public File[] getDependencyLocations() {
        return dependencyLocations;
    }

    @Override
    public boolean hasModel(final ITypeName name) {
        if (name == null) {
            return false;
        }
        return getModelArchive(name).hasModel(name);
    }

    @Override
    public IObjectMethodCallsNet acquireModel(final ITypeName name) {
        return getModelArchive(name).acquireModel(name);
    }

    @Override
    public void releaseModel(final IObjectMethodCallsNet model) {
        getModelArchive(model.getType()).releaseModel(model);
    }

    private IModelArchive getModelArchive(final ITypeName name) {
        try {
            final IType type = findType(name);
            if (type == null) {
                return ModelArchive.NULL;
            }
            final IPackageFragmentRoot packageFragmentRoot = getPackageRoot(type);
            final File file = getLocation(packageFragmentRoot);
            if (dependencyStore.containsManifest(file)) {
                final Manifest manifest = dependencyStore.getManifest(file);
                final IModelArchive archive = archiveStore.getModelArchive(manifest);
                if (archive == IModelArchive.NULL) {
                    dependencyStore.invalidateManifest(file);
                }
                return archive;
            } else {
                return IModelArchive.NULL;
            }
        } catch (final JavaModelException e) {
            throw Throws.throwUnhandledException(e, "Unable to load model for type name: %s", name);
        }
    }

    private File[] getLocations(final IPackageFragmentRoot[] packageRoots) {
        final File[] result = new File[packageRoots.length];
        for (int i = 0; i < packageRoots.length; i++) {
            result[i] = getLocation(packageRoots[i]);
        }
        return result;
    }

    private File getLocation(final IPackageFragmentRoot packageRoot) {
        final IResource resource = packageRoot.getResource();
        if (resource != null) {
            if (resource.getLocation() == null) {
                return resource.getRawLocation().toFile().getAbsoluteFile();
            } else {
                return resource.getLocation().toFile().getAbsoluteFile();
            }
        }
        if (packageRoot.isExternal()) {
            return packageRoot.getPath().toFile().getAbsoluteFile();
        }

        throw Throws
                .throwIllegalArgumentException("Unable to resolve location of IPackageFragmentRoot: " + packageRoot);
    }

    private IPackageFragmentRoot getPackageRoot(final IType type) {
        return JdtUtils.getPackageFragmentRoot(type.getPackageFragment());
    }

    private IType findType(final ITypeName name) throws JavaModelException {
        Checks.ensureIsNotNull(name);
        final String srcTypeName = Names.vm2srcTypeName(name.getIdentifier());
        final IType type = project.findType(srcTypeName);
        return type;
    }

    public void dispose() {
        JavaCore.removeElementChangedListener(this);
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        processRelevantDeltas(event.getDelta());
    }

    private void processRelevantDeltas(final IJavaElementDelta delta) {
        if (isJavaProject(delta)) {
            final IJavaProject javaProject = (IJavaProject) delta.getElement();
            if (isSameProject(javaProject) && containsClasspathChange(delta)) {
                readClasspathDependencies();
            }
        } else {
            for (final IJavaElementDelta childDelta : delta.getAffectedChildren()) {
                processRelevantDeltas(childDelta);
            }
        }
    }

    private boolean isSameProject(final IJavaProject javaProject) {
        return javaProject.equals(project);
    }

    private boolean isJavaProject(final IJavaElementDelta delta) {
        final IJavaElement element = delta.getElement();
        return (element instanceof IJavaProject);
    }

    private boolean containsClasspathChange(final IJavaElementDelta delta) {
        if (isClasspathChange(delta.getFlags())) {
            return true;
        }

        for (final IJavaElementDelta children : delta.getAffectedChildren()) {
            final boolean containsClasspathChange = containsClasspathChange(children);
            if (containsClasspathChange) {
                return true;
            }
        }
        return false;
    }

    private boolean isClasspathChange(final int flags) {
        return (flags & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0;
    }

    @Override
    public Set<ITypeName> findTypesBySimpleName(final ITypeName receiverType) {
        final Set<ITypeName> result = Sets.newHashSet();
        try {
            final SearchableEnvironment environment = ((JavaProject) project)
                    .newSearchableNameEnvironment(DefaultWorkingCopyOwner.PRIMARY);
            environment.findTypes(receiverType.getClassName().toCharArray(), false, false, IJavaSearchConstants.TYPE,
                    new ISearchRequestor() {
                        @Override
                        public void acceptConstructor(final int modifiers, final char[] simpleTypeName,
                                final int parameterCount, final char[] signature, final char[][] parameterTypes,
                                final char[][] parameterNames, final int typeModifiers, final char[] packageName,
                                final int extraFlags, final String path, final AccessRestriction access) {
                        }

                        @Override
                        public void acceptType(final char[] packageName, final char[] typeName,
                                final char[][] enclosingTypeNames, final int modifiers,
                                final AccessRestriction accessRestriction) {
                            result.add(createTypeName(String.valueOf(packageName), String.valueOf(typeName)));
                        }

                        @Override
                        public void acceptPackage(final char[] packageName) {
                        }
                    });
        } catch (final JavaModelException e) {
            Throws.throwUnhandledException(e);
        }
        return result;
    }

    protected ITypeName createTypeName(String packageName, final String typeName) {
        packageName = packageName.replaceAll("\\.", "/");
        return VmTypeName.get("L" + packageName + "/" + typeName);
    }
}

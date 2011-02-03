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
package org.eclipse.recommenders.internal.rcp.wala;

import static org.eclipse.jdt.core.IJavaElementDelta.ADDED;
import static org.eclipse.jdt.core.IJavaElementDelta.CHANGED;
import static org.eclipse.jdt.core.IJavaElementDelta.REMOVED;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaUtils;
import org.eclipse.recommenders.internal.rcp.wala.cp.EclipseProjectPath;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.wala.IClassHierarchyService;

import com.google.inject.Inject;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.UnimplementedError;

public class WalaClassHierarchyService implements IClassHierarchyService, IElementChangedListener {
    private final HashMap<IJavaProject, IClassHierarchy> project2chaTable = new HashMap<IJavaProject, IClassHierarchy>();

    private final JavaElementResolver resolver;

    @Inject
    public WalaClassHierarchyService(final JavaElementResolver resovler) {
        this.resolver = resovler;
        registerElementChangeListener();
    }

    /**
     * Called by JavaCore whenever a java element changed.
     * 
     * @see JavaCore#addElementChangedListener(IElementChangedListener, int)
     * @see #activate();
     */
    @Override
    public synchronized void elementChanged(final ElementChangedEvent event) {
        process(event.getDelta());
    }

    private void make(final IJavaProject project, final IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        final StopWatch w = new StopWatch();
        w.start();
        try {
            final EclipseProjectPath path = EclipseProjectPath.make(project);
            final AnalysisScope scope = path.toAnalysisScope();
            final IClassHierarchy cha = LazyClassHierarchy.make(project, scope);
            project2chaTable.put(project, cha);
        } catch (final Exception e) {
            throwUnhandledException(e);
        } finally {
            w.stop();
            System.out.printf("Creating ClassHierarchy for project %s took %s.\n", project.getElementName(), w);
        }
    }

    private void process(final IJavaElementDelta delta) {
        if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
            for (final IJavaElementDelta child : delta.getAffectedChildren()) {
                process(child);
            }
            return;
        }
        switch (delta.getKind()) {
        case ADDED:
            break;
        case REMOVED:
            break;
        case CHANGED:
            processChanged(delta);
            break;
        }
    }

    private void processChanged(final IJavaElementDelta delta) {
        final IJavaElement element = delta.getElement();
        switch (element.getElementType()) {
        case IJavaElement.JAVA_PROJECT:
            if ((delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0) {
                final IJavaProject project = element.getJavaProject();
                project2chaTable.remove(project);
            }
            break;
        default:
            break;
        }
    }

    // @Override
    @Override
    public synchronized IClassHierarchy getClassHierachy(final IJavaElement jdtElement) {
        ensureIsNotNull(jdtElement);
        final IJavaProject project = jdtElement.getJavaProject();
        try {
            if (!project2chaTable.containsKey(project)) {
                make(project, new NullProgressMonitor());
                // final WorkspaceJob job = new WorkspaceJob("") {
                // @Override
                // public IStatus runInWorkspace(final IProgressMonitor monitor)
                // throws CoreException {
                // try {
                // } catch (final InvocationTargetException e) {
                // RecommendersPlugin.logError(e, "Exception during analysis.");
                // } catch (final InterruptedException e) {
                // RecommendersPlugin.logError(e, "Exception during analysis.");
                // }
                // return Status.OK_STATUS;
                // }
                // };
                // job.schedule();
                // job.join();
            }
        } catch (final Exception x) {
            final String format = "Failed to create class hierarchy for project '%s'";
            RcpAnalysisPlugin.logError(x, format, project.getElementName());
            throwUnhandledException(x);
        }
        return project2chaTable.get(project);
    }

    @Override
    public IClass getType(final IType jdtType) {
        ensureIsNotNull(jdtType);
        final IClassHierarchy cha = getClassHierachy(jdtType);
        if (cha == null) {
            return null;
        }
        final ITypeName crType = resolver.toRecType(jdtType);
        final TypeReference walaType = cr2walaTypeReference(crType);

        try {
            final IClass clazz = cha.lookupClass(walaType);
            return clazz;
        } catch (final UnimplementedError x) {
            throw WalaUtils.throwWalaFailedUnexpectedlyException(x);
        }
    }

    @Override
    public IMethod getMethod(final org.eclipse.jdt.core.IMethod jdtMethod) {
        ensureIsNotNull(jdtMethod);
        final IClassHierarchy cha = getClassHierachy(jdtMethod);
        final IMethodName crMethod = resolver.toRecMethod(jdtMethod);
        final MethodReference walaMethod = cr2walaMethodReference(crMethod);
        final com.ibm.wala.classLoader.IMethod res = cha.resolveMethod(walaMethod);
        return res;
    }

    private TypeReference cr2walaTypeReference(final ITypeName crType) {
        return TypeReference.findOrCreate(ClassLoaderReference.Application, crType.getIdentifier());
    }

    private MethodReference cr2walaMethodReference(final IMethodName crMethod) {
        final TypeReference declaringType = cr2walaTypeReference(crMethod.getDeclaringType());
        final String methodName = crMethod.getName();
        final String methodDescriptor = crMethod.getDescriptor();
        return MethodReference.findOrCreate(declaringType, methodName, methodDescriptor);
    }

    private void registerElementChangeListener() {
        JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
    }

    private void deregisterElementChangedListener() {
        JavaCore.removeElementChangedListener(this);
    }
}

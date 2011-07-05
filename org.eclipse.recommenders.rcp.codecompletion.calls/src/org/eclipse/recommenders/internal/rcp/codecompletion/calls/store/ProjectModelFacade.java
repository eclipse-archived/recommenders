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

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.rcp.utils.JdtUtils;

import com.google.common.collect.Sets;

public class ProjectModelFacade implements IElementChangedListener {

    private final CallsModelIndex index;
    private final IJavaProject project;
    private IPackageFragmentRoot[] packageFragmentRoots;

    @Inject
    public ProjectModelFacade(final CallsModelIndex index, final IJavaProject project) {
        this.index = index;
        this.project = project;
        JavaCore.addElementChangedListener(this);
        readClasspathDependencies();
    }

    private void readClasspathDependencies() {
        try {
            packageFragmentRoots = project.getAllPackageFragmentRoots();
            index.load(packageFragmentRoots);
        } catch (final JavaModelException e) {
            Throws.throwUnhandledException(e, "Unable to resolve classpath dependencies for project %s", project);
        }
    }

    public boolean hasModel(final ITypeName name) {
        return getModelArchive(name).hasModel(name);
    }

    public IObjectMethodCallsNet acquireModel(final ITypeName name) {
        return getModelArchive(name).acquireModel(name);
    }

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
            final IModelArchive archive = index.getModelArchive(packageFragmentRoot);
            return archive;
        } catch (final JavaModelException e) {
            throw Throws.throwUnhandledException(e, "Unable to load model for type name: %s", name);
        }
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

    public Set<ITypeName> findTypesBySimpleName(final ITypeName receiverType) {
        // TODO: Implement simple name lookup service.
        return Sets.newHashSet();
    }
}

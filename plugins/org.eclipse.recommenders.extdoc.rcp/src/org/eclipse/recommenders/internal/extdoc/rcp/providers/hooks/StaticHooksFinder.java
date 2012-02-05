/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers.hooks;

import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createComposite;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createLabel;
import static org.eclipse.recommenders.internal.extdoc.rcp.ui.ExtdocUtils.createMethodLink;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.EventBus;

public class StaticHooksFinder extends ExtdocProvider {

    private final class HooksRendererRunnable implements Runnable {
        private final TreeMultimap<IType, IMethod> index;
        private final Composite parent;

        private HooksRendererRunnable(final TreeMultimap<IType, IMethod> index, final Composite parent) {
            this.index = index;
            this.parent = parent;
        }

        @Override
        public void run() {
            final Composite container = createComposite(parent, 1);
            if (index.isEmpty()) {
                createLabel(container, "No public static method found in selected package (-root)", true);
            }

            final GridDataFactory linkFactory = GridDataFactory.swtDefaults().indent(15, 0);
            final GridDataFactory labelFactory = GridDataFactory.swtDefaults().indent(3, 5);
            for (final IType type : index.keySet()) {
                labelFactory.applyTo(createLabel(container, type.getFullyQualifiedName() + ":", true, false,
                        SWT.COLOR_BLACK, true));
                for (final IMethod method : index.get(type)) {
                    final Link l = createMethodLink(container, method, workspaceBus);
                    linkFactory.applyTo(l);
                }
            }
        }
    }

    private final class MethodNameComparator implements Comparator<IMethod> {
        @Override
        public int compare(final IMethod o1, final IMethod o2) {
            final String s1 = JavaElementLabels.getElementLabel(o1, JavaElementLabels.ALL_FULLY_QUALIFIED);
            final String s2 = JavaElementLabels.getElementLabel(o2, JavaElementLabels.ALL_FULLY_QUALIFIED);
            return s1.compareTo(s2);
        }
    }

    private final class TypeNameComparator implements Comparator<IType> {
        @Override
        public int compare(final IType arg0, final IType arg1) {
            final String s0 = arg0.getFullyQualifiedName();
            final String s1 = arg1.getFullyQualifiedName();
            return s0.compareTo(s1);
        }
    }

    private final JavaElementResolver resolver;
    private final EventBus workspaceBus;

    @Inject
    public StaticHooksFinder(final JavaElementResolver resolver, final EventBus workspaceBus) {
        this.resolver = resolver;
        this.workspaceBus = workspaceBus;

    }

    @JavaSelectionSubscriber
    public Status onPackageRootSelection(final IPackageFragmentRoot root, final JavaSelectionEvent event,
            final Composite parent) throws ExecutionException {

        final TreeMultimap<IType, IMethod> index = TreeMultimap.create(new TypeNameComparator(),
                new MethodNameComparator());
        try {
            for (final IJavaElement e : root.getChildren()) {
                if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                    findStaticHooks((IPackageFragment) e, index);
                }
            }
        } catch (final Exception x) {
            RecommendersPlugin.logError(x, "Failed to determine static members for %s", root.getElementName());
        }
        runSyncInUiThread(new HooksRendererRunnable(index, parent));
        return Status.OK;
    }

    @JavaSelectionSubscriber
    public Status onPackageSelection(final IPackageFragment pkg, final JavaSelectionEvent event, final Composite parent)
            throws ExecutionException {

        final TreeMultimap<IType, IMethod> index = TreeMultimap.create(new TypeNameComparator(),
                new MethodNameComparator());
        try {
            findStaticHooks(pkg, index);
        } catch (final Exception e) {
            RecommendersPlugin.logError(e, "Failed to determine static members for package %s", pkg.getElementName());
        }

        runSyncInUiThread(new HooksRendererRunnable(index, parent));
        return Status.OK;
    }

    private void findStaticHooks(final IPackageFragment pkg, final TreeMultimap<IType, IMethod> index)
            throws JavaModelException {
        for (final ITypeRoot f : pkg.getClassFiles()) {
            findStaticHooks(index, f);
        }
        for (final ITypeRoot f : pkg.getCompilationUnits()) {
            findStaticHooks(index, f);
        }
    }

    private void findStaticHooks(final TreeMultimap<IType, IMethod> index, final ITypeRoot root)
            throws JavaModelException {
        final IType type = root.findPrimaryType();
        for (final IMethod m : type.getMethods()) {
            if (JdtFlags.isStatic(m) && JdtFlags.isPublic(m) && !JdtUtils.isInitializer(m)) {
                index.put(type, m);
            }
        }
    }
}

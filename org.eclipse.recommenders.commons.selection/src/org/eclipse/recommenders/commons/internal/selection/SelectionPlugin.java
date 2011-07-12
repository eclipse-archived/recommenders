/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.commons.internal.selection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.utils.annotations.Testing;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.BundleContext;

/**
 * The selection framework plugin activator.
 */
public final class SelectionPlugin extends AbstractUIPlugin {

    private static IWorkbenchWindow workbenchWindow;
    private static InternalSelectionListener internalListener;
    private static PartListener partListener;
    private static IWorkbenchPage page;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        new WorkbenchJob("Register Selection Listeners") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                start();
                return Status.OK_STATUS;
            }
        }.schedule(1000);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        if (workbenchWindow != null && workbenchWindow.getSelectionService() != null) {
            workbenchWindow.getSelectionService().removePostSelectionListener(internalListener);
            workbenchWindow = null;
        }
        if (page != null) {
            page.removePartListener(partListener);
            page = null;
        }
        internalListener = null;
        partListener = null;
    }

    private static void start() {
        workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow == null) {
            return;
        }

        SelectionPlugin.internalListener = new InternalSelectionListener();
        SelectionPlugin.partListener = new PartListener(internalListener);
        workbenchWindow.getSelectionService().addPostSelectionListener(internalListener);

        SelectionPlugin.page = workbenchWindow.getActivePage();
        addListenersForExistentEditors(page);
        page.addPartListener(partListener);
        loadExternalListeners();

        if (page.getSelection() != null) {
            internalListener.update(page.getActivePart(), page.getSelection());
        }
    }

    /**
     * @param page
     *            The {@link IWorkbenchPage} for which all already opened
     *            editors should have the internal mouse/keyboard listeners
     *            assigned to.
     */
    private static void addListenersForExistentEditors(final IWorkbenchPage page) {
        for (final IEditorReference editor : page.getEditorReferences()) {
            final IWorkbenchPart part = editor.getPart(false);
            if (part != null) {
                partListener.addViewerListener(part);
            }
        }
    }

    /**
     * Loads external listeners which registered for the extension point.
     */
    private static void loadExternalListeners() {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        for (final IConfigurationElement element : registry
                .getConfigurationElementsFor("org.eclipse.recommenders.commons.selection.listener")) {
            try {
                final IExtendedSelectionListener listener = (IExtendedSelectionListener) element
                        .createExecutableExtension("class");
                internalListener.addExternalListener(listener);
            } catch (final CoreException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * @param selection
     *            The selection event to be "faked" for the currently active
     *            workbench part.
     */
    @Testing
    public static void triggerUpdate(final ISelection selection) {
        internalListener.update(workbenchWindow.getActivePage().getActivePart(), selection);
    }

}

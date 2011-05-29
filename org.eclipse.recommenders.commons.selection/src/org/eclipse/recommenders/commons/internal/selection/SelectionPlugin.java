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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.utils.annotations.Testing;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
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

        workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow == null) {
            // Workaround for SWTBot. Should be fixed later.
            workbenchWindow = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
        }

        SelectionPlugin.internalListener = new InternalSelectionListener();
        SelectionPlugin.partListener = new PartListener(new CursorListener(internalListener, workbenchWindow));
        workbenchWindow.getSelectionService().addSelectionListener(internalListener);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        workbenchWindow.getSelectionService().removeSelectionListener(internalListener);
        workbenchWindow = null;
        internalListener = null;

        if (page != null) {
            page.removePartListener(partListener);
            page = null;
        }

        partListener = null;
    }

    /**
     * @return True, if the listeners are installed.
     */
    protected static boolean isStarted() {
        return page != null;
    }

    /**
     * @param page
     *            The active workbench page to which the internal listeners are
     *            registered to.
     */
    protected static void loadListeners(final IWorkbenchPage page) {
        SelectionPlugin.page = page;
        page.addPartListener(partListener);
        loadExternalListeners();
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
                internalListener.addListener(listener);
            } catch (final CoreException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * @param part
     *            The workbench part which shall "fake" a selection event.
     * @param selection
     *            The selection event which shall be "faked".
     */
    public static void triggerUpdate(final IWorkbenchPart part, final ISelection selection) {
        internalListener.update(part, selection);
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

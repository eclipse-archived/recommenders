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
package org.eclipse.recommenders.commons.selection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public final class SelectionPlugin extends AbstractUIPlugin {

    private static IWorkbenchWindow workbenchWindow;
    private static SelectionListener internalListener;
    private static IWorkbenchPage page;
    private static boolean started;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

        workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow == null) {
            // Workaround for SWTBot. Should be fixed later.
            workbenchWindow = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
        }

        SelectionPlugin.internalListener = new SelectionListener(workbenchWindow, new SelectionContextResolver());
        workbenchWindow.getSelectionService().addSelectionListener(internalListener);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        workbenchWindow.getSelectionService().removeSelectionListener(internalListener);
        page.removePartListener(internalListener.getPartListener());
        internalListener = null;
    }

    protected static boolean isStarted() {
        return started;
    }

    protected static void start(final IWorkbenchPage page) {
        SelectionPlugin.page = page;
        page.addPartListener(internalListener.getPartListener());
        loadListeners();
        started = true;
    }

    private static void loadListeners() {
        final IExtensionRegistry reg = Platform.getExtensionRegistry();
        for (final IConfigurationElement element : reg
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

    public static void triggerUpdate(final IWorkbenchPart part, final ISelection selection) {
        internalListener.update(part, selection);
    }

    public static void addListener(final IExtendedSelectionListener listener) {
        internalListener.addListener(listener);
    }

    public static void triggerUpdate(final ISelection selection) {
        internalListener.update(workbenchWindow.getActivePage().getActivePart(), selection);
    }

}

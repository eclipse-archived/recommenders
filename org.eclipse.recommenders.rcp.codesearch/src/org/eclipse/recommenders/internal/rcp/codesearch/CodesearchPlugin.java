/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.recommenders.internal.rcp.codesearch.views.QueryView;
import org.eclipse.recommenders.internal.rcp.codesearch.views.ResultsView;
import org.eclipse.recommenders.rcp.utils.LoggingUtils;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CodesearchPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.recommenders.rcp.codesearch"; //$NON-NLS-1$

    // The shared instance
    private static CodesearchPlugin plugin;

    public static CodesearchPlugin getDefault() {
        return plugin;
    }

    public static void log(final CoreException e) {
        LoggingUtils.log(e, getDefault());
    }

    public static void logError(final Throwable e, final String format, final Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final Throwable e, final String format, final Object... args) {
        LoggingUtils.logError(e, getDefault(), format, args);
    }

    public static void logWarning(final String format, final Object... args) {
        LoggingUtils.logWarning(null, getDefault(), format, args);
    }

    public static QueryView showQueryView() {
        ensureIsNotNull(Display.getCurrent(), "not called from ui thread");
        final IWorkbenchPage page = RCPUtils.getActiveWorkbenchPage();
        try {
            final IViewPart showView = page.showView(QueryView.ID);
            return (QueryView) showView;
        } catch (final PartInitException e) {
            log(e);
            return null;
        }
    }

    public static ResultsView showExamplesView() {
        ensureIsNotNull(Display.getCurrent(), "not called from ui thread");
        final IWorkbenchPage page = RCPUtils.getActiveWorkbenchPage();
        try {
            final IViewPart showView = page.showView(ResultsView.ID);
            return (ResultsView) showView;
        } catch (final PartInitException e) {
            log(e);
            return null;
        }
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

}

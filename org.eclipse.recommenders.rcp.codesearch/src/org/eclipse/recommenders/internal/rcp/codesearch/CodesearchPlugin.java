/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.commons.codesearch.client.ClientConfiguration;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.codesearch.preferences.PreferenceConstants;
import org.eclipse.recommenders.internal.rcp.codesearch.views.QueryView;
import org.eclipse.recommenders.internal.rcp.codesearch.views.ResultsView;
import org.eclipse.recommenders.rcp.utils.LoggingUtils;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Inject;

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
        final IWorkbenchPage page = RCPUtils.getActiveWorkbenchPage();
        IViewPart showView;
        try {
            showView = page.showView(QueryView.ID);
            return (QueryView) showView;
        } catch (final PartInitException e) {
            log(e);
            return null;
        }
    }

    public static ResultsView showExamplesView() {
        final IWorkbenchPage page = RCPUtils.getActiveWorkbenchPage();
        IViewPart showView;
        try {
            showView = page.showView(ResultsView.ID);
            return (ResultsView) showView;
        } catch (final PartInitException e) {
            log(e);
            return null;
        }
    }

    @Inject
    private ClientConfiguration config;

    private IPropertyChangeListener propertyChangeListener;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        requestInjection();
        initializeConfiguration();
        initializePreferenceListener();
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
        super.stop(context);
    }

    private void requestInjection() {
        InjectionService.getInstance().injectMembers(this);
    }

    private void initializeConfiguration() {
        config.setBaseUrl(getPreferenceStore().getString(PreferenceConstants.WEBSERVICE_HOST));
    }

    private void initializePreferenceListener() {
        propertyChangeListener = new IPropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                if (event.getProperty().equals(PreferenceConstants.WEBSERVICE_HOST)) {
                    config.setBaseUrl(event.getNewValue().toString());
                }
            }
        };
        getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
    }
}

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
package org.eclipse.recommenders.rcp.extdoc;

import java.net.URL;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.rcp.utils.LoggingUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.BundleContext;

public final class ExtDocPlugin extends AbstractUIPlugin {

    private static final String BUNDLENAME = "org.eclipse.recommenders.rcp.extdoc";

    private static ExtDocPlugin plugin;
    private static IEclipsePreferences preferences;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        preferences = new InstanceScope().getNode(BUNDLENAME);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        preferences = null;
        plugin = null;
        super.stop(context);
    }

    public static IPreferenceStore preferenceStore() {
        return plugin.getPreferenceStore();
    }

    /**
     * @return The interface to the plug-in's preference store.
     */
    public static IEclipsePreferences getPreferences() {
        return preferences;
    }

    /**
     * @param filename
     *            The icon's path relative to ./icons/full/.
     * @return The {@link Image} object created from the icon.
     */
    public static Image getIcon(final String filename) {
        return getIconDescriptor(filename).createImage();
    }

    /**
     * @param filename
     *            The icon's path relative to ./icons/full/.
     * @return The {@link ImageDescriptor} object created from the icon.
     */
    public static ImageDescriptor getIconDescriptor(final String filename) {
        return imageDescriptorFromPlugin(BUNDLENAME, String.format("icons/full/%s", filename));
    }

    static URL getBundleEntry(final long declaringBundleId, final String entry) {
        return plugin.getBundle().getBundleContext().getBundle(declaringBundleId).getEntry(entry);
    }

    /**
     * @param exception
     *            The exception to be logged to the workspace log.
     */
    public static void logException(final Exception exception) {
        LoggingUtils.logError(exception, plugin, null);
    }

}

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
package org.eclipse.recommenders.internal.rcp.extdoc;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public final class ExtDocPlugin extends AbstractUIPlugin {

    private static ExtDocPlugin plugin;
    private static IEclipsePreferences preferences;

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        preferences = new InstanceScope().getNode(getBundle().getSymbolicName());
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        preferences = null;
        plugin = null;
        super.stop(context);
    }

    public static ExtDocPlugin getDefault() {
        return plugin;
    }

    public static Image getIcon(final String uri) {
        return imageDescriptorFromPlugin(plugin.getBundle().getSymbolicName(), "icons/full/" + uri).createImage();
    }

    public static IEclipsePreferences getPreferences() {
        return preferences;
    }

}

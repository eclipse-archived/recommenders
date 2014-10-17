/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *
 *  Based on https://github.com/awltech/eclipse-mylyn-notifications
 */
package org.eclipse.recommenders.internal.stacktraces.rcp.fadedialog;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.recommenders.stacktraces.rcp"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        plugin.createImageRegistry();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin.getImageRegistry().dispose();
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Gets image, loaded from ImageRegistry
     *
     * @param imageDescriptor
     * @return Image
     */
    public static Image getImage(ImageDescriptor imageDescriptor) {
        ImageRegistry imageRegistry = Activator.plugin.getImageRegistry();
        String imageCode = String.valueOf(imageDescriptor.hashCode());
        Image image = imageRegistry.get(imageCode);
        if (image == null) {
            image = imageDescriptor.createImage(true);
            if (image != null) {
                imageRegistry.put(imageCode, image);
            }
        }
        return image;
    }

}

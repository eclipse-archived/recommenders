/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.ui;

import static java.lang.String.format;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ExtdocIconLoader {

    public enum Icon {
        STARTED, DELAYED, FAILED, NOT_AVAILABLE,
    }

    public Image getImage(final Icon icon) {
        switch (icon) {
        case STARTED:
            return getImageFromFile("loading.png");
        case DELAYED:
            return getSharedImage(ISharedImages.IMG_OBJS_WARN_TSK);
        case FAILED:
            return getSharedImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        case NOT_AVAILABLE:
            return getImageFromFile("not_available.png");
        default:
            return null;
        }
    }

    private static Image getSharedImage(final String img) {
        return PlatformUI.getWorkbench().getSharedImages().getImage(img);
    }

    private static Image getImageFromFile(final String iconName) {
        final String path = format("icons/%s", iconName);
        final ImageDescriptor imageDescriptor = AbstractUIPlugin
                .imageDescriptorFromPlugin(ExtdocPlugin.PLUGIN_ID, path);
        if (imageDescriptor == null) {
            return getSharedImage(ISharedImages.IMG_TOOL_DELETE);
        } else {
            return imageDescriptor.createImage();
        }
    }
}
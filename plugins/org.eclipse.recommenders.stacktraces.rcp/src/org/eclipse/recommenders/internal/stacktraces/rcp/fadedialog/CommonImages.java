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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.FrameworkUtil;

class CommonImages {

    private static final URL BASE_URL = FrameworkUtil.getBundle(CommonImages.class).getEntry("/icons/"); //$NON-NLS-1$

    private static final String T_EVIEW = "eview16"; //$NON-NLS-1$s

    public static final ImageDescriptor NOTIFICATION_CLOSE = create(T_EVIEW, "notification-close.gif"); //$NON-NLS-1$

    public static final ImageDescriptor NOTIFICATION_CLOSE_HOVER = create(T_EVIEW, "notification-close-active.gif"); //$NON-NLS-1$

    private static ImageDescriptor create(String prefix, String name) {
        try {
            return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
        if (BASE_URL == null) {
            throw new MalformedURLException();
        }

        StringBuffer buffer = new StringBuffer(prefix);
        buffer.append('/');
        buffer.append(name);
        return new URL(BASE_URL, buffer.toString());
    }
}

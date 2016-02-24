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
package org.eclipse.recommenders.internal.news.rcp;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.FrameworkUtil;

public class CommonImages {

    private static final URL BASE_URL = FrameworkUtil.getBundle(CommonImages.class).getEntry("/icons/"); //$NON-NLS-1$

    private static final String T_ELCL = "elcl16"; //$NON-NLS-1$
    private static final String T_EVIEW = "eview16"; //$NON-NLS-1$
    private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$

    public static final ImageDescriptor RSS_ACTIVE = create(T_EVIEW, "rss-active.png"); //$NON-NLS-1$

    public static final ImageDescriptor RSS_INACTIVE = create(T_EVIEW, "rss-inactive.png"); //$NON-NLS-1$

    public static final ImageDescriptor REFRESH = create(T_ELCL, "refresh.png"); //$NON-NLS-1$

    public static final ImageDescriptor RSS_DIALOG_TITLE = create(T_WIZBAN, "rss-wizban.png"); //$NON-NLS-1$

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

/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp;

import static org.eclipse.jdt.core.dom.Modifier.isStatic;
import static org.eclipse.recommenders.internal.rcp.Constants.BUNDLE_NAME;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.lang.reflect.Field;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.recommenders.internal.rcp.RcpPlugin;
import org.eclipse.swt.graphics.Image;

public final class SharedImages {

    public static final String ELCL_COLLAPSE_ALL = "/icons/elcl16/collapseall.gif";
    public static final String ELCL_DELETE = "/icons/elcl16/delete.gif";
    public static final String ELCL_EXPAND_ALL = "/icons/elcl16/expandall.gif";
    public static final String ELCL_REFRESH = "/icons/elcl16/refresh_tab.gif";
    public static final String ELCL_SYNCED = "/icons/elcl16/synced.gif";
    public static final String OBJ_CHECK_GREEN = "/icons/obj16/tick_small.png";
    public static final String OBJ_CROSS_RED = "/icons/obj16/cross_small.png";
    public static final String OBJ_BULLET_BLUE = "/icons/obj16/bullet_blue.png";
    public static final String OBJ_BULLET_GREEN = "/icons/obj16/bullet_green.png";
    public static final String OBJ_BULLET_ORANGE = "/icons/obj16/bullet_orange.png";
    public static final String OBJ_BULLET_RED = "/icons/obj16/bullet_red.png";
    public static final String OBJ_BULLET_STAR = "/icons/obj16/bullet_star.png";
    public static final String OBJ_BULLET_YELLOW = "/icons/obj16/bullet_yellow.png";
    public static final String OBJ_JAR = "/icons/obj16/jar.gif";
    public static final String OBJ_JAVA_PROJECT = "/icons/obj16/project.gif";
    public static final String OBJ_JRE = "/icons/obj16/jre.gif";
    public static final String OBJ_REPOSITORY = "/icons/obj16/repository.gif";
    public static final String OVR_STAR = "/icons/ovr16/star.png";
    public static final String VIEW_SLICE = "/icons/view16/slice.gif";

    private ImageRegistry registry = new ImageRegistry();

    public SharedImages() {
        initializeImages();
    }

    public ImageDescriptor getDescriptor(String key) {
        return registry.getDescriptor(key);
    }

    public Image getImage(String key) {
        return registry.get(key);
    }

    private void initializeImages() {
        try {
            for (Field f : getClass().getDeclaredFields()) {
                if (isStatic(f.getModifiers()) && f.getType() == String.class) {
                    String path = (String) f.get(null);
                    ImageDescriptor image = imageDescriptorFromPlugin(BUNDLE_NAME, path);
                    ensureIsNotNull(image, "Could not find '%s'", path);
                    registry.put(path, image);
                }
            }
        } catch (Exception e) {
            RcpPlugin.logError(e, "Failed to load a shared image.");
        }
    }
}

/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.ui.plugin.AbstractUIPlugin.imageDescriptorFromPlugin;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Maps;

public class ImageProvider {

    public static String IMG_JRE = "icons/cview16/classpath.gif";
    public static String IMG_JAR = "icons/cview16/jar_obj.gif";
    public static String IMG_PROJECT = "icons/cview16/projects.gif";

    private HashMap<String, Image> images = Maps.newHashMap();

    public Image provideImage(String path) {
        Image image = images.get(path);
        if (image == null) {
            Image loadedImage = loadImage(path);
            images.put(path, loadedImage);
            return loadedImage;
        }
        return image;
    }

    private Image loadImage(final String pathToFile) {
        ImageDescriptor d = imageDescriptorFromPlugin(Constants.BUNDLE_ID, pathToFile);
        return d == null ? null : d.createImage();
    }

    public void dispose() {
        for (Image image : images.values()) {
            image.dispose();
        }
        images.clear();
    }
}

/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Maps;

public class OverlayImageProposalProcessor extends ProposalProcessor {

    private final Map<Image, Image> cache = Maps.newHashMap();
    private final ImageDescriptor overlay;
    private final int decorationCorner;

    public OverlayImageProposalProcessor(ImageDescriptor overlay, int decorationCorner) {
        this.overlay = overlay;
        this.decorationCorner = decorationCorner;
    }

    @Override
    public Image modifyImage(Image image) {
        Image newImage = cache.get(image);
        if (newImage == null) {
            DecorationOverlayIcon decorator = new DecorationOverlayIcon(image, overlay, decorationCorner);
            newImage = decorator.createImage();
            cache.put(image, newImage);
        }
        return newImage;
    }
}

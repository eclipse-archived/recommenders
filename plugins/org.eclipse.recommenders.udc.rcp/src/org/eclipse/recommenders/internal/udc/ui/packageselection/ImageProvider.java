/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.recommenders.internal.udc.Activator;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.swt.graphics.Image;

public class ImageProvider {
    private static ImageProvider instance;
    private Image match;
    private Image noMatch;

    protected ImageProvider() {

    }

    public Image getPackageMatchesExpressionsImage() {
        if (match == null) {
            match = decoratePackage("res/icons/current_co.gif");
        }
        return match;
    }

    public Image getPackageDoesNotMatchExpressionsImage() {
        if (noMatch == null) {
            noMatch = decoratePackage("res/icons/unconfigured_co.gif");
        }
        return noMatch;
    }

    private Image decoratePackage(final String url) {
        final Image packageImage = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
        final ImageDescriptor overlay = ImageDescriptor.createFromURL(Activator.getDefault().getBundle()
                .getResource(url));
        return decorateImage(packageImage, overlay);
    }

    private Image decorateImage(final Image image, final ImageDescriptor decoratingImage) {
        Checks.ensureIsNotNull(image);
        Checks.ensureIsNotNull(decoratingImage);
        return new DecorationOverlayIcon(image, decoratingImage, IDecoration.TOP_RIGHT).createImage();
    }

    public static ImageProvider getInstance() {
        if (instance == null) {
            instance = new ImageProvider();
        }
        return instance;
    }

}

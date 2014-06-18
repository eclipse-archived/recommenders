/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp;

import static org.apache.commons.lang3.ArrayUtils.isEquals;
import static org.eclipse.recommenders.rcp.SharedImages.Images.VIEW_SLICE;
import static org.junit.Assert.*;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.rcp.SharedImages.ImageResource;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.swt.graphics.Image;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SharedImagesTest {

    private SharedImages sut;

    @Before
    public void beforeTest() {
        sut = new SharedImages();
    }

    @After
    public void afterTest() {
        sut.dispose();
    }

    @Test
    public void testLoadImages() {
        for (Images i : Images.values()) {
            Image image = sut.getImage(i);
            assertNotNull(image);
            ImageDescriptor desc = sut.getDescriptor(i);
            assertNotNull(desc);
            // comparing image equality is a bit tricky:
            Image descImage = desc.createImage();
            assertTrue(isEquals(image.getImageData().data, descImage.getImageData().data));
        }
    }

    @Test
    public void testIsCachingImages() {
        assertEquals(sut.getImage(VIEW_SLICE), sut.getImage(Images.VIEW_SLICE));
        assertEquals(sut.getDescriptor(VIEW_SLICE), sut.getDescriptor(Images.VIEW_SLICE));
    }

    @Test
    public void testDuplicatedImagesWork() {
        Image image1 = sut.getImage(Images.VIEW_SLICE);
        Image image2 = sut.getImage(new ImageResource() {
            @Override
            public String getName() {
                return Images.VIEW_SLICE.getName();
            }
        });
        assertNotSame(image1, image2);
    }
}

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

import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_HOMEPAGE;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
            assertThat(image, is(notNullValue()));
            ImageDescriptor desc = sut.getDescriptor(i);
            assertThat(desc, is(notNullValue()));
            // comparing image equality is a bit tricky:
            Image descImage = desc.createImage();
            assertThat(image.getImageData().data, is(equalTo(descImage.getImageData().data)));
        }
    }

    @Test
    public void testIsCachingImages() {
        assertThat(sut.getImage(OBJ_HOMEPAGE), is(equalTo(sut.getImage(Images.OBJ_HOMEPAGE))));
        assertThat(sut.getDescriptor(OBJ_HOMEPAGE), is(equalTo(sut.getDescriptor(Images.OBJ_HOMEPAGE))));
    }

    @Test
    public void testDuplicatedImagesWork() {
        Image image1 = sut.getImage(Images.OBJ_HOMEPAGE);
        Image image2 = sut.getImage(new ImageResource() {
            @Override
            public String getName() {
                return Images.OBJ_HOMEPAGE.getName();
            }
        });
        assertThat(image1, is(not(equalTo(image2))));
    }
}

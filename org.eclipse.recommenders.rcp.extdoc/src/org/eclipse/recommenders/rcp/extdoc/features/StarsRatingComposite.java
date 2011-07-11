/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.recommenders.internal.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class StarsRatingComposite {

    private static final Image ICON_STAR = ExtDocPlugin.getIcon("eview16/star.png");
    private static final Image ICON_STAR_ACTIVE = ExtDocPlugin.getIcon("eview16/star_active.png");
    private static final Image ICON_STAR_EMPTY = ExtDocPlugin.getIcon("eview16/star_empty.png");

    private Object element;
    private IProvider provider;
    private IStarsRatingsServer server;

    private Composite parentComposite;
    private Composite composite;

    public static StarsRatingComposite create(final Composite parent, final Object object, final IProvider provider,
            final IStarsRatingsServer server) {
        final StarsRatingComposite composite = new StarsRatingComposite();
        composite.element = object;
        composite.provider = provider;
        composite.server = server;
        composite.parentComposite = new Composite(parent, SWT.NONE);
        composite.parentComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
        composite.printStars();
        return composite;
    }

    private void printStars() {
        final int averageRating = server.getAverageRating(element, provider);
        final IRating userRating = server.getUserRating(element, provider);

        composite = new Composite(parentComposite, SWT.NONE);
        final RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.spacing = 0;
        layout.marginBottom = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.marginTop = 0;
        composite.setLayout(layout);

        createStars(averageRating, userRating);
    }

    private void createStars(final int averageRating, final IRating userRating) {
        final int userStars = userRating == null ? -1 : userRating.getRating();
        for (int i = 1; i <= 5; ++i) {
            final Label label = new Label(composite, SWT.NONE);
            label.setImage(userStars == i ? ICON_STAR_ACTIVE : averageRating < i ? ICON_STAR_EMPTY : ICON_STAR);
            if (userStars < 1) {
                final StarListener listener = new StarListener(i);
                label.addMouseListener(listener);
                label.addKeyListener(listener);
                label.addMouseTrackListener(new HoverListener());
                label.setToolTipText("Add " + i + " Stars");
            } else {
                label.setToolTipText("Average Rating: " + averageRating + " Stars");
            }
        }
    }

    void addRating(final int stars) {
        server.addRating(element, stars, provider);
        composite.dispose();
        printStars();
        parentComposite.layout(true);
    }

    private final class StarListener implements MouseListener, KeyListener {

        private final int stars;

        private StarListener(final int stars) {
            this.stars = stars;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
        }

        @Override
        public void keyReleased(final KeyEvent e) {
            addRating(stars);
        }

        @Override
        public void mouseDoubleClick(final MouseEvent e) {
        }

        @Override
        public void mouseDown(final MouseEvent e) {
        }

        @Override
        public void mouseUp(final MouseEvent e) {
            addRating(stars);
        }

    }

    private static final class HoverListener implements MouseTrackListener {

        private Image oldImage;

        @Override
        public void mouseEnter(final MouseEvent e) {
            oldImage = ((Label) e.widget).getImage();
            ((Label) e.widget).setImage(ICON_STAR_ACTIVE);
        }

        @Override
        public void mouseExit(final MouseEvent e) {
            ((Label) e.widget).setImage(oldImage);
        }

        @Override
        public void mouseHover(final MouseEvent e) {
        }

    }

}

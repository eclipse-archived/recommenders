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

import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

final class StarsRatingComposite {

    private static final Image ICON_STAR = ExtDocPlugin.getIcon("eview16/star.png");
    private static final Image ICON_STAR_ACTIVE = ExtDocPlugin.getIcon("eview16/star_active.png");
    private static final Image ICON_STAR_EMPTY = ExtDocPlugin.getIcon("eview16/star_empty.png");

    private final IName element;
    private final String keyAppendix;
    private final IProvider provider;
    private final IUserFeedbackServer server;

    private Composite composite;

    private final IRatingSummary ratingSummary;

    StarsRatingComposite(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedback feedback, final IUserFeedbackServer server, final Composite parent) {
        this.element = element;
        this.keyAppendix = keyAppendix;
        this.provider = provider;
        this.server = server;
        ratingSummary = feedback.getRatingSummary();
        createContents(parent);
    }

    private void createContents(final Composite parent) {
        final Composite parentComposite = new Composite(parent, SWT.NONE);
        parentComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        createStarsComposite(parentComposite);
        printStars(ratingSummary);
    }

    private void createStarsComposite(final Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        final RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.spacing = 0;
        layout.marginBottom = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.marginTop = 0;
        composite.setLayout(layout);
    }

    private void printStars(final IRatingSummary ratingSummary) {
        final int userStars = ratingSummary.getUserRating() == null ? -1 : ratingSummary.getUserRating().getRating();
        for (int star = 1; star <= 5; ++star) {
            createStar(star, userStars, ratingSummary);
        }
    }

    private void createStar(final int star, final int userStars, final IRatingSummary ratingSummary) {
        final Label label = new Label(composite, SWT.NONE);
        label.setImage(userStars == star ? ICON_STAR_ACTIVE : ratingSummary.getAverage() < star ? ICON_STAR_EMPTY
                : ICON_STAR);
        if (userStars < 1) {
            label.addMouseListener(new MouseListener() {
                @Override
                public void mouseDoubleClick(final MouseEvent e) {
                }

                @Override
                public void mouseDown(final MouseEvent e) {
                }

                @Override
                public void mouseUp(final MouseEvent e) {
                    addRating(star, ratingSummary);
                }
            });
            label.addMouseTrackListener(new HoverListener());
            label.setToolTipText("Add " + star + " Stars");
        } else {
            label.setToolTipText("Average Rating: " + ratingSummary.getAverage() + " Stars");
        }
    }

    void addRating(final int stars, final IRatingSummary ratingSummary) {
        final IRating userRating = server.addRating(stars, element, keyAppendix, provider);
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
        ratingSummary.addUserRating(userRating);
        printStars(ratingSummary);
        composite.layout(true);
    }

    void dispose() {
        composite.dispose();
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

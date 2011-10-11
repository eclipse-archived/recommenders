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
package org.eclipse.recommenders.rcp.extdoc.feedback;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

final class StarsRatingComposite extends Composite {

    private static final Image ICON_STAR_ACTIVE = ExtDocPlugin.getIcon("eview16/star_active.png");
    private static final Image ICON_STAR = ExtDocPlugin.getIcon("eview16/star.png");
    private static final Image ICON_STAR_EMPTY = ExtDocPlugin.getIcon("eview16/star_empty.png");

    private final IName element;
    private final String keyAppendix;
    private final IProvider provider;
    private final IUserFeedbackServer server;

    StarsRatingComposite(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedback feedback, final IUserFeedbackServer server, final Composite parent) {
        super(parent, SWT.NONE);
        setLayout(RowLayoutFactory.swtDefaults().spacing(0).create());
        setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.TOP).create());

        this.element = element;
        this.keyAppendix = keyAppendix;
        this.provider = provider;
        this.server = server;

        printStars(feedback.getRatingSummary());
    }

    private void printStars(final IRatingSummary summary) {
        final int userStars = summary.getUserRating() == null ? -1 : summary.getUserRating().getRating();
        for (int star = 1; star <= 5; ++star) {
            createStar(star, userStars, summary);
        }
        final int amountOfRatings = summary.getAmountOfRatings();
        SwtFactory.createLabel(this, " " + amountOfRatings + "x", false, false, SWT.COLOR_DARK_GRAY, false);
    }

    private void createStar(final int star, final int userStars, final IRatingSummary summary) {
        final Label label = new Label(this, SWT.NONE);
        label.setImage(userStars == star ? ICON_STAR_ACTIVE : summary.getAverage() < star ? ICON_STAR_EMPTY : ICON_STAR);
        if (userStars < 1) {
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(final MouseEvent event) {
                    addRating(star, summary);
                }
            });
            label.addMouseTrackListener(new HoverListener());
            label.setToolTipText("Add " + star + " Stars");
        } else {
            label.setToolTipText("Average Rating: " + summary.getAverage() + " Stars");
        }
    }

    void addRating(final int stars, final IRatingSummary summary) {
        final IRating userRating = server.addRating(stars, element, keyAppendix, provider);
        for (final Control child : getChildren()) {
            child.dispose();
        }
        summary.addUserRating(userRating);
        printStars(summary);
        layout(true);
    }

    private static final class HoverListener extends MouseTrackAdapter {

        private Image oldImage;

        @Override
        public void mouseEnter(final MouseEvent event) {
            oldImage = ((Label) event.widget).getImage();
            ((Label) event.widget).setImage(ICON_STAR_ACTIVE);
        }

        @Override
        public void mouseExit(final MouseEvent event) {
            ((Label) event.widget).setImage(oldImage);
        }

    }

}

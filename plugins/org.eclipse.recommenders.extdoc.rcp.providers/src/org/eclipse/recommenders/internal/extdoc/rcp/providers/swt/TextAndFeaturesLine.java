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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.extdoc.rcp.SwtFactory;
import org.eclipse.recommenders.extdoc.rcp.feedback.CommunityFeedback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public final class TextAndFeaturesLine extends Composite {

    private final StyledText styledText;

    public static TextAndFeaturesLine create(final Composite parent, final String text, final CommunityFeedback features) {
        return new TextAndFeaturesLine(parent, text, features);
    }

    private TextAndFeaturesLine(final Composite parent, final String text, final CommunityFeedback features) {
        super(parent, SWT.NONE);
        setLayout(GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(10, 0).create());
        setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).create());

        styledText = SwtFactory.createStyledText(this, text, SWT.COLOR_BLACK, true);
        features.loadStarsRatingComposite(this);
    }

    public void createStyleRange(final int start, final int length, final int fontStyle, final boolean makeBlue,
            final boolean makeCodeFont) {
        SwtFactory.createStyleRange(styledText, start, length, fontStyle, makeBlue, makeCodeFont);
    }

}

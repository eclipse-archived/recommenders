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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.swt;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.FeaturesComposite;
import org.eclipse.recommenders.server.extdoc.IStarsRatingsServer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public final class TextAndFeaturesLine {

    private final Composite line;
    private final StyledText styledText;

    public TextAndFeaturesLine(final Composite parent, final String text, final Object element,
            final String elementName, final IProvider provider, final IStarsRatingsServer server,
            final Dialog editDialog) {
        line = SwtFactory.createGridComposite(parent, 2, 10, 0, 0, 0);
        styledText = SwtFactory.createStyledText(line, text);
        FeaturesComposite.create(line, element, elementName, provider, server, editDialog);
    }

    public void createStyleRange(final int start, final int length, final int fontStyle, final boolean makeBlue,
            final boolean makeCodeFont) {
        SwtFactory.createStyleRange(styledText, start, length, fontStyle, makeBlue, makeCodeFont);
    }

    public void dispose() {
        line.dispose();
    }

}

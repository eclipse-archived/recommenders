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
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public final class SwtFactory {

    private static final Font BOLDFONT = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
    private static final Font CODEFONT;
    private static final Color BLUECOLOR = new Color(Display.getCurrent(), 0, 0, 255);

    static {
        final FontData fontData = JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT).getFontData()[0];
        fontData.setHeight(BOLDFONT.getFontData()[0].getHeight());
        CODEFONT = new Font(Display.getCurrent(), fontData);
    }

    private SwtFactory() {
    }

    public static Composite createGridComposite(final Composite parent, final int columns, final int hSpacing,
            final int vSpacing, final int hMargin, final int vMargin) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout grid = new GridLayout(columns, false);
        grid.horizontalSpacing = hSpacing;
        grid.verticalSpacing = vSpacing;
        grid.marginHeight = vMargin;
        grid.marginWidth = hMargin;
        composite.setLayout(grid);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return composite;
    }

    public static void createSeparator(final Composite parent) {
        final Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    public static Label createLabel(final Composite parent, final String text, final boolean bold, final boolean blue,
            final boolean code) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        if (code) {
            label.setFont(CODEFONT);
        } else if (bold) {
            label.setFont(BOLDFONT);
        }
        if (blue) {
            label.setForeground(BLUECOLOR);
        }
        return label;
    }

    public static Text createText(final Composite parent, final String text, final int height, final int width) {
        final Text textComponent = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.heightHint = height;
        gridData.widthHint = width;
        textComponent.setLayoutData(gridData);
        textComponent.setText(text);
        return textComponent;
    }

    public static StyledText createStyledText(final Composite parent, final String text) {
        final StyledText styledText = new StyledText(parent, SWT.WRAP);
        styledText.setEnabled(false);
        styledText.setDoubleClickEnabled(false);
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        styledText.setEditable(false);
        styledText.setText(text);
        return styledText;
    }

    public static void createStyleRange(final StyledText styledText, final int start, final int length,
            final int fontStyle, final boolean makeBlue, final boolean makeCodeFont) {
        final StyleRange styleRange = new StyleRange();
        styleRange.start = start;
        styleRange.length = length;
        styleRange.fontStyle = fontStyle;
        if (makeBlue) {
            styleRange.foreground = BLUECOLOR;
        }
        if (makeCodeFont) {
            styleRange.font = CODEFONT;
        }
        styledText.setStyleRange(styleRange);
    }

    public static Button createCheck(final Composite area, final String text, final boolean selected) {
        final Button button = new Button(area, SWT.CHECK);
        button.setText(text);
        button.setSelection(selected);
        return button;
    }

    public static Label createSquare(final Composite parent) {
        return createLabel(parent, "▪", true, false, false);
    }

}

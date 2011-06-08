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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
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

    private static final Map<String, Font> FONTS = new HashMap<String, Font>();

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
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        return composite;
    }

    public static void createSeparator(final Composite parent) {
        final Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    public static Label createLabel(final Composite parent, final String text, final boolean bold) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        if (bold) {
            label.setFont(getFont("Segoe UI", 9, SWT.BOLD, false, false));
        }
        return label;
    }

    public static Text createText(final Composite parent, final String text, final int height, final int width) {
        final Text textComponent = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gridData.heightHint = height;
        gridData.widthHint = width;
        textComponent.setLayoutData(gridData);
        textComponent.setText(text);
        return textComponent;
    }

    public static Button createCheck(final Composite area, final String text, final boolean selected) {
        final Button button = new Button(area, SWT.CHECK);
        button.setText(text);
        button.setSelection(selected);
        return button;
    }

    /**
     * Borrowed from WindowBuilder.
     */
    private static Font getFont(final String name, final int size, final int style, final boolean strikeout,
            final boolean underline) {
        final String fontName = name + '|' + size + '|' + style + '|' + strikeout + '|' + underline;
        Font font = FONTS.get(fontName);
        if (font == null) {
            final FontData fontData = new FontData(name, size, style);
            font = new Font(Display.getCurrent(), fontData);
            FONTS.put(fontName, font);
        }
        return font;
    }

}

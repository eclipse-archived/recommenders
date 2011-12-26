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
package org.eclipse.recommenders.internal.extdoc.rcp.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Several shortcuts for creating SWT components in the Extdoc default way.
 */
// TODO: Review these methods. not sure they are still the defaults
public final class SwtUtils {

    static final Font CODEFONT = JFaceResources.getTextFont();
    private static final Font BOLDFONT = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);

    private static final Map<Integer, Color> COLORCACHE = new HashMap<Integer, Color>();

    private SwtUtils() {
    }

    public static Composite createGridComposite(final Composite parent, final int columns, final int hSpacing,
            final int vSpacing, final int hMargin, final int vMargin) {
        final Composite composite = new Composite(parent, SWT.NO_BACKGROUND);
        final GridLayout layout = GridLayoutFactory.swtDefaults().numColumns(columns).margins(hMargin, vMargin)
                .spacing(hSpacing, vSpacing).create();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return composite;
    }

    static void createSeparator(final Composite parent) {
        final Label separator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    /**
     * @param parent
     *            The composite to which the label shall be appended.
     * @param text
     *            The label's text.
     * @param wrap
     *            True, if the label should set GridData in order to be wrapped when it exceeds the parent's width.
     * @return The label created with the specified parameters.
     */
    public static Label createLabel(final Composite parent, final String text, final boolean wrap) {
        return createLabel(parent, text, false, false, SWT.COLOR_BLACK, wrap);
    }

    public static Label createLabel(final Composite parent, final String text, final boolean bold, final boolean code,
            final int color, final boolean wrap) {
        final Label label = new Label(parent, SWT.WRAP);
        label.setText(text);
        if (code) {
            label.setFont(CODEFONT);
        } else if (bold) {
            label.setFont(BOLDFONT);
        }
        label.setForeground(createColor(color));
        if (wrap) {
            label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        }
        return label;
    }

    public static CLabel createCLabel(final Composite parent, final String text, final boolean bold, final Image image) {
        final CLabel label = new CLabel(parent, SWT.NONE);
        label.setText(text);
        if (bold) {
            label.setFont(BOLDFONT);
        }
        label.setImage(image);
        return label;
    }

    /**
     * @param parent
     *            The composite to which the text shall be appended.
     * @param text
     *            The default text of the text widget.
     * @param width
     *            The width of the text widget.
     * @return The text widget created with the specified parameters.
     */
    public static Text createText(final Composite parent, final String text, final int width) {
        final Text textComponent = new Text(parent, SWT.BORDER | SWT.SINGLE);
        textComponent.setText(text);
        final GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false);
        gridData.widthHint = width;
        textComponent.setLayoutData(gridData);
        return textComponent;
    }

    public static Text createTextArea(final Composite parent, final String text, final int height, final int width) {
        final Text textComponent = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
        textComponent.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(width, height).create());
        textComponent.setText(text);
        return textComponent;
    }

    public static StyledText createStyledText(final Composite parent, final String text, final int color,
            final boolean grabExcessHorizontalSpace) {
        final StyledText styledText = new StyledText(parent, SWT.WRAP);
        styledText.setEnabled(false);
        styledText.setDoubleClickEnabled(false);
        styledText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, grabExcessHorizontalSpace, false));
        styledText.setEditable(false);
        styledText.setText(text);
        styledText.setForeground(createColor(color));
        return styledText;
    }

    public static void createStyleRange(final StyledText styledText, final int start, final int length,
            final int fontStyle, final boolean makeBlue, final boolean makeCodeFont) {
        final StyleRange styleRange = new StyleRange();
        styleRange.start = start;
        styleRange.length = length;
        styleRange.fontStyle = fontStyle;
        if (makeBlue) {
            styleRange.foreground = createColor(SWT.COLOR_BLUE);
        }
        if (makeCodeFont) {
            styleRange.font = CODEFONT;
        }
        styledText.setStyleRange(styleRange);
    }

    public static StyleRange createStyleRange(final int start, final int length, final int fontStyle,
            final boolean makeBlue, final boolean makeCodeFont) {
        final StyleRange range = new StyleRange();
        range.start = start;
        range.length = length;
        range.fontStyle = fontStyle;
        if (makeBlue) {
            range.foreground = createColor(SWT.COLOR_BLUE);
        }
        if (makeCodeFont) {
            range.font = CODEFONT;
        }
        return range;
    }

    // TODO: Use link and put together with a image into a grid.
    public static CLabel createLink(final Composite parent, final String text, final String tooltip, final Image image,
            final boolean blueColor, final MouseListener listener) {
        final CLabel link = new CLabel(parent, SWT.NONE);
        link.setText(text);
        if (tooltip != null) {
            link.setToolTipText(tooltip);
        }
        if (blueColor) {
            link.setForeground(createColor(SWT.COLOR_BLUE));
        }
        link.setImage(image);
        link.addMouseListener(listener);
        link.setCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_HAND));
        if (blueColor) {
            link.addMouseTrackListener(new MouseTrackAdapter() {

                @Override
                public void mouseExit(final MouseEvent event) {
                    link.setForeground(createColor(SWT.COLOR_BLUE));
                }

                @Override
                public void mouseEnter(final MouseEvent event) {
                    link.setForeground(createColor(SWT.COLOR_DARK_BLUE));
                }
            });
        }
        return link;
    }

    public static void createSourceCodeArea(final Composite parent, final String snippet) {
        new SourceCodeArea(parent).setCode(snippet);
    }

    public static Button createButton(final Composite parent, final String text,
            final SelectionListener selectionListener) {
        final Button button = new Button(parent, SWT.NONE);
        button.setText(text);
        button.addSelectionListener(selectionListener);
        return button;
    }

    public static Color createColor(final int swtColor) {
        final Integer color = Integer.valueOf(swtColor);
        if (!COLORCACHE.containsKey(color)) {
            COLORCACHE.put(color, Display.getCurrent().getSystemColor(swtColor));
        }
        return COLORCACHE.get(color);
    }

}

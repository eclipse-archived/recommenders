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

import static java.lang.String.format;
import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.METHOD_DECLARATION;
import static org.eclipse.swt.SWT.COLOR_INFO_BACKGROUND;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.TreeBag;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;

/**
 * Several shortcuts for creating SWT components in the Extdoc default way.
 */
// TODO: Review these methods. not sure they are still the defaults
public final class ExtdocUtils {

    static final Font CODEFONT = JFaceResources.getTextFont();
    private static final Font BOLDFONT = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);

    private static final Map<Integer, Color> COLORCACHE = new HashMap<Integer, Color>();

    private ExtdocUtils() {
    }

    public static void setInfoBackgroundColor(final Control c) {
        final Display display = c.getDisplay();
        final Color color = display.getSystemColor(COLOR_INFO_BACKGROUND);
        c.setBackground(color);
    }

    public static Composite createGridComposite(final Composite parent, final int columns, final int hSpacing,
            final int vSpacing, final int hMargin, final int vMargin) {
        final Composite composite = new Composite(parent, SWT.NONE);
        setInfoBackgroundColor(composite);
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

    public static Table renderMethodDirectivesBlock(final Composite parent, final TreeBag<IMethodName> methods,
            final int total, final EventBus bus, final JavaElementResolver resolver, final String middlePhrase) {
        final Table table = new Table(parent, SWT.NONE | SWT.HIDE_SELECTION);
        table.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
        table.setLayoutData(GridDataFactory.fillDefaults().indent(10, 0).create());
        final TableColumn column1 = new TableColumn(table, SWT.NONE);
        final TableColumn column2 = new TableColumn(table, SWT.NONE);
        final TableColumn column3 = new TableColumn(table, SWT.NONE);
        final TableColumn column4 = new TableColumn(table, SWT.NONE);

        for (final IMethodName method : methods.elementsOrderedByFrequency()) {

            final int frequency = methods.count(method);
            final int percentage = (int) Math.round(frequency * 100.0d / total);
            final String phraseText = percentageToRecommendationPhrase(percentage);
            final String stats = format(" -   (%d %% - %d times)", percentage, frequency);

            final Link bar = createMethodLink(table, method, resolver, bus);
            final TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { phraseText, middlePhrase, bar.getText(), stats });
            item.setFont(0, JFaceResources.getBannerFont());
            final TableEditor editor = new TableEditor(table);
            editor.grabHorizontal = editor.grabVertical = true;
            editor.setEditor(bar, item, 2);

        }
        column1.pack();
        column2.pack();
        column3.pack();
        column4.pack();
        return table;
    }

    public static Link createMethodLink(final Composite parent, final IMethod method, final EventBus workspaceBus) {
        final String text = "<a>" + JavaElementLabels.getElementLabel(method, JavaElementLabels.M_APP_RETURNTYPE)
                + "</a>";
        final String tooltip = JavaElementLabels.getElementLabel(method, JavaElementLabels.DEFAULT_QUALIFIED);

        final Link link = new Link(parent, SWT.NONE);
        link.setText(text);
        link.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
        link.setToolTipText(tooltip);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final JavaSelectionEvent event = new JavaSelectionEvent(method, METHOD_DECLARATION);
                workspaceBus.post(event);
            }
        });
        return link;
    }

    public static Link createMethodLink(final Composite parent, final IMethodName method,
            final JavaElementResolver resolver, final EventBus workspaceBus) {
        final String text = "<a>" + Names.vm2srcSimpleMethod(method) + "</a>";
        final String tooltip = Names.vm2srcQualifiedMethod(method);

        final Link link = new Link(parent, SWT.NONE);
        link.setText(text);
        link.setBackground(ExtdocUtils.createColor(SWT.COLOR_INFO_BACKGROUND));
        link.setToolTipText(tooltip);
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Optional<IMethod> opt = resolver.toJdtMethod(method);
                if (opt.isPresent()) {
                    final JavaSelectionEvent event = new JavaSelectionEvent(opt.get(), METHOD_DECLARATION);
                    workspaceBus.post(event);
                } else {
                    link.setEnabled(false);
                }
            }
        });
        return link;
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
        setInfoBackgroundColor(label);
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

    public static String percentageToRecommendationPhrase(final int percentage) {
        if (percentage >= 95) {
            return "always";
        } else if (percentage >= 65) {
            return "usually";
        } else if (percentage >= 25) {
            return "sometimes";
        } else if (percentage >= 10) {
            return "occasionally";
        } else {
            return "rarely";
        }
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

    public static Composite createComposite(final Composite parent, final int numColumns) {
        final Composite container = new Composite(parent, SWT.NONE);
        setInfoBackgroundColor(container);
        container.setLayout(GridLayoutFactory.fillDefaults().margins(10, 0).spacing(0, 0).numColumns(numColumns)
                .create());
        container.setLayoutData(GridDataFactory.fillDefaults().create());
        return container;
    }

}

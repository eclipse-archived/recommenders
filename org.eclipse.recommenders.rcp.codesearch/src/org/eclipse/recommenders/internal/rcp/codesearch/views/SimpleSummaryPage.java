/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codesearch.client.RCPResponse;
import org.eclipse.recommenders.internal.rcp.codesearch.client.RCPResponse.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.OpenSourceCodeInEditorJob;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendUserClickFeedbackJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class SimpleSummaryPage implements ExampleSummaryPage {
    private String title;
    private Composite control;
    public RCPResponse serverResponse;
    public RCPProposal proposal;
    public SnippetSummary snippet;
    private SourceViewer contentArea;
    private StringBuffer contentAreaBuffer;
    private List<StyleRange> contentAreaStyles;
    private SimpleSummaryViewerConfiguration configuration;
    private String searchData;
    private final CodeSearchClient searchClient;

    public SimpleSummaryPage(final CodeSearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @Override
    public void createControl(final Composite parent) {
        final GridDataFactory gd = GridDataFactory.fillDefaults();
        final GridLayoutFactory gl = GridLayoutFactory.fillDefaults().spacing(1, 1);
        control = new Composite(parent, SWT.NONE);
        control.setLayoutData(gd.grab(true, true).hint(300, 80).create());
        control.setLayout(gl.numColumns(1).create());
        createContentArea(gd, gl);
    }

    private void createContentArea(final GridDataFactory gd, final GridLayoutFactory gl) {
        contentArea = new SourceViewer(control, null, SWT.NONE);
        configuration = new SimpleSummaryViewerConfiguration();
        final Document doc = new Document();
        contentArea.setDocument(doc);
        contentArea.setEditable(false);
        final StyledText textWidget = contentArea.getTextWidget();
        textWidget.setLayout(gl.numColumns(1).create());
        textWidget.setLayoutData(gd.grab(true, true).hint(300, 80).create());
        textWidget.setMargins(10, 5, 5, 2);
        textWidget.setWordWrap(true);
        addDoubleClickListener();
        addSingleClickListener();
        addImagePaintListener();
        addMouseMoveListener();
        addMouseTrackListener();
        hookContextMenu();
    }

    private void hookContextMenu() {
        final MenuManager m = new MenuManager();
        m.add(new Action("Thumbs up!") {
            @Override
            public void run() {
                System.out
                        .println("Thumbs up... enter the code for giving feedback here - line 110 in SimpleSummaryPage.java");
            }
        });
        m.add(new Action("Thumbs down!") {
            @Override
            public void run() {
                System.out
                        .print("Thumbs down... enter the code for giving feedback here - line 118 in SimpleSummaryPage.java");
            }
        });
        final Menu menu = m.createContextMenu(control);
        contentArea.getTextWidget().setMenu(menu);
    }

    private void addSingleClickListener() {
        contentArea.getTextWidget().addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                if (isMouseAtTittleArea(event.y)) {
                    final Feedback feedback = Feedback.newFeedback(proposal.getId(), FeedbackType.EDITOR_OPENED);
                    new SendUserClickFeedbackJob(serverResponse.getRequestId(), feedback, searchClient).schedule();
                    new OpenSourceCodeInEditorJob(snippet, proposal, searchData).schedule();
                } else {
                    for (final Control child : control.getParent().getChildren()) {
                        ((GridData) child.getLayoutData()).minimumHeight = 80;
                        ((ScrolledComposite) control.getParent().getParent()).setMinSize(300, control.getParent()
                                .getChildren().length * 80);
                    }
                    final StyledText textWidget = contentArea.getTextWidget();
                    final GC gc = new GC(textWidget);
                    gc.setFont(textWidget.getFont());
                    final FontMetrics metrics = gc.getFontMetrics();
                    // int o=Dialog.convertWidthInCharsToPixels(metrics,
                    // textWidget.getText().length());
                    // int height=Dialog.
                    // gc.dispose();
                    // int op=o/textWidget.getSize().x;
                    // int opt=op*textWidget.getLineHeight(0);
                    final int i = textWidget.getSize().x / metrics.getAverageCharWidth();
                    final int j = textWidget.getText().length() / i;
                    final int h = textWidget.getLineHeight(0) * (j + 2);
                    if (h > ((GridData) control.getLayoutData()).minimumHeight) {
                        ((GridData) control.getLayoutData()).minimumHeight = h;
                        final int minHeight = ((ScrolledComposite) control.getParent().getParent()).getMinHeight();
                        ((ScrolledComposite) control.getParent().getParent()).setMinSize(300, minHeight + h);
                    }
                    gc.dispose();
                    control.getParent().getParent().layout(true, true);
                }
            }
        });
    }

    private void addMouseTrackListener() {
        contentArea.getTextWidget().addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseHover(final MouseEvent e) {
            }

            @Override
            public void mouseExit(final MouseEvent e) {
                // StyleRange range = createTitleStyleRange(0, tittle.length());
                final StyledText widget = (StyledText) e.widget;
                // widget.setStyleRange(range);
                final StyleRange[] ranges = widget.getStyleRanges(0, title.length());
                for (final StyleRange s : ranges) {
                    s.underline = false;
                    widget.setStyleRange(s);
                }
            }

            @Override
            public void mouseEnter(final MouseEvent e) {
            }
        });
    }

    private void addMouseMoveListener() {
        contentArea.getTextWidget().addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(final MouseEvent e) {
                // StyleRange range = createTitleStyleRange(0, tittle.length());
                final StyledText widget = (StyledText) e.widget;
                // widget.setStyleRange(range);
                final StyleRange[] ranges = widget.getStyleRanges(0, title.length());
                if (isMouseAtTittleArea(e.y)) {
                    for (final StyleRange s : ranges) {
                        s.underline = true;
                        widget.setStyleRange(s);
                    }
                    widget.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
                } else {
                    for (final StyleRange s : ranges) {
                        s.underline = false;
                        widget.setStyleRange(s);
                        widget.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
                    }
                }
            }
        });
    }

    private void addImagePaintListener() {
        contentArea.getTextWidget().addPaintObjectListener(new PaintObjectListener() {
            @Override
            public void paintObject(final PaintObjectEvent event) {
                final StyleRange style = event.style;
                final Image image = (Image) style.data;
                if (!image.isDisposed()) {
                    final int x = event.x;
                    final int y = event.y + event.ascent - style.metrics.ascent;
                    event.gc.drawImage(image, x, y);
                }
            }
        });
    }

    private void addDoubleClickListener() {
        contentArea.getTextWidget().addListener(SWT.MouseDoubleClick, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                if (!isMouseAtTittleArea(event.y)) {
                    new OpenSourceCodeInEditorJob(snippet, proposal, searchData).schedule();
                    final Feedback feedback = Feedback.newFeedback(proposal.getId(), FeedbackType.EDITOR_OPENED);
                    new SendUserClickFeedbackJob(snippet.id, feedback, searchClient).schedule();
                }
            }
        });
    }

    private boolean isMouseAtTittleArea(final int y) {
        final StyledText widget = contentArea.getTextWidget();
        final int line = widget.getLineIndex(y);
        final int offset = widget.getOffsetAtLine(line);
        if (offset >= 0 && offset <= title.length()) {
            return true;
        }
        return false;
    }

    @Override
    public void setInput(final Request request, final RCPResponse response, final RCPProposal result,
            final String searchData) {
        this.searchData = searchData;
        this.serverResponse = response;
        this.snippet = request.query;
        this.proposal = result;
        configuration.hit = result;
        //
        switch (proposal.getType()) {
        case METHOD:
            setContents();
            break;
        case CLASS:
        default:
            setContents();
            break;
        }
    }

    private void setContents() {
        contentAreaStyles = Lists.newLinkedList();
        contentAreaBuffer = new StringBuffer();
        addTitle();
        // first filtering out the types which are contained in the request and
        // then all the rest
        addTypesAndMethodsBlock(true);
        addTypesAndMethodsBlock(false);
        contentArea.getTextWidget().setText(contentAreaBuffer.toString());
        contentArea.getTextWidget().setStyleRanges(contentAreaStyles.toArray(new StyleRange[contentAreaStyles.size()]));
        configuration.tittle = title;
        contentArea.configure(configuration);
        final IDocumentPartitioner partitioner = new DefaultPartitioner(createScanner(),
                new String[] { SimpleSummaryPartitionScanner.TITTLE_TYPE });
        partitioner.connect(contentArea.getDocument());
        contentArea.getDocument().setDocumentPartitioner(partitioner);
        contentAreaBuffer = null;
        contentAreaStyles = null;
    }

    public IPartitionTokenScanner createScanner() {
        final IToken titleToken = new Token(SimpleSummaryPartitionScanner.TITTLE_TYPE);
        final IPredicateRule[] rules = new IPredicateRule[1];
        rules[0] = new SingleLineRule(title, "\n", titleToken);
        final RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
        scanner.setPredicateRules(rules);
        return scanner;
    }

    private boolean isTypeQualified(final ITypeName type) {
        if (searchData.isEmpty()) {
            return true;
        }
        final TypesUsesMethodCallsGrouper grouper = new TypesUsesMethodCallsGrouper(snippet, proposal);
        final Multimap<ITypeName, IMethodName> groups = grouper.getGroups();
        final Collection<IMethodName> methodGroup = groups.get(type);
        if (type.getClassName().toLowerCase().contains(searchData.toLowerCase())) {
            return true;
        }
        for (final IMethodName method : methodGroup) {
            final String IMethodName = Names.vm2srcSimpleMethod(method);
            if (IMethodName.toLowerCase().contains(searchData.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMethodQualified(final IMethodName method) {
        if (searchData.isEmpty()) {
            return true;
        }
        final String IMethodName = Names.vm2srcSimpleMethod(method);
        if (IMethodName.toLowerCase().contains(searchData.toLowerCase())) {
            return true;
        }
        return false;
    }

    private void addTypesAndMethodsBlock(final boolean foundInRequest) {
        final TypesUsesMethodCallsGrouper grouper = new TypesUsesMethodCallsGrouper(snippet, proposal);
        final List<ITypeName> typeGroup = grouper.getTypes(foundInRequest);
        final Multimap<ITypeName, IMethodName> groups = grouper.getGroups();
        for (final ITypeName type : typeGroup) {
            if (isTypeQualified(type)) {
                addTypeImage();
                addITypeName(type, grouper.inRequest(type));
                contentAreaBuffer.append(": ");
                final Collection<IMethodName> methodGroup = groups.get(type);
                for (final IMethodName method : methodGroup) {
                    addIMethodName(method, grouper.inRequest(method));
                }
                if (!methodGroup.isEmpty()) {
                    contentAreaBuffer.setLength(contentAreaBuffer.length() - 2);
                    contentAreaBuffer.append(" ");
                }
            }
        }
    }

    private void addIMethodName(final IMethodName method, final boolean foundInRequest) {
        final String IMethodName = Names.vm2srcSimpleMethod(method);
        if (isMethodQualified(method)) {
            final int start = contentAreaBuffer.length();
            final int length = IMethodName.length();
            contentAreaBuffer.append(IMethodName);
            if (foundInRequest) {
            } else {
                addNormalGreyStyleRange(start, length);
                // use default black
            }
            contentAreaBuffer.append(", ");
        }
    }

    private void addITypeName(final ITypeName type, final boolean typeFoundInRequest) {
        final String className = type.getClassName();
        final int start = contentAreaBuffer.length();
        final int length = className.length();
        contentAreaBuffer.append(className);
        if (typeFoundInRequest) {
            addBoldBlackStyleRange(start, length);
        } else {
            addBoldGreyStyleRange(start, length);
        }
    }

    private void addNormalGreyStyleRange(final int start, final int length) {
        final Color foreground = JavaUI.getColorManager().getColor(new RGB(128, 128, 128));
        final StyleRange style = new StyleRange(start, length, foreground, null);
        contentAreaStyles.add(style);
    }

    private void addBoldGreyStyleRange(final int start, final int length) {
        final Color foreground = JavaUI.getColorManager().getColor(new RGB(128, 128, 128));
        final StyleRange style = new StyleRange(start, length, foreground, null);
        style.fontStyle = SWT.BOLD;
        contentAreaStyles.add(style);
    }

    private void addBoldBlackStyleRange(final int start, final int length) {
        final StyleRange style = new StyleRange(start, length, null, null);
        style.fontStyle = SWT.BOLD;
        contentAreaStyles.add(style);
    }

    private void addTitle() {
        if (proposal.getMethodName() != null) {
            title = Names.vm2srcQualifiedMethod(proposal.getMethodName());
        } else if (proposal.getClassName() != null) {
            title = Names.vm2srcQualifiedType(proposal.getClassName());
        } else {
            title = "<error: neither class name nor method name set in hit>";
        }
        contentAreaBuffer.append(title);
        contentAreaBuffer.append(":\n");
        // contentAreaStyles.add(createTitleStyleRange(0,
        // contentAreaBuffer.length()));
    }

    private void addTypeImage() {
        final Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
        final StyleRange style = new StyleRange();
        style.start = contentAreaBuffer.length();
        contentAreaBuffer.append("\uFFFC");
        style.length = 1;
        style.data = image;
        final Rectangle rect = image.getBounds();
        style.metrics = new GlyphMetrics(rect.height - 2, 0, rect.width);
        contentAreaStyles.add(style);
    }

    // private StyleRange createTitleStyleRange(int start, int length)
    // {
    // IColorManager mgr = JavaUI.getColorManager();
    // Color foreground = mgr.getColor(IJavaColorConstants.JAVADOC_LINK);
    // StyleRange style = new StyleRange(0, length, foreground, null);
    // style.fontStyle = SWT.BOLD;
    // // style.underline = true;
    // // style.font = JFaceResources.getBannerFont();
    // Font initialFont = JFaceResources.getDefaultFont();
    // FontData[] fontData = initialFont.getFontData();
    // for (int i = 0; i < fontData.length; i++)
    // {
    // fontData[i].setHeight(12);
    // }
    // Font newFont = new Font(contentArea.getTextWidget().getDisplay(),
    // fontData);
    // style.font = newFont;
    // return style;
    // }
    // private StyleRange createKeywordStyleRange(int start, int length)
    // {
    // IColorManager mgr = JavaUI.getColorManager();
    // Color foreground = mgr.getColor(IJavaColorConstants.JAVA_DEFAULT);
    // StyleRange style = new StyleRange(start, length, foreground, null);
    // style.fontStyle = SWT.BOLD;
    // return style;
    // }
    @Override
    public Control getControl() {
        return control;
    }

}

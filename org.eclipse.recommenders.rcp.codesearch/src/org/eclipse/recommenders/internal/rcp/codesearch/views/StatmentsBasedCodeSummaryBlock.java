/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import static java.lang.String.format;

import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.recommenders.rcp.utils.SummaryCodeFormatter;
import org.eclipse.recommenders.rcp.utils.ast.ASTStringUtils;
import org.eclipse.recommenders.rcp.utils.ast.HeuristicUsedTypesAndMethodsLocationFinder;
import org.eclipse.recommenders.rcp.utils.ast.UsedTypesAndMethodsLocationFinder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class StatmentsBasedCodeSummaryBlock implements ICodeSummaryBlock {

    private static Image IMG_REMOVE = AbstractUIPlugin.imageDescriptorFromPlugin(CodesearchPlugin.PLUGIN_ID,
            "icons/obj16/remove.png").createImage();

    private final CodesearchController controller;
    private final SummaryCodeFormatter formatter;

    private RCPResponse response;
    private RCPProposal proposal;

    private Composite rootComposite;
    private Composite headerComposite;
    private Link headerTitleLink;
    private JavaSourceViewer sourceCodeViewer;

    private Set<ASTNode> summaryStatementNodes;

    public StatmentsBasedCodeSummaryBlock(final CodesearchController controller, final SummaryCodeFormatter formatter) {
        this.controller = controller;
        this.formatter = formatter;
    }

    @Override
    public Control createControl(final Composite parent) {
        createRootPane(parent);
        createHeaderArea();
        createSourceCodeArea();
        return rootComposite;

    }

    private void createRootPane(final Composite parent) {
        final Display display = parent.getDisplay();
        final Color white = display.getSystemColor(SWT.COLOR_WHITE);

        rootComposite = new Composite(parent, SWT.NONE);
        rootComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        rootComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(1).create());
        rootComposite.setBackground(white);

    }

    private void createHeaderArea() {
        createHeaderComposite();
        createTitleLink();
        createToolBar();
    }

    private void createHeaderComposite() {
        headerComposite = new Composite(rootComposite, SWT.NONE);
        headerComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        headerComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    }

    private void createTitleLink() {
        headerTitleLink = new Link(headerComposite, SWT.NONE);
        headerTitleLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.openInEditor(proposal);
                controller.sendFeedback(proposal, FeedbackType.EDITOR_OPENED);
            }
        });
        final Color color = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVADOC_LINK);
        headerTitleLink.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false)
                .indent(3, 1).create());
        headerTitleLink.setForeground(color);

        Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        final FontData[] fD = font.getFontData();
        fD[0].setHeight(fD[0].getHeight() + 1);
        fD[0].setStyle(SWT.BOLD);
        font = new Font(rootComposite.getDisplay(), fD[0]);
        headerTitleLink.setFont(font);
    }

    private void createToolBar() {
        final ToolBar toolBar = new ToolBar(headerComposite, SWT.FLAT);
        final ToolItem dislikeItem = new ToolItem(toolBar, SWT.NONE);
        dislikeItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.removeProposal(proposal);
                controller.sendFeedback(proposal, FeedbackType.CLEARED);
            }
        });

        dislikeItem.setImage(IMG_REMOVE);
        dislikeItem
                .setToolTipText("Removes this proposal from the list and sends a 'This example was not helpful all' feedback to the server to improve code search.");
        toolBar.setLayoutData(GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).create());
    }

    private void createSourceCodeArea() {
        final IPreferenceStore store = JavaPlugin.getDefault().getCombinedPreferenceStore();
        final JavaTextTools javaTextTools = JavaPlugin.getDefault().getJavaTextTools();
        final IColorManager colorManager = javaTextTools.getColorManager();

        sourceCodeViewer = new JavaSourceViewer(rootComposite, null, null, false, SWT.READ_ONLY | SWT.WRAP, store);
        final JavaSourceViewerConfiguration configuration = new JavaSourceViewerConfiguration(colorManager, store,
                null, null);

        sourceCodeViewer.configure(configuration);

        final Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        // final FontData[] fD = font.getFontData();
        // fD[0].setHeight(10);
        // font = new Font(rootControl.getDisplay(), fD[0]);

        sourceCodeViewer.getTextWidget().setFont(font);
        sourceCodeViewer.setEditable(false);
        sourceCodeViewer.getTextWidget().setLayoutData(GridDataFactory.fillDefaults().indent(20, 0).create());
    }

    @Override
    public void display(final RCPResponse response, final RCPProposal proposal) {
        this.response = response;
        this.proposal = proposal;
        findInterestingStatements();
        createDocumentsFromStatements();
    }

    private void createDocumentsFromStatements() {
        final StringBuilder sb = new StringBuilder();
        for (final ASTNode i : summaryStatementNodes) {
            sb.append(i.toString());
        }
        sb.append("\n");
        final String source = sb.toString(); // retrieve the source

        final IDocument document = new Document(source);
        formatCode(document);
        sourceCodeViewer.setInput(document);

        final String title = computeSummaryTitle();
        headerTitleLink.setText(format("<a>%s {</a>", title));
    }

    private void findInterestingStatements() {
        final ASTNode root = getAstRootNode(proposal);
        if (root == null) {
            return;
        }

        final SnippetSummary query = getQuery();
        final UsedTypesAndMethodsLocationFinder finder = UsedTypesAndMethodsLocationFinder.find(root, query.usedTypes,
                query.calledMethods);
        summaryStatementNodes = Sets.newTreeSet(new Comparator<ASTNode>() {

            @Override
            public int compare(final ASTNode o1, final ASTNode o2) {
                if (o1.equals(o2)) {
                    return 0;
                }
                return o1.getStartPosition() - o2.getStartPosition();
            }
        });

        for (final ASTNode node : finder.getTypeSimpleNames()) {
            final Statement statement = findClosestStatement(node);
            if (isRelevantStatement(statement)) {
                summaryStatementNodes.add(statement);
            }

        }
        for (final ASTNode node : finder.getMethodSimpleNames()) {
            final Statement statement = findClosestStatement(node);
            if (isRelevantStatement(statement)) {
                summaryStatementNodes.add(statement);
            }

        }

        for (final ASTNode node : HeuristicUsedTypesAndMethodsLocationFinder.find(root, query.usedTypes,
                query.calledMethods)) {
            final Statement statement = findClosestStatement(node);
            if (isRelevantStatement(statement)) {
                summaryStatementNodes.add(statement);
            }

        }

    }

    private boolean isRelevantStatement(final Statement statement) {
        if (statement == null) {
            return false;
        }
        switch (statement.getNodeType()) {
        case ASTNode.IF_STATEMENT:
        case ASTNode.FOR_STATEMENT:
        case ASTNode.ENHANCED_FOR_STATEMENT:
        case ASTNode.RETURN_STATEMENT:
        case ASTNode.EXPRESSION_STATEMENT:
        case ASTNode.CONSTRUCTOR_INVOCATION:
        case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
        case ASTNode.VARIABLE_DECLARATION_STATEMENT:
            return true;
        case ASTNode.BLOCK:
        case ASTNode.WHILE_STATEMENT:
        case ASTNode.DO_STATEMENT:
        case ASTNode.TRY_STATEMENT:
        case ASTNode.SWITCH_STATEMENT:
        case ASTNode.SYNCHRONIZED_STATEMENT:
        case ASTNode.THROW_STATEMENT:
        case ASTNode.BREAK_STATEMENT:
        case ASTNode.CONTINUE_STATEMENT:
        case ASTNode.EMPTY_STATEMENT:
        case ASTNode.LABELED_STATEMENT:
        case ASTNode.ASSERT_STATEMENT:
        case ASTNode.TYPE_DECLARATION_STATEMENT:
        default:
            return false;
        }
    }

    private void formatCode(final IDocument document) {
        formatter.format(document);
    }

    private String computeSummaryTitle() {
        final MethodDeclaration methodDeclaration = proposal.getAstMethodDeclaration(new NullProgressMonitor());
        final String title = methodDeclaration != null ? ASTStringUtils.toQualifiedString(methodDeclaration) : Names
                .vm2srcQualifiedMethod(proposal.getMethodName());
        return title;
        // final String shortenTitle = Dialog.shortenText(title,
        // headerTitleLink);
        // return shortenTitle;
    }

    private SnippetSummary getQuery() {
        return response.getRequest().query;
    }

    private ASTNode getAstRootNode(final RCPProposal result) {
        ASTNode res = null;
        switch (result.getType()) {
        case METHOD:
            res = result.getAstMethodDeclaration(new NullProgressMonitor());
            if (res != null) {
                return res;
            }
        case CLASS:
            res = result.getAstTypeDeclaration(new NullProgressMonitor());
            if (res != null) {
                return res;
            }
        case UNKNOWN:
        default:
            return result.getAst(new NullProgressMonitor());
        }
    }

    private Statement findClosestStatement(final ASTNode tmp) {
        if (tmp == null) {
            return null;
        } else if (tmp instanceof BodyDeclaration) {
            return null;
        } else if (tmp instanceof Statement) {
            return (Statement) tmp;
        } else {
            return findClosestStatement(tmp.getParent());
        }
    }

}

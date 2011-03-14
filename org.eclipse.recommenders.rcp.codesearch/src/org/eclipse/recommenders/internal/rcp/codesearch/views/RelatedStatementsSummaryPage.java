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

import static java.lang.String.format;

import java.util.Comparator;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.codesearch.client.CodeSearchClient;
import org.eclipse.recommenders.internal.rcp.codesearch.CodesearchPlugin;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.OpenSourceCodeInEditorJob;
import org.eclipse.recommenders.internal.rcp.codesearch.jobs.SendUserClickFeedbackJob;
import org.eclipse.recommenders.rcp.utils.ast.ASTStringUtils;
import org.eclipse.recommenders.rcp.utils.ast.HeuristicUsedTypesAndMethodsLocationFinder;
import org.eclipse.recommenders.rcp.utils.ast.UsedTypesAndMethodsLocationFinder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class RelatedStatementsSummaryPage implements ExampleSummaryPage {
    private static Image IMG_REMOVE = AbstractUIPlugin.imageDescriptorFromPlugin(CodesearchPlugin.PLUGIN_ID,
            "icons/obj16/remove.png").createImage();
    private static Image IMG_ACCEPT = AbstractUIPlugin.imageDescriptorFromPlugin(CodesearchPlugin.PLUGIN_ID,
            "icons/obj16/accept.png").createImage();
    private final CodeSearchClient searchClient;

    private RCPResponse response;
    private RCPProposal proposal;

    private Composite rootControl;

    private Set<ASTNode> summaryStatementNodes;
    private Link titleLink;
    private JavaSourceViewer javaSourceViewer;
    private Composite headerPane;

    public RelatedStatementsSummaryPage(final CodeSearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @Override
    public void createControl(final Composite parent) {
        createRootPane(parent);
        createHeaderArea();
        createSourceCodeArea();

    }

    private void createRootPane(final Composite parent) {
        final Display display = parent.getDisplay();
        final Color white = display.getSystemColor(SWT.COLOR_WHITE);

        rootControl = new Composite(parent, SWT.NONE);
        rootControl.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        rootControl.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(1).create());
        rootControl.setBackground(white);

    }

    private void createHeaderArea() {
        createHeaderComposite();
        createTitleLink();
        createToolBar();
    }

    private void createHeaderComposite() {
        headerPane = new Composite(rootControl, SWT.NONE);
        headerPane.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        headerPane.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    }

    private void createTitleLink() {
        titleLink = new Link(headerPane, SWT.NONE);
        titleLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                new OpenSourceCodeInEditorJob(response.getRequest().query, proposal, "").schedule();
            }
        });
        final Color color = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVADOC_LINK);
        titleLink.setLayoutData(GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false)
                .indent(3, 1).create());
        titleLink.setForeground(color);
        Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        final FontData[] fD = font.getFontData();
        fD[0].setHeight(fD[0].getHeight() + 1);
        fD[0].setStyle(SWT.BOLD);
        font = new Font(rootControl.getDisplay(), fD[0]);
        titleLink.setFont(font);
    }

    private void createToolBar() {
        final ToolBar toolBar = new ToolBar(headerPane, SWT.FLAT);
        final ToolItem dislikeItem = new ToolItem(toolBar, SWT.NONE);
        dislikeItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Feedback feedback = Feedback.newFeedback(proposal.getId(), FeedbackType.RATED_USELESS);
                new SendUserClickFeedbackJob(response.getRequestId(), feedback, searchClient).schedule();
                updateParentsLayout();
            }

            private void updateParentsLayout() {
                // TODO this doesn't feel nice: Accessing the parent w/ type
                // casts and updating the parent...
                final Composite scrollableCompositeContentPane = rootControl.getParent();
                final ScrolledComposite scrollableComposite = (ScrolledComposite) scrollableCompositeContentPane
                        .getParent();

                rootControl.dispose();
                final Point preferredSize = scrollableCompositeContentPane.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                scrollableComposite.setMinSize(preferredSize);
                scrollableComposite.update();
            }
        });

        dislikeItem.setImage(IMG_REMOVE);
        dislikeItem
                .setToolTipText("Removes this proposal from the list and sends a 'This example was not helpful all' feedback to the server to improve code search.");
    }

    private void createSourceCodeArea() {
        final IPreferenceStore store = JavaPlugin.getDefault().getCombinedPreferenceStore();
        final JavaTextTools javaTextTools = JavaPlugin.getDefault().getJavaTextTools();
        final IColorManager colorManager = javaTextTools.getColorManager();

        javaSourceViewer = new JavaSourceViewer(rootControl, null, null, false, SWT.READ_ONLY | SWT.WRAP, store);
        final JavaSourceViewerConfiguration configuration = new JavaSourceViewerConfiguration(colorManager, store,
                null, null);
        javaSourceViewer.configure(configuration);
        final Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        // final FontData[] fD = font.getFontData();
        // fD[0].setHeight(10);
        // font = new Font(rootControl.getDisplay(), fD[0]);

        javaSourceViewer.getTextWidget().setFont(font);
        javaSourceViewer.setEditable(false);
        javaSourceViewer.getTextWidget().setLayoutData(GridDataFactory.fillDefaults().indent(5, 0).create());
    }

    @Override
    public Control getControl() {
        return rootControl;
    }

    @Override
    public void setInput(final RCPResponse response, final RCPProposal proposalToDisplay) {
        this.response = response;
        this.proposal = proposalToDisplay;
        findInterestingStatements();
        createDocumentsFromStatements();
    }

    private void createDocumentsFromStatements() {
        final StringBuilder sb = new StringBuilder();
        for (final ASTNode i : summaryStatementNodes) {
            sb.append(i.toString());
        }
        final Document document = new Document(sb.toString());
        javaSourceViewer.setInput(document);
        final MethodDeclaration methodDeclaration = proposal.getAstMethodDeclaration(new NullProgressMonitor());
        titleLink.setText(format("<a>%s {</a>", ASTStringUtils.toQualifiedString(methodDeclaration)));
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
            if (statement != null) {
                summaryStatementNodes.add(statement);
            }

        }
        for (final ASTNode node : finder.getMethodSimpleNames()) {
            final Statement statement = findClosestStatement(node);
            if (statement != null) {
                summaryStatementNodes.add(statement);
            }

        }

        for (final ASTNode node : HeuristicUsedTypesAndMethodsLocationFinder.find(root, query.usedTypes,
                query.calledMethods)) {
            final Statement statement = findClosestStatement(node);
            if (statement != null) {
                summaryStatementNodes.add(statement);
            }

        }

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
        } else if (tmp instanceof Statement) {
            return (Statement) tmp;
        } else {
            return findClosestStatement(tmp.getParent());
        }
    }

    public JavaSourceViewer getJavaSourceViewer() {
        return javaSourceViewer;
    }

}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.views.recommendations;

import static org.eclipse.recommenders.utils.Checks.cast;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.INamedCodeElement;
import org.eclipse.recommenders.rcp.IRecommendation;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.annotations.Clumsy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.FrameworkUtil;

import com.google.common.collect.Multimap;

@Clumsy
public class RecommendationsView extends ViewPart {

    private final class IElementComparerImplementation implements IElementComparer {

        @Override
        public int hashCode(final Object element) {
            if (element instanceof INamedCodeElement) {
                final INamedCodeElement c = cast(element);
                return c.getName().hashCode();
            } else {
                return element.hashCode();
            }
        }

        @Override
        public boolean equals(final Object a, final Object b) {
            if (a instanceof INamedCodeElement && b instanceof INamedCodeElement) {
                final INamedCodeElement ca = cast(a);
                final INamedCodeElement cb = cast(b);
                return ca.getName().equals(cb.getName());
            } else {
                return a.equals(b);
            }
        }
    }

    public static final String ID = "org.eclipse.recommenders.internal.rcp.views.RecommendationsView"; //$NON-NLS-1$

    private Action expandAllAction;

    private Action collapseAllAction;

    private Action linkWithEditorAction;

    private TreeViewer treeViewer;

    /**
     * Create contents of the view part.
     * 
     * @param parent
     */
    @Override
    public void createPartControl(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        treeViewer = new TreeViewer(container, SWT.BORDER);
        final Tree tree = treeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        final TreeViewerColumn targetTreeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        final TreeColumn targetColumn = targetTreeViewerColumn.getColumn();
        targetColumn.setWidth(200);
        targetColumn.setText("Target/Recommendation");
        final TreeViewerColumn probabilityTreeViewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        final TreeColumn probabilityColumn = probabilityTreeViewerColumn.getColumn();
        treeViewer.setContentProvider(new CompilationUnitWithRecommendationsContentProvider());
        treeViewer.setLabelProvider(new WorkbenchLabelProvider());
        treeViewer.setComparer(new IElementComparerImplementation());
        probabilityColumn.setWidth(100);
        probabilityColumn.setText("Probability");
        probabilityTreeViewerColumn.setLabelProvider(new LabelProviderColumnTextAndBar());
        treeViewer.setSorter(new ProbabilityViewerSorter());
        createActions();
        initializeToolBar();
        initializeMenu();
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        // Create the actions
        {
            expandAllAction = new Action("Expand All") {

                @Override
                public void run() {
                    treeViewer.expandAll();
                };
            };
            expandAllAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                    FrameworkUtil.getBundle(RecommendationsView.class).getSymbolicName(),
                    "/icons/full/elcl16/expandall.gif"));
        }
        {
            collapseAllAction = new Action("Collapse All") {

                @Override
                public void run() {
                    treeViewer.collapseAll();
                };
            };
            collapseAllAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                    FrameworkUtil.getBundle(RecommendationsView.class).getSymbolicName(),
                    "/icons/full/elcl16/collapseall.gif"));
        }
        {
            linkWithEditorAction = new Action("Link with Editor") {
            };
            linkWithEditorAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
                    FrameworkUtil.getBundle(RecommendationsView.class).getSymbolicName(),
                    "/icons/full/elcl16/synced.gif"));
        }
    }

    /**
     * Initialize the toolbar.
     */
    private void initializeToolBar() {
        final IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        toolbarManager.add(expandAllAction);
        toolbarManager.add(collapseAllAction);
        toolbarManager.add(linkWithEditorAction);
    }

    /**
     * Initialize the menu.
     */
    @SuppressWarnings("unused")
    private void initializeMenu() {
        final IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
    }

    @Override
    public void setFocus() {
        // Set the focus
    }

    public void setInput(final CompilationUnit cu, final Multimap<Object, IRecommendation> recommendations) {
        final Object[] expandedElements = treeViewer.getExpandedElements();
        treeViewer.setInput(Tuple.create(cu, recommendations));
        // treeViewer.refresh(false);
        treeViewer.setExpandedElements(expandedElements);
    }
}

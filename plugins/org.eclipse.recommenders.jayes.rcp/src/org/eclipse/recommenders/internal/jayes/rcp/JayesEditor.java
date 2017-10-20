/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.jayes.rcp;

import java.io.InputStream;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.inference.jtree.JunctionTreeBuilder;
import org.eclipse.recommenders.jayes.io.jbif.JayesBifReader;
import org.eclipse.recommenders.jayes.util.triangulation.MinDegree;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Very basic <b>viewer</b> (also the extension point used is called editor) to display the contents of a Bayesian
 * network stored in .jbif file format.
 */
public class JayesEditor extends EditorPart {

    private TreeViewer viewer;
    private BayesNet net;
    private JunctionTreeAlgorithm junctionTree;

    @Override
    public void createPartControl(Composite parent) {
        createTreeViewer(parent);
        registerContextMenu();
        setViewerInput();
    }

    private void setViewerInput() {
        IEditorInput input = getEditorInput();
        if (input instanceof IURIEditorInput) {
            IURIEditorInput fei = (IURIEditorInput) input;
            try (InputStream is = fei.getURI().toURL().openStream()) {

                JayesBifReader r = new JayesBifReader(is);
                net = r.read();
                r.close();

                this.junctionTree = new JunctionTreeAlgorithm();
                junctionTree.setJunctionTreeBuilder(JunctionTreeBuilder.forHeuristic(new MinDegree()));
                junctionTree.setNetwork(net);

                viewer.setInput(net);
                setPartName(fei.getName());
            } catch (Exception e) {
                // TODO no proper handling needed ATM
                e.printStackTrace();
            }
        }
    }

    private void createTreeViewer(Composite parent) {
        viewer = new TreeViewer(parent, SWT.BORDER);
        Tree tree = viewer.getTree();
        tree.setLinesVisible(true);
        tree.setHeaderVisible(true);

        MyViewerSorter sorter = new MyViewerSorter();
        viewer.setComparator(sorter);

        TreeViewerColumn nameColumn = createNameColumn(tree, sorter);
        TreeViewerColumn valueColumn = createValueColumn(tree, sorter);
        TreeColumnLayout treeLayout = new TreeColumnLayout();
        treeLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(4, 200, true));
        treeLayout.setColumnData(valueColumn.getColumn(), new ColumnWeightData(1, 50, true));
        parent.setLayout(treeLayout);

        viewer.setContentProvider(new BayesNodesContentProvider());
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                for (Object o : selection.toList()) {
                    if (o instanceof OutcomeNode) {
                        OutcomeNode n = (OutcomeNode) o;
                        if (hasEvidence(n)) {
                            junctionTree.removeEvidence(n.node);
                        } else {
                            junctionTree.addEvidence(n.node, n.outcomeName);
                        }
                    }
                }
                viewer.refresh();
            }
        });
    }

    private TreeViewerColumn createNameColumn(final Tree tree, final MyViewerSorter sorter) {
        final TreeViewerColumn nameColumn = new TreeViewerColumn(viewer, SWT.LEFT);
        nameColumn.getColumn().setText("Nodes");
        nameColumn.getColumn().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tree.setSortDirection(SWT.DOWN);
                tree.setSortColumn(nameColumn.getColumn());
                sorter.criterion = 0;
                viewer.refresh();

            }
        });

        nameColumn.setLabelProvider(new NameColumnLabelProvider());
        return nameColumn;
    }

    private TreeViewerColumn createValueColumn(final Tree tree, final MyViewerSorter sorter) {
        final TreeViewerColumn valueColumn = new TreeViewerColumn(viewer, SWT.LEFT);
        valueColumn.getColumn().setText("Value");
        valueColumn.setLabelProvider(new ValueColumnLabelProvider());
        valueColumn.getColumn().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tree.setSortDirection(SWT.DOWN);
                tree.setSortColumn(valueColumn.getColumn());
                sorter.criterion = 1;
                viewer.refresh();
            }
        });
        return valueColumn;
    }

    private void registerContextMenu() {
        MenuManager mgr = new MenuManager();
        mgr.setRemoveAllWhenShown(true);
        mgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Action("Collapse all") {
                    @Override
                    public void run() {
                        viewer.collapseAll();
                    }
                });
                manager.add(new Action("Expand all") {
                    @Override
                    public void run() {
                        viewer.expandToLevel(2);
                    }
                });
                manager.add(new Action("Show CPT") {
                    private BayesNode node;

                    @Override
                    public void run() {
                        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                        Object e = selection.getFirstElement();
                        node = e instanceof BayesNode ? node = (BayesNode) e : ((OutcomeNode) e).node;
                        new CPTDialog(getSite().getShell(), node).open();
                    }
                });
            }
        });
        Menu menu = mgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
    }

    private boolean hasEvidence(BayesNode node) {
        return junctionTree.getEvidence().containsKey(node);
    }

    private boolean hasEvidence(OutcomeNode node) {
        String state = junctionTree.getEvidence().get(node.node);
        return node.outcomeName.equals(state);
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setInput(input);
        setSite(site);
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    @Override
    public void doSaveAs() {

    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    private final class NameColumnLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof BayesNode) {
                return ((BayesNode) element).getName();
            } else if (element instanceof OutcomeNode) {
                return ((OutcomeNode) element).outcomeName;
            }
            return super.getText(element);
        }

        @Override
        public Color getForeground(Object element) {
            return getNodeForegroundColor(element);
        }

        @Override
        public Font getFont(Object element) {
            return getNodeFont(element);
        }
    }

    private final class ValueColumnLabelProvider extends ColumnLabelProvider {
        DecimalFormat outcomeNumberFormat = new DecimalFormat("0.00#############");
        DecimalFormat nodeNumberFormat = new DecimalFormat("0.000");

        @Override
        public String getText(Object element) {
            if (element instanceof BayesNode) {
                BayesNode n = (BayesNode) element;
                double[] beliefs = junctionTree.getBeliefs(n);
                double max = 0d;
                int maxIndex = 0;
                for (int i = 0; i < beliefs.length; i++) {
                    if (beliefs[i] > max) {
                        max = beliefs[i];
                        maxIndex = i;
                    }
                }
                String outcomeName = n.getOutcomeName(maxIndex);
                return "p(" + outcomeName + ") \u2248 " + nodeNumberFormat.format(beliefs[maxIndex]);

            } else if (element instanceof OutcomeNode) {
                OutcomeNode n = (OutcomeNode) element;
                if (hasEvidence(n)) {
                    return "\u23DA";
                }
                double[] beliefs = junctionTree.getBeliefs(n.node);
                return outcomeNumberFormat.format(beliefs[n.outcomeIndex]);
            }
            return null;
        }

        @Override
        public Color getForeground(Object element) {
            return getNodeForegroundColor(element);
        }

        @Override
        public Font getFont(Object element) {
            return getNodeFont(element);
        }
    }

    private Color getNodeForegroundColor(Object element) {
        if (element instanceof BayesNode) {
            boolean observed = junctionTree.getEvidence().containsKey(element);
            if (observed) {
                return Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);
            }
        } else if (element instanceof OutcomeNode) {
            OutcomeNode node = (OutcomeNode) element;
            String state = junctionTree.getEvidence().get(node.node);
            if (node.outcomeName.equals(state)) {
                return Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA);
            }
        }
        return null;
    }

    private Font getNodeFont(Object element) {
        boolean evidence = element instanceof BayesNode ? hasEvidence((BayesNode) element)
                : hasEvidence((OutcomeNode) element);
        return evidence ? JFaceResources.getBannerFont() : null;
    }

    /**
     * A very limited sorter used to sort elements in the viewer by probability or by name.
     */
    private final class MyViewerSorter extends ViewerComparator {

        private static final int SORT_BY_NAME = 0;
        private static final int SORT_BY_VALUE = 1;
        int criterion = 0;

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            if (e1 instanceof BayesNode && e2 instanceof BayesNode) {
                return ((BayesNode) e1).getName().compareTo(((BayesNode) e2).getName());
            }
            if (e1 instanceof OutcomeNode && e2 instanceof OutcomeNode) {
                OutcomeNode l1 = (OutcomeNode) e1;
                OutcomeNode l2 = (OutcomeNode) e2;
                switch (criterion) {
                // by name
                case SORT_BY_NAME:
                    return l1.outcomeName.compareTo(l2.outcomeName);
                case SORT_BY_VALUE:
                    // by probability
                    double[] beliefs = junctionTree.getBeliefs(l1.node);
                    int compare = -1 * Double.compare(beliefs[l1.outcomeIndex], beliefs[l2.outcomeIndex]);
                    if (compare == 0) {
                        return l1.outcomeName.compareTo(l2.outcomeName);
                    }
                    return compare;
                default:
                    break;
                }
            }
            return super.compare(viewer, e1, e2);
        }
    }

    /**
     * Represents Outcome nodes (children of BayesNode nodes) in the tree viewer. Stores all necessary information to
     * compute information used in the viewer, like parent node, outcome name & index.
     */
    public static class OutcomeNode {
        public BayesNode node;
        public int outcomeIndex;
        public String outcomeName;

        public OutcomeNode(BayesNode node, int outcomeIndex) {
            this.node = node;
            this.outcomeIndex = outcomeIndex;
            outcomeName = node.getOutcomeName(outcomeIndex);
        }
    }

    public class BayesNodesContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object net) {
            return ((BayesNet) net).getNodes().toArray();
        }

        @Override
        public boolean hasChildren(Object element) {
            return element instanceof BayesNode;
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof BayesNode) {
                BayesNode node = (BayesNode) parent;

                OutcomeNode[] res = new OutcomeNode[node.getOutcomeCount()];
                for (int i = 0; i < res.length; i++) {
                    res[i] = new OutcomeNode(node, i);
                }
                return res;
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof OutcomeNode) {
                OutcomeNode outcome = (OutcomeNode) element;
                return outcome.node;
            }
            return null;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

    }
}

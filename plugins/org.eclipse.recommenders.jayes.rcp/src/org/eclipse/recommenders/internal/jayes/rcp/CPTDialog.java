/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.jayes.rcp;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 * Very basic Conditional Probability Table (CPT) viewer for BayesNodes.
 */
public class CPTDialog extends Dialog {
    class CPTRow {
        public String outcomeName;
        public double[] probabilities;

        public CPTRow(String outcome, double[] probabilities) {
            super();
            this.outcomeName = outcome;
            this.probabilities = probabilities;
        }
    }

    private NumberFormat numberFormat = new DecimalFormat("0.00###############");

    private BayesNode node;

    CPTDialog(Shell parentShell, BayesNode node) {
        super(parentShell);
        this.node = node;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);

        TableColumnLayout tableLayout = new TableColumnLayout();
        container.setLayout(tableLayout);

        final double[] p = node.getProbabilities();
        TableViewer v = new TableViewer(container);
        Table table = v.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableViewerColumn tcs = new TableViewerColumn(v, SWT.RIGHT);
        tcs.getColumn().setText("node \\ parents");
        tcs.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((CPTRow) element).outcomeName;
            }
        });
        tableLayout.setColumnData(tcs.getColumn(), new ColumnWeightData(1, 50, false));

        int index = 0;
        for (BayesNode parentNode : node.getParents()) {
            for (String parentNodeOutcome : parentNode.getOutcomes()) {
                final int indexI = index;
                TableViewerColumn tc = new TableViewerColumn(v, SWT.RIGHT);
                tc.getColumn().setText(parentNodeOutcome);
                tc.setLabelProvider(new ColumnLabelProvider() {

                    @Override
                    public String getText(Object element) {
                        CPTRow data = (CPTRow) element;
                        return numberFormat.format(data.probabilities[indexI]);
                    };
                });
                index++;
                tableLayout.setColumnData(tc.getColumn(), new ColumnWeightData(1, 50, true));
            }
        }
        if (node.getParents().isEmpty()) {
            TableViewerColumn tc = new TableViewerColumn(v, SWT.RIGHT);
            tc.getColumn().setText("prior");
            tableLayout.setColumnData(tc.getColumn(), new ColumnWeightData(1, 50, true));
            tc.setLabelProvider(new ColumnLabelProvider() {
                @Override
                public String getText(Object element) {
                    CPTRow data = (CPTRow) element;
                    return numberFormat.format(data.probabilities[0]);
                };
            });

        }

        v.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                int outcomeCount = node.getOutcomeCount();

                int lineLength = p.length / outcomeCount;
                CPTRow[] res = new CPTRow[outcomeCount];
                // for each outcome, collect their probabilities
                for (int i = 0; i < outcomeCount; i++) {
                    res[i] = new CPTRow(node.getOutcomeName(i), new double[lineLength]);
                }

                int copyIndex = 0;
                for (int i = 0; i < p.length; i += outcomeCount) {
                    for (int j = 0; j < outcomeCount; j++) {
                        res[j].probabilities[copyIndex] = p[i + j];
                    }
                    copyIndex++;
                }
                return res;
            }
        });
        v.setInput(p);
        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, true);
    }
}

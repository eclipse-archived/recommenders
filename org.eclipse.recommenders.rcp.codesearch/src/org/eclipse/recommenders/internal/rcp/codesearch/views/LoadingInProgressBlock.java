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

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class LoadingInProgressBlock implements ICodeSummaryBlock {

    public static final RCPProposal LOADING_IN_PROGRESS = new RCPProposal();

    public LoadingInProgressBlock() {
    }

    @Override
    public Control createControl(final Composite parent) {
        final Composite root = createRootComposite(parent);
        createLabel(root);
        createProgressBar(root);
        return root;
    }

    private Composite createRootComposite(final Composite parent) {
        final Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(20, 10).create());
        root.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        final Color white = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
        root.setBackground(white);
        return root;
    }

    private void createProgressBar(final Composite root) {
        final ProgressBar progressBar = new ProgressBar(root, SWT.SMOOTH | SWT.INDETERMINATE);
        progressBar.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    }

    private void createLabel(final Composite root) {
        final Label label = new Label(root, SWT.NONE);
        label.setText("Loading source code...");
        label.setLayoutData(GridDataFactory.swtDefaults().grab(false, false).create());
        final Color color = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVADOC_LINK);
        label.setForeground(color);
    }

    @Override
    public void display(final RCPResponse response, final RCPProposal proposal) {
    }

}

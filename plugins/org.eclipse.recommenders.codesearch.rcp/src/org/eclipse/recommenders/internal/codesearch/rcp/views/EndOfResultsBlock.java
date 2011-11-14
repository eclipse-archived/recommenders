/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.codesearch.rcp.views;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.codesearch.rcp.RCPProposal;
import org.eclipse.recommenders.internal.codesearch.rcp.RCPResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class EndOfResultsBlock implements ICodeSummaryBlock {

    public static final RCPProposal END_OF_RESULTS = new RCPProposal();

    public EndOfResultsBlock() {
    }

    @Override
    public Control createControl(final Composite parent) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText("No results left.");
        label.setLayoutData(GridDataFactory.swtDefaults().indent(20, 20).create());

        final Color color = JavaUI.getColorManager().getColor(IJavaColorConstants.JAVADOC_LINK);
        label.setForeground(color);
        return label;
    }

    @Override
    public void display(final RCPResponse response, final RCPProposal proposal) {
    }

}

package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
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

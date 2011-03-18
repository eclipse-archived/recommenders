package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class FindMoreBlock implements ICodeSummaryBlock {

    public static final RCPProposal MORE = new RCPProposal();
    private final CodesearchController controller;

    public FindMoreBlock(final CodesearchController controller) {
        this.controller = controller;
    }

    @Override
    public Control createControl(final Composite parent) {
        final Button link = new Button(parent, SWT.PUSH);
        link.setText("more...");
        link.setLayoutData(GridDataFactory.swtDefaults().indent(20, 20).create());
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                controller.showNextProposals(5);
            }
        });
        return link;
    }

    @Override
    public void display(final RCPResponse response, final RCPProposal proposal) {
    }

}

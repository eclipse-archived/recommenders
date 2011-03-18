package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.recommenders.internal.rcp.codesearch.RCPProposal;
import org.eclipse.recommenders.internal.rcp.codesearch.RCPResponse;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface ICodeSummaryBlock {

    public Control createControl(Composite parent);

    void display(RCPResponse response, RCPProposal proposal);
}

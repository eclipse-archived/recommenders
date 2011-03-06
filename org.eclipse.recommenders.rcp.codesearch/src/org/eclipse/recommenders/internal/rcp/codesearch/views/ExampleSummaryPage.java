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

import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.internal.rcp.codesearch.client.RCPResponse;
import org.eclipse.recommenders.internal.rcp.codesearch.client.RCPResponse.RCPProposal;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface ExampleSummaryPage {
    void createControl(Composite parent);

    Control getControl();

    void setInput(Request request, RCPResponse response, RCPProposal result, String searchData);
}

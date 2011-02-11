/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import java.util.List;

import junit.framework.Assert;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.junit.Ignore;
import org.junit.Test;

public class TemplatesCompletionEngineTest {

    @Test
    @Ignore
    public final void testTemplatesCompletionEngine() throws Exception {
        final TemplatesCompletionEngine engine = new TemplatesCompletionEngine(null,
                ExpressionPrinterTest.getExpressionPrinterMock());

        final List<IJavaCompletionProposal> proposals = engine.computeProposals(ReceiverBuilderTest
                .getMockedConstructorContext("Button bu", "bu", "Button"));

        Assert.assertEquals(1, proposals.size());
    }

}

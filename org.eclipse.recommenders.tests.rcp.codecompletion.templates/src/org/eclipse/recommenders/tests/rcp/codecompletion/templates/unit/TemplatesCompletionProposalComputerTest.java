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
package org.eclipse.recommenders.tests.rcp.codecompletion.templates.unit;

import java.util.List;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for covering the {@link TemplatesCompletionProposalComputer}
 * class.
 */
public final class TemplatesCompletionProposalComputerTest {

    @Test
    public void testTemplatesCompletionEngine() {
        final IIntelligentCompletionContext context = CompletionTargetVariableBuilderTest.getConstructorContextMock(
                "Button bu", "bu", "Lorg/eclipse/swt/widgets/Button");

        final TemplatesCompletionProposalComputer engine = new TemplatesCompletionProposalComputer(
                PatternRecommenderTest.getPatternRecommenderMock(context.getReceiverType()),
                UnitTestSuite.getCodeBuilderMock(), new IntelligentCompletionContextResolver(new JavaElementResolver()));

        final List<? extends IJavaCompletionProposal> proposals = engine.computeCompletionProposals(context);

        Assert.assertEquals(0, proposals.size());
        for (final IJavaCompletionProposal proposal : proposals) {
            Assert.assertEquals(612, proposal.getRelevance());
            Assert.assertEquals("dynamic 'Button' - Pattern 1 - 50 %", proposal.getDisplayString());
        }
    }

}

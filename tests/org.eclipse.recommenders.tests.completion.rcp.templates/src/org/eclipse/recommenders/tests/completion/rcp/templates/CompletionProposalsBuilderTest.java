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
package org.eclipse.recommenders.tests.completion.rcp.templates;

import java.util.List;

import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.recommenders.internal.completion.rcp.templates.CompletionProposalsBuilder;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.MethodCall;
import org.eclipse.recommenders.internal.completion.rcp.templates.types.PatternRecommendation;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for covering the {@link CompletionProposalsBuilder} class.
 */
@SuppressWarnings("restriction")
public final class CompletionProposalsBuilderTest {

    /**
     * Tests the {@link CompletionProposalsBuilder} using a constructor
     * scenario.
     */
    @Test
    public void testProposalsBuilder() {
        final List<PatternRecommendation> patterns = Lists.newLinkedList();

        final MethodCall methodCall = TestUtils.getDefaultConstructorCall();
        patterns.add(new PatternRecommendation("Pattern 1", methodCall.getInvokedMethod().getDeclaringType(), Lists
                .newArrayList(methodCall.getInvokedMethod()), 50));

        final CompletionProposalsBuilder builder = new CompletionProposalsBuilder(null,
                TestUtils.getCodeBuilderMock());
        final JavaContext javaContext = new JavaContext(null, new Document(), new Position(0), null);
        Assert.assertEquals(1, builder.computeProposals(patterns, javaContext, methodCall.getVariable()).size());
    }
}

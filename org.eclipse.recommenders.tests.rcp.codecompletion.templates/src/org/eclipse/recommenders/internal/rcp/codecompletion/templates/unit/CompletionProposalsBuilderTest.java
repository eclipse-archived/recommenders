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
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit;

import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.CompletionProposalsBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.PatternRecommendation;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("restriction")
public final class CompletionProposalsBuilderTest {

    @Test
    public void testProposalsBuilder() throws JavaModelException {
        final MethodCall methodCall = UnitTestSuite.getDefaultConstructorCall();

        final List<PatternRecommendation> patterns = Lists
                .newArrayList(PatternRecommendation.create("Pattern 1", methodCall.getInvokedMethod()
                        .getDeclaringType(), Lists.newArrayList(methodCall.getInvokedMethod()), 50));

        final CompletionProposalsBuilder builder = new CompletionProposalsBuilder(null,
                UnitTestSuite.getCodeBuilderMock());

        final JavaContext javaContext = new JavaContext(null, new Document(), new Position(0), null);
        Assert.assertEquals(1, builder.computeProposals(patterns, javaContext, methodCall.getVariableName()).size());
    }
}

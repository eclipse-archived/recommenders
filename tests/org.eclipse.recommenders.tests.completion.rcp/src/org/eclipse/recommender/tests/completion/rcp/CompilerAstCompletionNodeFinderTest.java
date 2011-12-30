/**
 * Copyright (c) 2010 Darmstadt University of Technology. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommender.tests.completion.rcp;

import static org.junit.Assert.assertSame;

import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.recommenders.internal.completion.rcp.CompilerAstCompletionNodeFinder;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("restriction")
public class CompilerAstCompletionNodeFinderTest {

    private static final char[] EMPTY = new char[0];
    private final CompilerAstCompletionNodeFinder sut = new CompilerAstCompletionNodeFinder();
    private BlockScope nullBlockScope;

    @Test
    public void testCompletionOnSingleNameReference() {

        // setup:
        final CompletionOnSingleNameReference mock = Mockito.mock(CompletionOnSingleNameReference.class);
        mock.resolvedType = Mockito.mock(TypeBinding.class);
        mock.token = EMPTY;

        // exercise:
        sut.visit(mock, nullBlockScope);

        // verify:
        assertSame(mock, sut.completionNode);
        assertSame(mock.resolvedType, sut.receiverType);
    }
}

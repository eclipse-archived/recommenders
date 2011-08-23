/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.unit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.ProposalMatcher;
import org.junit.Test;

public class ProposalMatcherTest {

    @Test
    public void testHappyPath() {
        final ProposalMatcher matcher = createMatcher("test", "(I)V", "LTestClass");
        final VmMethodName method = VmMethodName.get("LTestClass", "test(I)V");
        assertTrue(matcher.matches(method));
    }

    @Test
    public void testDifferentMethodName() {
        final ProposalMatcher matcher = createMatcher("test", "(I)V", "LTestClass");
        final VmMethodName method = VmMethodName.get("LTestClass", "otherName(I)V");
        assertFalse(matcher.matches(method));
    }

    @Test
    public void testGenericParameter() {
        final ProposalMatcher matcher = createMatcher("test", "(LGenericType;)V", "LTestClass<LGenericType;>;");
        final VmMethodName method = VmMethodName.get("LTestClass", "test(I)V");
        assertTrue(matcher.matches(method));
    }

    @Test
    public void testMultipleGenericParameters() {
        final ProposalMatcher matcher = createMatcher("test", "(LGenericType;LOtherGeneric;)V",
                "LTestClass<LGenericType;LOtherGeneric;>;");
        final VmMethodName method = VmMethodName.get("LTestClass", "test(I,J)V");
        assertTrue(matcher.matches(method));
    }

    @Test
    public void testMixedParameters() {
        final ProposalMatcher matcher = createMatcher("test", "(LGenericType;J)V", "LTestClass<LGenericType;>;");
        final VmMethodName match = VmMethodName.get("LTestClass", "test(I,J)V");
        assertTrue(matcher.matches(match));

        final VmMethodName mismatch = VmMethodName.get("LTestClass", "test(I,B)V");
        assertFalse(matcher.matches(mismatch));
    }

    @Test
    public void testListParameter() {
        final ProposalMatcher matcher = createMatcher("test", "(LList<LGenericType;>;)V", "LTestClass<LGenericType;>;");
        final VmMethodName match = VmMethodName.get("LTestClass", "test(LList<I>;)V");
        assertTrue(matcher.matches(match));

        // Not implemented yet. VmMethodName and VmTypeName must keep
        // informations about generics to pass this test
        // final VmMethodName mismatch = VmMethodName.get("LTestClass",
        // "test(I)V");
        // assertFalse(matcher.matches(mismatch));
    }

    @Test
    public void testGenericReturnType() {
        final ProposalMatcher matcher = createMatcher("test", "()LGenericType;", "LTestClass<LGenericType;>;");
        final VmMethodName method = VmMethodName.get("LTestClass", "test()I");
        assertTrue(matcher.matches(method));
    }

    private ProposalMatcher createMatcher(final String jdtMethodName, final String jdtMethodSignature,
            final String jdtVariableTypeDeclarationSignature) {
        final CompletionProposal proposal = mock(CompletionProposal.class);
        when(proposal.getName()).thenReturn(jdtMethodName.toCharArray());
        when(proposal.getSignature()).thenReturn(jdtMethodSignature.toCharArray());
        when(proposal.getDeclarationSignature()).thenReturn(jdtVariableTypeDeclarationSignature.toCharArray());
        return new ProposalMatcher(proposal);
    }
}

/**
 * Copyright (c) 2011 Paul-Emmanuel Faidherbe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion proposals relevance benchmark
 *    Johannes Lerch, Marcel Bruch - Added utility functions for proposal generation 
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;

public class SubwordsUtils {
    static final String JDT_ALL_CATEGORY = "org.eclipse.jdt.ui.javaAllProposalCategory";
    static final String MYLYN_ALL_CATEGORY = "org.eclipse.mylyn.java.ui.javaAllProposalCategory";

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(final CompletionProposal proposal) {
        boolean isPotentialMethodDecl = proposal.getKind() == CompletionProposal.POTENTIAL_METHOD_DECLARATION;
        char[] token = proposal.getCompletion();
        if (Arrays.equals(token, new char[] { '(', ')' })) {
            token = proposal.getName();
        } else if (isPotentialMethodDecl && proposal.getCompletion().length == 0) {
            char[] signature = proposal.getDeclarationSignature();
            char[] typeName = Signature.getSignatureSimpleName(signature);
            return String.valueOf(typeName);
        }
        return getTokensBetweenLastWhitespaceAndFirstOpeningBracket(String.valueOf(token));
    }

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(String completion) {
        if (completion.contains("(")) {
            completion = getMethodIdentifierFromProposalText(completion);
        } else {
            completion = StringUtils.substringBefore(completion, " ");
        }
        return completion;
    }

    private static String getMethodIdentifierFromProposalText(String completion) {
        completion = StringUtils.substringBefore(completion, "(");
        if (completion.contains(" ")) {
            completion = StringUtils.substringAfterLast(completion, " ");
        }
        return completion;
    }

    public static boolean isMylynInstalled() {
        CompletionProposalComputerRegistry reg = CompletionProposalComputerRegistry.getDefault();
        for (CompletionProposalCategory cat : reg.getProposalCategories()) {
            if (cat.getId().equals(MYLYN_ALL_CATEGORY)) {
                return true;
            }
        }
        return false;
    }

}

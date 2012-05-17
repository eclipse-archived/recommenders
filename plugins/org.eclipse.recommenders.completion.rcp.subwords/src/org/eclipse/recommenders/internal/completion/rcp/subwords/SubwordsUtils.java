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
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.TextStyle;

public class SubwordsUtils {
    static final String JDT_ALL_CATEGORY = "org.eclipse.jdt.ui.javaAllProposalCategory";
    static final String MYLYN_ALL_CATEGORY = "org.eclipse.mylyn.java.ui.javaAllProposalCategory";

    public static StyledString deepCopy(final StyledString displayString) {
        final StyledString copy = new StyledString(displayString.getString());
        for (final StyleRange range : displayString.getStyleRanges()) {
            copy.setStyle(range.start, range.length, new Styler() {

                @Override
                public void applyStyles(final TextStyle textStyle) {
                    textStyle.background = range.background;
                    textStyle.borderColor = range.borderColor;
                    textStyle.borderStyle = range.borderStyle;
                    textStyle.font = range.font;
                    textStyle.foreground = range.foreground;
                }
            });
        }
        return copy;
    }

    public static String getTokensBetweenLastWhitespaceAndFirstOpeningBracket(final CompletionProposal proposal) {
        char[] token = proposal.getCompletion();
        if (Arrays.equals(token, new char[] { '(', ')' })) {
            token = proposal.getName();
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

    @SuppressWarnings("restriction")
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

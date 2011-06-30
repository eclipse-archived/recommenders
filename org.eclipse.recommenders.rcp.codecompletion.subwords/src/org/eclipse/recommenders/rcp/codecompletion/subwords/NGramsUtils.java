/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Paul-Emmanuel Faidherbe - Completion proposals relevance benchmark
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.StyledString;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NGramsUtils {

    public static int calculateMatchingNGrams(final String s1, final String s2, final int n) {
        final Set<String> nGrams1 = Sets.newHashSet(createNGrams(s1, n));
        final Set<String> nGrams2 = Sets.newHashSet(createNGrams(s2, n));
        nGrams1.retainAll(nGrams2);
        return nGrams1.size();
    }

    public static List<String> createNGrams(final String candidate, final int n) {
        final List<String> nGrams = Lists.newLinkedList();
        for (int i = 0; i + n <= candidate.length(); i++) {
            final String nGram = candidate.substring(i, i + n);
            nGrams.add(nGram);
        }
        return nGrams;
    }

    public static StyledString style(final StyledString proposalLabel, final String token) {
        final StyledString res = proposalLabel.append("");
        final String string = RegexUtil.getTokensUntilFirstOpeningBracket(res.getString());
        for (final String bigram : NGramsUtils.createNGrams(token, 2)) {
            final int indexOf = string.indexOf(bigram);
            if (indexOf != -1) {
                res.setStyle(indexOf, bigram.length(), StyledString.COUNTER_STYLER);
            }
        }
        return res;
    }
}

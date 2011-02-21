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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class SimpleSummaryPartitionScanner extends RuleBasedPartitionScanner {
    // public final static String CODE_LOCATION = "__code_location";
    // public final static String CODE_USES = "__code_uses";
    // public final static String CODE_CALLS = "__code_calls";
    // public final static String CODE_SCORE = "__code_score";
    public final static String TITTLE_TYPE = "__tittle__type";
    public static String[] CONTENT_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE, };

    public SimpleSummaryPartitionScanner() {
        final IToken key = new Token(IDocument.DEFAULT_CONTENT_TYPE);
        final IPredicateRule[] rules = new IPredicateRule[] {
        // Add rule for leading white space.
        new IPredicateRule() {
            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                return key;
            }

            @Override
            public IToken getSuccessToken() {
                return key;
            }

            @Override
            public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
                return key;
            }
        } };
        setPredicateRules(rules);
    }
}

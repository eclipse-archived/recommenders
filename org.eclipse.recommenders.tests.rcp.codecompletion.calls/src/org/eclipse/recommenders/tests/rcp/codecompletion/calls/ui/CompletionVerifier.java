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
package org.eclipse.recommenders.tests.rcp.codecompletion.calls.ui;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;

public class CompletionVerifier {

    private final Iterator<String> inputIterator;
    private int currentLineNumber = -1;
    private String currentLine;
    private StringBuffer proposedLines;
    private StringBuffer verificationLines;

    public CompletionVerifier(final List<String> inputLines) {
        inputIterator = inputLines.iterator();
    }

    public void verify() {
        while (inputIterator.hasNext()) {
            nextLine();
            if (matchesStartTag()) {
                proposedLines = new StringBuffer();
                verificationLines = new StringBuffer();
                collectProposedLines();
                verifyProposedMatchesVerification();
            }
        }
    }

    private void collectProposedLines() {
        while (true) {
            nextLine();
            if (matchesEndTag()) {
                collectVerificationLines();
                return;
            } else {
                storeAsProposalLine();
            }
        }
    }

    private void collectVerificationLines() {
        while (true) {
            nextLine();
            if (isVerificationLine()) {
                storeAsVerificationLine();
            } else {
                return;
            }
        }
    }

    private void storeAsProposalLine() {
        String storeLine;
        final int startIndex = currentLine.indexOf("<^Space");
        if (startIndex >= 0) {
            storeLine = currentLine.substring(0, startIndex);
            storeLine += currentLine.substring(currentLine.indexOf(">") + 1, currentLine.length());
        } else {
            storeLine = currentLine;
        }
        proposedLines.append(storeLine.trim() + " ");
    }

    private void storeAsVerificationLine() {
        final int commentIndex = currentLine.indexOf("//");
        verificationLines.append(currentLine.substring(commentIndex + 2).trim() + " ");
    }

    private void verifyProposedMatchesVerification() {
        if (proposedLines.toString().equals(verificationLines.toString())) {
            return;
        } else {
            Assert.fail("Proposed lines do not match verification lines.\nProposed:\n" + proposedLines
                    + "\nVerification:\n" + verificationLines);
        }
    }

    private void nextLine() {
        if (inputIterator.hasNext()) {
            currentLine = inputIterator.next();
            currentLineNumber++;
        } else {
            throw new IllegalStateException("Pattern incomplete");
        }
    }

    private boolean matchesEndTag() {
        return currentLine.matches("\\s*//\\s*?@end\\s*");
    }

    private boolean matchesStartTag() {
        return currentLine.matches("\\s*//\\s*?@start\\s*");
    }

    private boolean isVerificationLine() {
        return currentLine.matches("\\s*//.+");
    }
}

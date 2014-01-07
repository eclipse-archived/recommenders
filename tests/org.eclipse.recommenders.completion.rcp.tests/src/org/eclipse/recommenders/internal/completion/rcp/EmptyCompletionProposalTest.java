package org.eclipse.recommenders.internal.completion.rcp;

import org.eclipse.jface.text.Document;
import org.junit.Test;

@SuppressWarnings("restriction")
public class EmptyCompletionProposalTest {

    /**
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=425005">Bug 425005</a>
     */
    @Test
    public void testBug425005() {
        EmptyCompletionProposal sut = new EmptyCompletionProposal(0);
        Document doc = new Document("document");
        sut.getPrefixCompletionText(doc, 0);
    }
}

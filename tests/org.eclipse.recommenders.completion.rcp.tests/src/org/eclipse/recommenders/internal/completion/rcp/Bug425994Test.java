package org.eclipse.recommenders.internal.completion.rcp;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.Document;
import org.eclipse.recommenders.rcp.SharedImages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Test that the <em>special</em> proposals like {@link EnableCompletionProposal} do not cause a NullPointerException
 * when in the following situation:
 * 
 * <pre>
 * &quot;some string&quot;.unknown<kbd>&lt;Ctrl+Space&gt;</kbd>
 * </pre>
 * 
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=425005">Bug 425005</a>
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=425994">Bug 425994</a>
 */
@RunWith(Parameterized.class)
@SuppressWarnings("restriction")
public class Bug425994Test {

    private final AbstractJavaCompletionProposal sut;

    public Bug425994Test(AbstractJavaCompletionProposal proposal) {
        this.sut = proposal;
    }

    @Parameters
    public static Collection<Object[]> proposals() {
        SharedImages images = new SharedImages();

        LinkedList<Object[]> proposals = Lists.newLinkedList();

        proposals.add(new Object[] { new DiscoveryCompletionProposal(images) });
        proposals.add(new Object[] { new EmptyCompletionProposal(0) });
        proposals.add(new Object[] { new EnableCompletionProposal(images, 0) });

        return proposals;
    }

    @Test
    public void testNoNullPointerException() {
        Document doc = new Document("document");
        sut.getPrefixCompletionText(doc, 0);
    }
}

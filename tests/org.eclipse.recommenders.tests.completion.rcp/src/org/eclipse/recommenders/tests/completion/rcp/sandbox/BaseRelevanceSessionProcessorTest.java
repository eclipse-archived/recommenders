package org.eclipse.recommenders.tests.completion.rcp.sandbox;

import static org.junit.Assert.assertEquals;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.ProposalProcessorManager;
import org.eclipse.recommenders.internal.completion.rcp.sandbox.BaseRelevanceSessionProcessor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.Test;

import com.google.common.base.Optional;

public class BaseRelevanceSessionProcessorTest {

    @Test
    public final void test() throws Exception {

        // setup
        IProcessableProposal p = new FakeProcessableProposal();
        p.setRelevance(200);
        p.setStyledDisplayString(new StyledString("some"));
        p.setProposalProcessorManager(new ProposalProcessorManager(p));
        BaseRelevanceSessionProcessor sut = new BaseRelevanceSessionProcessor();

        // exercise
        sut.process(p);
        p.getProposalProcessorManager().prefixChanged("");

        // verify
        assertEquals(200, p.getRelevance());
    }

    private final class FakeProcessableProposal implements IProcessableProposal {
        private int relevance;
        private StyledString styledDisplayString;
        private ProposalProcessorManager mgr;
        private String prefix;

        @Override
        public Point getSelection(IDocument document) {
            return null;
        }

        @Override
        public Image getImage() {
            return null;
        }

        @Override
        public String getDisplayString() {
            return styledDisplayString.toString();
        }

        @Override
        public IContextInformation getContextInformation() {
            return null;
        }

        @Override
        public String getAdditionalProposalInfo() {
            return null;
        }

        @Override
        public void apply(IDocument document) {
        }

        @Override
        public int getRelevance() {
            return relevance;
        }

        @Override
        public void setStyledDisplayString(StyledString styledDisplayString) {
            this.styledDisplayString = styledDisplayString;

        }

        @Override
        public void setRelevance(int newRelevance) {
            this.relevance = newRelevance;
        }

        @Override
        public void setProposalProcessorManager(ProposalProcessorManager mgr) {
            this.mgr = mgr;

        }

        @Override
        public StyledString getStyledDisplayString() {
            return styledDisplayString;
        }

        @Override
        public ProposalProcessorManager getProposalProcessorManager() {
            return mgr;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public Optional<CompletionProposal> getCoreProposal() {
            return null;
        }
    }
}

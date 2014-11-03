package org.eclipse.recommenders.snipmatch.rcp.util;

import static java.lang.String.format;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.internal.snipmatch.rcp.SnippetProposal;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class SnippetProposalMatcher extends TypeSafeMatcher<SnippetProposal> {

    private final String displayString;
    private final int repoPriority;
    private final String selection;

    private SnippetProposalMatcher(String displayString, int repoPriority, String selection) {
        this.displayString = displayString;
        this.repoPriority = repoPriority;
        this.selection = selection;
    }

    public static SnippetProposalMatcher snippet(String displayString, int repoPriority, String selection) {
        return new SnippetProposalMatcher(displayString, repoPriority, selection);
    }

    @Override
    public boolean matchesSafely(SnippetProposal proposal) {
        if (!displayString.equals(proposal.getDisplayString())) {
            return false;
        }

        if (repoPriority != proposal.getRepositoryRelevance()) {
            return false;
        }

        if (!StringUtils.equals(selection, proposal.getTemplateContext().getVariable("selection"))) {
            return false;
        }

        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(getDescription(displayString, repoPriority, selection));
    }

    @Override
    public void describeMismatchSafely(SnippetProposal proposal, Description description) {
        description.appendText("was ");
        description.appendText(getDescription(proposal.getDisplayString(), proposal.getRepositoryRelevance(), proposal
                .getTemplateContext().getVariable("selection")));
    }

    private String getDescription(String displayString, int repoPriority, String selection) {
        return format("a proposal named '%s', repo priority: %d, selection: %s", displayString, repoPriority, selection);
    }
}

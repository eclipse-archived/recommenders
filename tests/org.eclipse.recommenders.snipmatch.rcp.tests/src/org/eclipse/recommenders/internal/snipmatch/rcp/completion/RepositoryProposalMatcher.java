package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static java.lang.String.format;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public final class RepositoryProposalMatcher extends TypeSafeMatcher<RepositoryProposal> {

    private final String name;
    private final int matches;
    private final int repoPriority;

    private RepositoryProposalMatcher(String repoName, int matches, int repoPriority) {
        this.name = repoName;
        this.matches = matches;
        this.repoPriority = repoPriority;
    }

    public static RepositoryProposalMatcher repository(String name, int matches, int repoPriority) {
        return new RepositoryProposalMatcher(name, matches, repoPriority);
    }

    @Override
    public boolean matchesSafely(RepositoryProposal proposal) {
        if (!name.equals(proposal.getName())) {
            return false;
        }

        if (matches != proposal.getNumberOfMatches()) {
            return false;
        }

        if (repoPriority != proposal.getRepositoryPriority()) {
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(getDescription(name, matches, repoPriority));
    }

    @Override
    public void describeMismatchSafely(RepositoryProposal proposal, Description description) {
        description.appendText("was ");
        description.appendText(getDescription(proposal.getName(), proposal.getNumberOfMatches(),
                proposal.getRepositoryPriority()));
    }

    private String getDescription(String name, int matches, int priority) {
        return format("a repository named '%s' with %d matches and priority %d", name, matches, priority);
    }
}

package org.eclipse.recommenders.internal.rcp.codesearch.client;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.codesearch.SnippetType;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.inject.internal.util.Lists;

public class RCPResponse {

    public static RCPResponse newInstance(final Response serverResponse) {
        final RCPResponse res = new RCPResponse();
        res.original = serverResponse;
        for (final Proposal proposal : serverResponse.proposals) {
            final RCPProposal rcpProposal = RCPProposal.newProposalFromServerProposal(proposal);
            res.proposals.add(rcpProposal);
        }
        return res;
    }

    public static class RCPProposal {

        public static RCPProposal newProposalFromServerProposal(final Proposal serverProposal) {
            final RCPProposal res = new RCPProposal();
            res.original = serverProposal;
            return res;
        }

        private Proposal original;

        private CompilationUnit ast;

        public SnippetSummary getSummary() {
            return original.snippet;
        }

        public float getScore() {
            return original.score;
        }

        public FeatureWeights getIndividualFeatureScores() {
            return original.individualFeatureScores;
        }

        public CompilationUnit getAst() {
            return ast;
        }

        public void setAST(final CompilationUnit ast) {
            this.ast = ast;
        }

        public SnippetType getType() {
            return getSummary().type;
        }

        public IMethodName getMethodName() {
            return getSummary().methodName;
        }

        public ITypeName getClassName() {
            return getSummary().className;
        }

        public Set<ITypeName> getUsedTypes() {
            return getSummary().usedTypes;
        }

        public Set<IMethodName> getCalledMethods() {
            return getSummary().calledMethods;
        }

        public URL getSource() {
            final URI uri = getSummary().source;
            try {
                final URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
                return url;
            } catch (final MalformedURLException e) {
                throw throwUnhandledException(e);
            }
        }

        public String getId() {
            return getSummary().id;
        }
    }

    private Response original;

    private final List<RCPProposal> proposals = Lists.newArrayList();

    public List<RCPProposal> getProposals() {
        return proposals;
    }

    public String getRequestId() {
        return original.requestId;
    }

    public boolean isEmpty() {
        return proposals.isEmpty();
    }

    public int getNumberOfProposals() {
        return proposals.size();
    }

}

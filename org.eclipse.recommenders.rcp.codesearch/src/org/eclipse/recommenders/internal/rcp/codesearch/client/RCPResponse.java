package org.eclipse.recommenders.internal.rcp.codesearch.client;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.commons.codesearch.FeatureWeights;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.codesearch.SnippetType;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.CrASTUtil;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

import com.google.inject.internal.util.Lists;

public class RCPResponse {

    public static RCPResponse newInstance(final Response serverResponse, final IJavaProject typesResolverContext) {
        final RCPResponse res = new RCPResponse();
        res.original = serverResponse;
        for (final Proposal proposal : serverResponse.proposals) {
            final RCPProposal rcpProposal = RCPProposal.newProposalFromServerProposal(proposal, typesResolverContext);
            res.proposals.add(rcpProposal);
        }
        return res;
    }

    public static class RCPProposal {

        public static RCPProposal newProposalFromServerProposal(final Proposal serverProposal,
                final IJavaProject typesResolverContext) {
            final RCPProposal res = new RCPProposal();
            res.original = serverProposal;
            res.resolverContext = typesResolverContext;
            return res;
        }

        private Proposal original;
        private IJavaProject resolverContext;

        private CompilationUnit ast;

        private String lazySource;

        public SnippetSummary getSummary() {
            return original.snippet;
        }

        public float getScore() {
            return original.score;
        }

        public FeatureWeights getIndividualFeatureScores() {
            return original.individualFeatureScores;
        }

        public CompilationUnit getAst(final IProgressMonitor monitor) {
            final String source = getSource(monitor);
            final ITypeName primaryType = getSummary().className;
            final CompilationUnit cu = CrASTUtil.createCompilationUnitFromString(primaryType, source, resolverContext);
            return cu;
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

        public URL getSourceURL() {
            try {
                final URI uri = getSummary().source;
                return uri.toURL();
            } catch (final MalformedURLException e) {
                throw throwUnhandledException(
                        "Source URL mapping failed for some internal reasons. Please report this error", e);
            }
        }

        public String getSource(final IProgressMonitor monitor) {
            try {
                if (lazySource == null) {
                    final InputStream stream = getSourceURL().openStream();
                    lazySource = IOUtils.toString(stream);
                }
                return lazySource;
            } catch (final IOException e) {
                RecommendersPlugin.logError(e, "failed to load source from server");
                return "";
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

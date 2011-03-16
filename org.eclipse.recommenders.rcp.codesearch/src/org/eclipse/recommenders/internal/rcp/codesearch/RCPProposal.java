package org.eclipse.recommenders.internal.rcp.codesearch;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.SnippetSummary;
import org.eclipse.recommenders.commons.codesearch.SnippetType;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codesearch.utils.CrASTUtil;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.utils.ast.MethodDeclarationFinder;
import org.eclipse.recommenders.rcp.utils.ast.TypeDeclarationFinder;

public class RCPProposal {

    public static RCPProposal newProposalFromServerProposal(final Proposal serverProposal,
            final IJavaProject typesResolverContext) {
        final RCPProposal res = new RCPProposal();
        res.original = serverProposal;
        res.resolverContext = typesResolverContext;
        return res;
    }

    private Proposal original;
    private IJavaProject resolverContext;

    private CompilationUnit lazyAst;

    private String lazySource;

    public SnippetSummary getSummary() {
        return original.snippet;
    }

    public float getScore() {
        return original.score;
    }

    // public FeatureWeights getIndividualFeatureScores() {
    // return original.individualFeatureScores;
    // }

    public CompilationUnit getAst(final IProgressMonitor monitor) {
        if (lazyAst == null) {
            final String source = getSource(monitor);
            final ITypeName primaryType = getSummary().className;
            lazyAst = CrASTUtil.createCompilationUnitFromString(primaryType, source, resolverContext);
            // final IProblem[] problems = lazyAst.getProblems();
            // if (problems.length > 0) {
            // for (final Message m : lazyAst.getMessages()) {
            // System.out.println(m.getMessage());
            // }
            // }
        }
        return lazyAst;
    }

    public MethodDeclaration getAstMethodDeclaration(final IProgressMonitor monitor) {
        if (getMethodName() == null) {
            return null;
        }
        return MethodDeclarationFinder.find(getAst(monitor), getMethodName());
    }

    public TypeDeclaration getAstTypeDeclaration(final IProgressMonitor monitor) {
        if (getClassName() == null) {
            return null;
        }
        return TypeDeclarationFinder.find(getAst(monitor), getClassName());
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
                System.out.println("loading " + getSourceURL());
                final InputStream stream = getSourceURL().openStream();
                lazySource = IOUtils.toString(stream);
                System.out.println("loading " + getSourceURL() + " done.");
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
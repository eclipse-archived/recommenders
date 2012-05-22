package org.eclipse.recommenders.tests.completion.rcp.templates;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

public class TestingTemplateCompletionProposalComputer extends TemplatesCompletionProposalComputer{

    public TestingTemplateCompletionProposalComputer(final IRecommendersCompletionContextFactory ctxFactory,
            final IModelArchiveStore<IType, IObjectMethodCallsNet> store, final JavaElementResolver elementResolver) {
        super(ctxFactory, store, elementResolver);
    }

    @Override
    protected boolean shouldMakeProposals() {
        return true;
    }

}

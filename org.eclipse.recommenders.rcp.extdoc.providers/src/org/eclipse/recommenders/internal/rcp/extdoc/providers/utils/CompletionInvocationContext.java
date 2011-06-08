package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.ui.IEditorPart;

public final class CompletionInvocationContext extends JavaContentAssistInvocationContext {

    public CompletionInvocationContext(final JavaContentAssistInvocationContext invocationContext,
            final IEditorPart editor) {
        super(invocationContext.getViewer(), invocationContext.getInvocationOffset(), editor);
    }

    @Override
    public String computeIdentifierPrefix() {
        return "";
    }

}

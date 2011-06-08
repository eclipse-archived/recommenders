/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import com.google.inject.Inject;

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractBrowserProvider;

public final class ExtendedJavadocProvider extends AbstractBrowserProvider {

    private static final String SEPARATOR = System.getProperty("line.separator");
    private final IntelligentCompletionContextResolver contextResolver;

    @Inject
    public ExtendedJavadocProvider(final IntelligentCompletionContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
    public String getHtmlContent(final IJavaElementSelection context) {
        final StringBuilder builder = new StringBuilder(128);
        IIntelligentCompletionContext completionContext = null;

        if (context.getInvocationContext() != null) {
            completionContext = contextResolver.resolveContext(context.getInvocationContext());
        }

        builder.append(String.format("%s%s%s%s", SEPARATOR, context, SEPARATOR, SEPARATOR));
        builder.append(completionContext);

        return builder.toString().replaceAll("\r?\n", "<br/>");
    }
}

/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henß - initial API and implementation.
 */
package org.eclipse.recommenders.internal.chain.rcp;

import java.lang.reflect.Field;

import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.utils.Reflections;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;

/**
 * A scope is required to determine for methods and fields if they are visible from the invocation site.
 */
@SuppressWarnings("restriction")
@Beta
public final class ScopeAccessWorkaround {

    private static final Field EXTENDED_CONTEXT = Reflections
            .getDeclaredField(true, InternalCompletionContext.class, "extendedContext").orNull(); //$NON-NLS-1$
    private static final Field ASSIST_SCOPE = Reflections
            .getDeclaredField(true, InternalExtendedCompletionContext.class, "assistScope").orNull(); //$NON-NLS-1$

    private ScopeAccessWorkaround() {
        // Not meant to be instantiated
    }

    public static Optional<Scope> resolveScope(final IRecommendersCompletionContext ctx) {
        InternalCompletionContext context = ctx.get(CompletionContextKey.INTERNAL_COMPLETIONCONTEXT, null);
        if (context == null) {
            return Optional.absent();
        }
        if (EXTENDED_CONTEXT == null || ASSIST_SCOPE == null) {
            return Optional.absent();
        }
        try {
            final InternalExtendedCompletionContext extendedContext = (InternalExtendedCompletionContext) EXTENDED_CONTEXT
                    .get(context);
            if (extendedContext == null) {
                return Optional.absent();
            }
            return Optional.fromNullable((Scope) ASSIST_SCOPE.get(extendedContext));
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}

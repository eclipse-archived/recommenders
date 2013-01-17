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
package org.eclipse.recommenders.internal.completion.rcp.chain;

import java.lang.reflect.Field;

import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.utils.annotations.Provisional;

import com.google.common.base.Optional;

/**
 * A scope is required to determine for methods and fields if they are visible from the invocation site.
 */
@Provisional
public final class ScopeAccessWorkaround {

    private static Field extendedContextField;
    private static Field assistScopeField;

    private ScopeAccessWorkaround() {
    }

    static {
        try {
            extendedContextField = InternalCompletionContext.class.getDeclaredField("extendedContext"); //$NON-NLS-1$
            extendedContextField.setAccessible(true);
            assistScopeField = InternalExtendedCompletionContext.class.getDeclaredField("assistScope"); //$NON-NLS-1$
            assistScopeField.setAccessible(true);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static Optional<Scope> resolveScope(final IRecommendersCompletionContext ctx) {
        final InternalCompletionContext context = (InternalCompletionContext) ctx.getJavaContext().getCoreContext();
        if (context == null) {
            return Optional.absent();
        }
        try {
            final InternalExtendedCompletionContext extendedContext = (InternalExtendedCompletionContext) extendedContextField
                    .get(context);
            if (extendedContext == null) {
                return Optional.absent();
            }
            return Optional.fromNullable((Scope) assistScopeField.get(extendedContext));
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}

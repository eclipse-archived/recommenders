/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp;

import org.eclipse.recommenders.utils.Nullable;

/**
 * Completion context functions provide an extensible API for the {@link IRecommendersCompletionContext}. Extenders may
 * register own implementations by providing them in a separate Guice module or adding them at runtime to the active
 * completion contexts by calling {@link IRecommendersCompletionContext#set(String, Object)} and passing an
 * ICompletionContextFunction as value.
 */
public interface ICompletionContextFunction<T> {

    /**
     * Computes some value for the specified key and from the given context. It's up to the function to either cache the
     * result for repeated accesses by storing it into context under the given key before returning or (ii) to compute
     * the value every time from scratch when a callee requests the key.
     * 
     * @param key
     *            the key used to lookup the value in the context later.
     * @return the computed value or {@code null}
     */
    @Nullable
    T compute(IRecommendersCompletionContext context, String key);
}

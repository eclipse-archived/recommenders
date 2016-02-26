/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.news.api.poll;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;

public interface INewsPollingService {

    /**
     * @param requests
     *            A collection of polling requests
     * @param monitor
     *            A progress monitor, or <code>null</code> if progress reporting and cancellation are not desired
     * @return A collection of polling results, one for each request
     */
    Collection<PollingResult> poll(Collection<PollingRequest> requests, @Nullable IProgressMonitor monitor);
}

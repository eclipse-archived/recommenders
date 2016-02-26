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

import java.net.URI;

public final class PollingRequest {

    private final URI feedUri;
    private final PollingPolicy pollingPolicy;

    public PollingRequest(URI feedUri, PollingPolicy pollingPolicy) {
        this.feedUri = feedUri;
        this.pollingPolicy = pollingPolicy;
    }

    public URI getFeedUri() {
        return feedUri;
    }

    public PollingPolicy getPollingPolicy() {
        return pollingPolicy;
    }
}

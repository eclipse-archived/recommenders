/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.util.UUID;

public class SnippetAppliedEvent {

    private final UUID uuid;
    private final String repoUri;

    public SnippetAppliedEvent(UUID uuid, String repoUri) {
        this.uuid = uuid;
        this.repoUri = repoUri;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getRepoUri() {
        return repoUri;
    }
}

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
package org.eclipse.recommenders.server.extdoc;

import org.eclipse.recommenders.internal.server.extdoc.AbstractCommentsServer;

import com.google.inject.Inject;

public final class GenericServer extends AbstractCommentsServer {

    @Inject
    public GenericServer(final ICouchDbServer server, final UsernamePreferenceListener usernameListener) {
        super(server, usernameListener);
    }

}

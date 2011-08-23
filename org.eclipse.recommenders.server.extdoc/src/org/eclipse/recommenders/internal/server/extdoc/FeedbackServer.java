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
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.recommenders.server.extdoc.ICouchDbServer;
import org.eclipse.recommenders.server.extdoc.UsernameProvider;

import com.google.inject.Inject;

/**
 * A provider server only containing community feedback support.
 */
final class FeedbackServer extends AbstractFeedbackServer {

    @Inject
    FeedbackServer(final ICouchDbServer server, final UsernameProvider usernameListener) {
        super(server, usernameListener);
    }

}

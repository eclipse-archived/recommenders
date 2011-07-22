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

import java.util.Collections;
import java.util.List;

import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.extdoc.features.ICommentsServer;

public final class CallsServer extends AbstractRatingsServer implements ICommentsServer {

    @Override
    public List<IComment> getComments(final Object object) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public IComment addComment(final Object object, final String text) {
        // TODO Auto-generated method stub
        return null;
    }

}

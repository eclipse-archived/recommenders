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
package org.eclipse.recommenders.server.extdoc.types;

import java.util.Date;

public final class Comment {

    private String text;
    private Date date;

    public static Comment create(final Object object, final String text) {
        final Comment comment = new Comment();
        comment.text = text;
        comment.date = new Date();
        return comment;
    }

    String getText() {
        return text;
    }

    Date getDate() {
        return date;
    }

}

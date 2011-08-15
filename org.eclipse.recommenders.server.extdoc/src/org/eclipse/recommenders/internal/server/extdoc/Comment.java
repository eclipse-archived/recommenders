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

import java.util.Date;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.rcp.extdoc.features.IComment;
import org.eclipse.recommenders.rcp.utils.UUIDHelper;

/**
 * A comment submitted by a user to a provider.
 */
final class Comment implements IComment {

    private Date date;
    private String user;
    private String text;
    private String username;

    /**
     * @param text
     *            The comment's text.
     * @param username
     *            The displayed user name set by the user.
     * @return The created comment object.
     */
    static Comment create(final String text, final String username) {
        final Comment comment = new Comment();
        comment.date = new Date();
        comment.text = text;
        comment.user = UUIDHelper.getUUID();
        comment.username = username;
        comment.validate();
        return comment;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void validate() {
        Checks.ensureIsNotNull(getDate());
        Checks.ensureIsTrue(!user.isEmpty());
        Checks.ensureIsTrue(!getText().isEmpty());
        Checks.ensureIsTrue(!getUsername().isEmpty());
    }

    @Override
    public int hashCode() {
        return date.hashCode() + text.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return this == object || object instanceof IComment && object.hashCode() == hashCode();
    }

}

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
package org.eclipse.recommenders.rcp.extdoc.feedback;

import java.util.Date;

import org.eclipse.recommenders.rcp.extdoc.IServerType;

/**
 * A comment submitted by a user to a provider.
 */
public interface IComment extends IServerType {

    /**
     * @return The date the comment was submitted.
     */
    Date getDate();

    /**
     * @return The displayed author name.
     */
    String getUsername();

    /**
     * @return The text of the comment.
     */
    String getText();

}

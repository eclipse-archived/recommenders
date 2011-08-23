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
package org.eclipse.recommenders.rcp.extdoc.features;

import java.util.Date;

import org.eclipse.recommenders.rcp.extdoc.IServerType;

public interface IRating extends IServerType {

    /**
     * @return A value from 1 to 5 where 5 is the best rating.
     */
    int getRating();

    /**
     * @return An internal unique user ID allowing to associate ratings with
     *         users.
     */
    String getUserId();

    /**
     * @return The date the rating was submitted.
     */
    Date getDate();

}

/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.news.rcp;

import java.net.URL;
import java.util.Date;

public interface IFeedMessage {
    /**
     * @return the unique identifier of the message.
     */
    String getId();

    /**
     * @return the date of item's publishing. Supports RSS 2.0 dates
     */
    Date getDate();

    /**
     * @return the item synopsis.
     */
    String getDescription();

    /**
     * @return the title of the message.
     */
    String getTitle();

    /**
     * @return the URL of the message. Should not be null, otherwise the message won't be opened
     */
    URL getUrl();

    /**
     * @return states whether message has been read by user or not.
     */
    boolean isRead();

    /**
     * sets the message as read/unread
     */
    void setRead(boolean read);
}

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

    String getId();

    Date getDate();

    String getDescription();

    String getTitle();

    URL getUrl();

    boolean isRead();

    void markAsRead();
}

/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.codesearch;

public enum FeedbackType {

    /**
     * User opened the code snippet in his editor and also copied some content
     * from the snippet's source.
     */
    TEXT_COPIED,

    /**
     * User opened the given code snippet in his/her editor.
     */
    EDITOR_OPENED,

    /**
     * User rated this snippet as being helpful
     */
    RATED_USEFUL,

    /**
     * This item has not been rated at all.
     */
    NOT_RATED,

    /**
     * User rated this snippet as being not helpful
     */
    RATED_USELESS,
}
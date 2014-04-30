/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn- initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.util.List;

import org.eclipse.recommenders.utils.Uuidable;

public interface ISnippet extends Uuidable {

    /**
     * This will be displayed in Snipmatch search.
     * 
     * @return a short (approximately 50 characters) description of the snippet. Examples are {@literal "Add Button"} or
     *         {@literal "Iterate over a Collection"}.
     */
    String getName();

    /**
     * @return a user-visible, full-sentence explanation of what the snippet does. This may encompass hints about
     *         correct usage.
     */
    String getDescription();

    /**
     * @return the snippet in JFace Template syntax
     */
    String getCode();

    /**
     * @return a list of user-visible tags from an uncontrolled vocabulary.
     */
    List<String> getTags();

    /**
     * @return a list of keywords describing the snippet. Typically keywords are synonyms of common search terms.
     */
    List<String> getKeywords();
}

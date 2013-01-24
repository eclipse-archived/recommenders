package org.eclipse.recommenders.webclient.results;

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResultObject<T> {

    public static <T> ResultObject<T> create(final String id, final T value) {
        final ResultObject<T> res = new ResultObject<T>();
        res.id = id;
        res.value = value;
        return res;
    }

    public String id;
    public T value;
}

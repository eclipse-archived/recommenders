/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.utils;

import org.eclipse.core.databinding.conversion.IConverter;

public class ObjectToBooleanConverter implements IConverter {
    @Override
    public Object getFromType() {
        return Object.class;
    }

    @Override
    public Object getToType() {
        return Boolean.TYPE;
    }

    @Override
    public Boolean convert(Object fromObject) {
        return fromObject != null;
    }
}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.gson;

import java.lang.reflect.Type;

import org.eclipse.recommenders.utils.names.IName;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonNameSerializer implements JsonSerializer<IName> {
    @Override
    public JsonElement serialize(final IName src, final Type typeOfSrc, final JsonSerializationContext context) {
        return new JsonPrimitive(src.getIdentifier());
    }
}

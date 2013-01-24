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
package org.eclipse.recommenders.utils.gson;

import static org.eclipse.recommenders.utils.Checks.ensureEquals;
import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MultimapTypeAdapter implements JsonSerializer<Multimap>, JsonDeserializer<Multimap> {

    @Override
    public JsonElement serialize(final Multimap src, final Type typeOfSrc, final JsonSerializationContext context) {
        return context.serialize(src.asMap(), createMapType(typeOfSrc));
    }

    @Override
    public Multimap deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
            throws JsonParseException {
        final Multimap multimap = HashMultimap.create();
        final Map map = context.deserialize(json, createMapType(typeOfT));
        for (final Object key : map.keySet()) {
            final Collection values = (Collection) map.get(key);
            multimap.putAll(key, values);
        }

        return multimap;
    }

    private Type createMapType(final Type multimapType) {
        final ParameterizedType paramType = ensureIsInstanceOf(multimapType, ParameterizedType.class);
        final Type[] typeArguments = paramType.getActualTypeArguments();
        ensureEquals(2, typeArguments.length, "Type must contain exactly 2 type arguments.");

        final ParameterizedTypeImpl valueType = new ParameterizedTypeImpl(Collection.class, null, typeArguments[1]);
        final ParameterizedTypeImpl mapType = new ParameterizedTypeImpl(Map.class, null, typeArguments[0], valueType);
        return mapType;
    }

}

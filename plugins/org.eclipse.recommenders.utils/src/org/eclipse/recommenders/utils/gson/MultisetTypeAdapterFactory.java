/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.utils.gson;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class MultisetTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();
        if (typeToken.getRawType() != Multiset.class || !(type instanceof ParameterizedType)) {
            return null;
        }

        Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
        TypeAdapter<?> elementAdapter = gson.getAdapter(TypeToken.get(elementType));
        return (TypeAdapter<T>) newMultisetAdapter(elementAdapter);
    }

    private <E> TypeAdapter<Multiset<E>> newMultisetAdapter(final TypeAdapter<E> elementAdapter) {
        return new TypeAdapter<Multiset<E>>() {
            public void write(JsonWriter out, Multiset<E> multiset) throws IOException {
                if (multiset == null) {
                    out.nullValue();
                    return;
                }

                out.beginArray();
                for (Entry<E> entry : multiset.entrySet()) {
                    out.beginObject();
                    out.name("id");
                    elementAdapter.write(out, entry.getElement());
                    out.name("count");
                    out.value(entry.getCount());
                    out.endObject();
                }
                out.endArray();
            }

            public Multiset<E> read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }

                Multiset<E> result = LinkedHashMultiset.create();
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();
                    in.nextName(); // "id"
                    E element = elementAdapter.read(in);
                    in.nextName(); // "count"
                    int count = in.nextInt();
                    result.add(element, count);
                    in.endObject();
                }
                in.endArray();
                return result;
            }
        };
    }
}

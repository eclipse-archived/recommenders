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
package org.eclipse.recommenders.utils.gson;

import java.io.IOException;

import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.VmFieldName;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class FieldNameTypeAdapter extends TypeAdapter<IFieldName> {

    @Override
    public void write(JsonWriter out, IFieldName value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getIdentifier());
        }
    }

    @Override
    public IFieldName read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String identifier = in.nextString();
            return VmFieldName.get(identifier);
        }
    }
}

/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.utils.gson;

import java.io.IOException;

import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.VmPackageName;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class PackageNameTypeAdapter extends TypeAdapter<IPackageName> {

    @Override
    public void write(JsonWriter out, IPackageName value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getIdentifier());
        }
    }

    @Override
    public IPackageName read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String identifier = in.nextString();
            return VmPackageName.get(identifier);
        }
    }
}

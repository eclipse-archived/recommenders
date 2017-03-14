/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.coordinates.rcp;

import java.io.File;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * For legacy reasons, this {@link TypeAdapter} encodes a {@link File} with path {@literal p} as a JSON object {@code { "path": "p" }} rather
 * than a simple JSON string {@code "p"}.
 * 
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=513634">Bug 513634</a>
 */
public class FileJsonTypeAdapter extends TypeAdapter<File> {

    private static final String PATH_PROPERTY = "path";

    @Override
    public void write(JsonWriter out, File value) throws IOException {
        out.beginObject();
        out.name(PATH_PROPERTY);
        out.value(value.getPath());
        out.endObject();
    }

    @Override
    public File read(JsonReader in) throws IOException {
        in.beginObject();
        String name = in.nextName();
        if (!(PATH_PROPERTY.equals(name))) {
            throw new IOException(String.format("Expected property '%s'", PATH_PROPERTY));
        }
        String path = in.nextString();
        in.endObject();
        return new File(path);
    }
}

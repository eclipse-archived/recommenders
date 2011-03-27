package org.eclipse.recommenders.commons.utils.gson;

import java.io.File;
import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonFileSerializer implements JsonSerializer<File> {

    @Override
    public JsonElement serialize(final File src, final Type typeOfSrc, final JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

}

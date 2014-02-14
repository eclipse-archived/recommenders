/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * {@link JsonSerializer} and {@link JsonDeserializer} implementation for {@link DependencInfo}. Examples for the json
 * representation:
 * <ul>
 * <li>{"location":"D:\\text.jar","type":"JAR","hints":{"k1":"v1","k2":"v2"}}
 * </ul>
 */
public class DependencyInfoJsonTypeAdapter implements JsonSerializer<DependencyInfo>, JsonDeserializer<DependencyInfo> {

    @SuppressWarnings("serial")
    private final Type cacheType = new TypeToken<Map<String, String>>() {
    }.getType();

    @Override
    public DependencyInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonPrimitive fileElement = jsonObject.getAsJsonPrimitive("location"); //$NON-NLS-1$
        File file = new File(fileElement.getAsString());

        JsonElement typeElement = jsonObject.get("type"); //$NON-NLS-1$
        DependencyType type = context.deserialize(typeElement, DependencyType.class);

        JsonElement hintsElement = jsonObject.get("hints"); //$NON-NLS-1$
        Map<String, String> hints = context.deserialize(hintsElement, cacheType);

        return new DependencyInfo(file, type, hints);
    }

    @Override
    public JsonElement serialize(DependencyInfo src, Type typeOfSrc, JsonSerializationContext context) {
        JsonPrimitive file = new JsonPrimitive(src.getFile().getAbsoluteFile().toString());
        JsonElement type = context.serialize(src.getType());
        JsonElement hints = context.serialize(src.getHints());

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("location", file); //$NON-NLS-1$
        jsonObject.add("type", type); //$NON-NLS-1$
        jsonObject.add("hints", hints); //$NON-NLS-1$
        return jsonObject;
    }

}

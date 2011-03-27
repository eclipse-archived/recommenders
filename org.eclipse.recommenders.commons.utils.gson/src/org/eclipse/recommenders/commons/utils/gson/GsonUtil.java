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
package org.eclipse.recommenders.commons.utils.gson;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Date;

import org.eclipse.recommenders.commons.utils.IOUtils;
import org.eclipse.recommenders.commons.utils.names.IFieldName;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmFieldName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    private static Gson gson;

    public static synchronized Gson getInstance() {
        if (gson == null) {
            final GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(VmMethodName.class, new GsonNameSerializer());
            builder.registerTypeAdapter(IMethodName.class, new GsonNameSerializer());
            builder.registerTypeAdapter(VmMethodName.class, new GsonMethodNameDeserializer());
            builder.registerTypeAdapter(IMethodName.class, new GsonMethodNameDeserializer());
            builder.registerTypeAdapter(VmTypeName.class, new GsonNameSerializer());
            builder.registerTypeAdapter(ITypeName.class, new GsonNameSerializer());
            builder.registerTypeAdapter(VmTypeName.class, new GsonTypeNameDeserializer());
            builder.registerTypeAdapter(ITypeName.class, new GsonTypeNameDeserializer());
            builder.registerTypeAdapter(VmFieldName.class, new GsonNameSerializer());
            builder.registerTypeAdapter(IFieldName.class, new GsonNameSerializer());
            builder.registerTypeAdapter(VmFieldName.class, new GsonFieldNameDeserializer());
            builder.registerTypeAdapter(IFieldName.class, new GsonFieldNameDeserializer());
            //
            builder.registerTypeAdapter(File.class, new GsonFileDeserializer());
            builder.registerTypeAdapter(File.class, new GsonFileSerializer());
            builder.setPrettyPrinting();
            // builder.setDateFormat("dd.MM.yyyy HH:mm:ss");
            builder.registerTypeAdapter(Date.class, new ISO8601DateParser());
            gson = builder.create();
        }
        return gson;
    }

    public static <T> T deserialize(final String json, final Type classOfT) {
        ensureIsNotNull(json);
        ensureIsNotNull(classOfT);
        return getInstance().fromJson(json, classOfT);
    }

    public static <T> T deserialize(final InputStream jsonStream, final Type classOfT) {
        ensureIsNotNull(jsonStream);
        ensureIsNotNull(classOfT);
        final InputStreamReader reader = new InputStreamReader(jsonStream);
        final T res = getInstance().fromJson(reader, classOfT);
        IOUtils.closeQuietly(reader);
        return res;
    }

    public static <T> T deserialize(final File jsonFile, final Type classOfT) throws IOException {
        ensureIsNotNull(jsonFile);
        ensureIsNotNull(classOfT);
        final FileInputStream fis = new FileInputStream(jsonFile);
        return deserialize(fis, classOfT);
    }

    public static String serialize(final Object obj) {
        ensureIsNotNull(obj);
        final StringBuilder sb = new StringBuilder();
        serialize(obj, sb);
        return sb.toString();
    }

    public static void serialize(final Object obj, final Appendable writer) {
        ensureIsNotNull(obj);
        ensureIsNotNull(writer);
        getInstance().toJson(obj, writer);
    }

    public static void serialize(final Object obj, final File dest) {
        ensureIsNotNull(obj);
        ensureIsNotNull(dest);
        FileWriter fw = null;
        try {
            fw = new FileWriter(dest);
            getInstance().toJson(obj, fw);
        } catch (final IOException x) {
            throwUnhandledException(x);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }
}

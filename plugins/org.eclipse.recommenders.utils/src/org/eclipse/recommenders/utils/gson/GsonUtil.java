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

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.recommenders.utils.IOUtils;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GsonUtil {
    public static final Type T_LIST_STRING = new TypeToken<List<String>>() {
    }.getType();

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
            builder.registerTypeAdapter(Multimap.class, new MultimapTypeAdapter());
            builder.enableComplexMapKeySerialization();
            gson = builder.create();
        }
        return gson;
    }

    public static <T> T deserialize(final CharSequence json, final Type classOfT) {
        return deserialize(json.toString(), classOfT);
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

    public static <T> T deserialize(final File jsonFile, final Type classOfT) {
        ensureIsNotNull(jsonFile);
        ensureIsNotNull(classOfT);
        InputStream fis;
        try {
            fis = new BufferedInputStream(new FileInputStream(jsonFile));
        } catch (final FileNotFoundException e) {
            throw Throws.throwUnhandledException(e, "Unable to deserialize from file " + jsonFile.getAbsolutePath());
        }
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
        Writer fw = null;
        try {
            fw = new BufferedWriter(new FileWriter(dest));
            getInstance().toJson(obj, fw);
        } catch (final IOException x) {
            throwUnhandledException(x);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    public static <T> List<T> deserializeZip(File zip, Class<T> classOfT) throws IOException {

        List<T> res = Lists.newLinkedList();
        ZipInputStream zis = null;
        try {
            InputSupplier<FileInputStream> fis = Files.newInputStreamSupplier(zip);
            zis = new ZipInputStream(fis.getInput());
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    final InputStreamReader reader = new InputStreamReader(zis);
                    final T data = getInstance().fromJson(reader, classOfT);
                    res.add(data);
                }
            }
        } finally {
            Closeables.closeQuietly(zis);
        }
        return res;
    }
}

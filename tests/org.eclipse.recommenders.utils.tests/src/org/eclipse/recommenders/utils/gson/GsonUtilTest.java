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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.recommenders.utils.NamesTest;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class GsonUtilTest {
    @Test
    public void testMethodNameDeserialization() {
        // setup
        final IMethodName expected = NamesTest.STRING_HASHCODE;
        // exercise
        final JsonElement e = new JsonPrimitive(expected.getIdentifier());
        final IMethodName actual1 = new GsonMethodNameDeserializer().deserialize(e, VmMethodName.class, null);
        final IMethodName actual2 = new GsonMethodNameDeserializer().deserialize(e, IMethodName.class, null);
        // verify
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    @Test
    public void testMethodNameSerialization() {
        // setup
        final IMethodName type = NamesTest.STRING_HASHCODE;
        final JsonElement expected = new JsonPrimitive(type.getIdentifier());
        // exercise
        final JsonElement actual = new GsonNameSerializer().serialize(type, IMethodName.class, null);
        // verify
        assertEquals(expected, actual);
    }

    @Test
    public void testTypeNameSerialization() {
        // setup
        final ITypeName typeName = NamesTest.STRING;
        final JsonElement expected = new JsonPrimitive(typeName.getIdentifier());
        // exercise
        final JsonElement actual = new GsonNameSerializer().serialize(typeName, ITypeName.class, null);
        // verify
        assertEquals(expected, actual);
    }

    @Test
    public void testEmptyFileDeserialization() throws IOException {
        // setup
        File f = File.createTempFile("tmp", ".json");
        f.deleteOnExit();

        // exercise
        Object res = GsonUtil.deserialize(f, new TypeToken<Map<File, String>>() {
        }.getType());

        // it's actually null :)
        assertNull(res);
    }

    @Test
    public void testTypeNameDeserialization() {
        // setup
        final ITypeName expected = NamesTest.STRING;
        // exercise
        final JsonElement e = new JsonPrimitive(expected.getIdentifier());
        final ITypeName actual1 = new GsonTypeNameDeserializer().deserialize(e, VmTypeName.class, null);
        final ITypeName actual2 = new GsonTypeNameDeserializer().deserialize(e, ITypeName.class, null);
        // verify
        assertEquals(expected, actual1);
        assertEquals(expected, actual2);
    }

    @Test
    public void testPrettyPrint() {
        final GsonTestStruct struct = GsonTestStruct.create("string", 0.43d, "s1", "s2");
        GsonUtil.serialize(struct);
    }

    @Test
    public void testSerializationRoundTrip() {
        // setup
        final GsonTestStruct input = GsonTestStruct.create("string", 0.43d, "s1", "s2");
        // exercise
        final String json = GsonUtil.serialize(input);
        final GsonTestStruct output = GsonUtil.deserialize(json, GsonTestStruct.class);
        // verify
        assertEquals(input, output);
    }

    @Test
    public void testSerializationRoundTrip_ViaInputStream() {
        // setup
        final GsonTestStruct input = GsonTestStruct.create("string", 0.43d, "s1", "s2");
        // exercise
        final String json = GsonUtil.serialize(input);
        final GsonTestStruct output = GsonUtil.deserialize(new ByteArrayInputStream(json.getBytes()),
                GsonTestStruct.class);
        // verify
        assertEquals(input, output);
    }

    @Test
    public void testSerializationPrettyRoundTrip_ViaInputStream() {
        // setup
        final GsonTestStruct input = GsonTestStruct.create("string", 0.43d, "s1", "s2");
        // exercise
        final String prettyJson = GsonUtil.serialize(input);
        final GsonTestStruct output = GsonUtil.deserialize(prettyJson, GsonTestStruct.class);
        // verify
        assertEquals(input, output);
    }

    @Test
    public void testMultimapOfStrings() {
        // setup:
        final Multimap<String, String> map = HashMultimap.create();
        map.put("key", "value1");
        map.put("key", "value2");
        // exercise:
        final String json = GsonUtil.getInstance().toJson(map, new TypeToken<Multimap<String, String>>() {
        }.getType());
        final Multimap<String, String> output = GsonUtil.deserialize(json, new TypeToken<Multimap<String, String>>() {
        }.getType());
        // verify
        assertEquals(map, output);
    }
}

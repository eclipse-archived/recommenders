package org.eclipse.recommenders.utils.gson;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import org.eclipse.recommenders.utils.NamesTest;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class TypeAdaptersTest {

    private final Object value;
    private final Class<?> type;

    public TypeAdaptersTest(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    @Parameters
    public static Iterable<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        scenarios.add(scenario(NamesTest.STRING, ITypeName.class));
        scenarios.add(scenario(NamesTest.STRING, VmTypeName.class));

        scenarios.add(scenario(NamesTest.STRING_NEW, IMethodName.class));
        scenarios.add(scenario(NamesTest.STRING_NEW, VmMethodName.class));

        scenarios.add(scenario(NamesTest.EVENT_FIELD, IFieldName.class));
        scenarios.add(scenario(NamesTest.EVENT_FIELD, VmFieldName.class));

        scenarios.add(scenario(NamesTest.JAVA_LANG, IPackageName.class));
        scenarios.add(scenario(NamesTest.JAVA_LANG, VmPackageName.class));

        scenarios.add(scenario(new File("/tmp/example.json"), File.class));

        scenarios.add(scenario(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"), UUID.class));

        scenarios.add(scenario(new Date(0), Date.class));

        scenarios.add(scenario(HashMultimap.create(), HashMultimap.class));

        scenarios.add(scenario(HashMultiset.create(), HashMultiset.class));
        scenarios.add(scenario(HashMultiset.create(asList("x", "y", "z", "z", "y")), HashMultiset.class));

        return scenarios;
    }

    private static <T> Object[] scenario(T value, Class<? extends T> type) {
        return new Object[] { value, type };
    }

    @Test
    public void testSerializationRoundtrip() {
        String serializedValue = GsonUtil.serialize(value);
        Object deserializedValue = GsonUtil.deserialize(serializedValue, type);

        assertThat(deserializedValue, is(equalTo(value)));
    }
}

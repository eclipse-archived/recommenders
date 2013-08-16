package org.eclipse.recommenders.utils.gson;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.eclipse.recommenders.utils.NamesTest;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TypeAdaptersTest {

    private final Object value;
    private final Class<?> type;

    public TypeAdaptersTest(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    @Parameters
    public static Iterable<Object[]> parameters() {
        return Arrays.asList(value(NamesTest.STRING, ITypeName.class), value(NamesTest.STRING, VmTypeName.class),
                value(NamesTest.STRING_NEW, IMethodName.class), value(NamesTest.STRING_NEW, VmMethodName.class),
                value(NamesTest.EVENT_FIELD, IFieldName.class), value(NamesTest.EVENT_FIELD, VmFieldName.class),
                value(new File("C:/test/test.json"), File.class),
                value(UUID.fromString("01234567-89ab-cdef-0123-456789abcdef"), UUID.class),
                value(new Date(0), Date.class));
    }

    @Test
    public void testSerializationRoundtrip() {
        String serializedValue = GsonUtil.serialize(value);
        Object deserializedValue = GsonUtil.deserialize(serializedValue, type);

        assertThat(deserializedValue, is(equalTo(value)));
    }

    private static <T> Object[] value(T value, Class<? extends T> type) {
        return new Object[] { value, type };
    }
}

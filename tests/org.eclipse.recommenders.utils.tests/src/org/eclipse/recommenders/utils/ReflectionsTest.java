package org.eclipse.recommenders.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FilterInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

import com.google.common.base.Optional;

public class ReflectionsTest {

    @Test
    public void testGetDeclaredPublicMethod() {
        Optional<Method> result = Reflections.getDeclaredMethod(Object.class, "hashCode");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredProtectedMethod() {
        Optional<Method> result = Reflections.getDeclaredMethod(Object.class, "clone");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredMissingMethod() {
        Optional<Method> result = Reflections.getDeclaredMethod(Object.class, "missing");

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredMethodIsNullSafe() {
        Optional<Method> declaringClassIsNull = Reflections.getDeclaredMethod(null, "hashCode");
        Optional<Method> nameIsNull = Reflections.getDeclaredMethod(Object.class, null);
        Optional<Method> parameterTypeIsNull = Reflections.getDeclaredMethod(Object.class, "hashCode", (Class[]) null);

        assertThat(declaringClassIsNull.isPresent(), is(false));
        assertThat(nameIsNull.isPresent(), is(false));
        assertThat(parameterTypeIsNull.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredMethodWithAlternativeSignatures() {
        Optional<Method> result = Reflections.getDeclaredMethodWithAlternativeSignatures(Object.class, "equals",
                new Class[] { String.class }, new Class[] { Object.class });

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredMethodWithoutMatchingAlternativeSignatures() {
        Optional<Method> result = Reflections.getDeclaredMethodWithAlternativeSignatures(Object.class, "equals",
                new Class[] { String.class });

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredMethodWithAlternativeSignaturesIsNullSafe() {
        Optional<Method> declaringClassIsNull = Reflections.getDeclaredMethodWithAlternativeSignatures(null,
                "hashCode");
        Optional<Method> nameIsNull = Reflections.getDeclaredMethodWithAlternativeSignatures(Object.class, null);
        Optional<Method> parameterTypeIsNull = Reflections.getDeclaredMethodWithAlternativeSignatures(Object.class,
                "hashCode", (Class[][]) null);

        assertThat(declaringClassIsNull.isPresent(), is(false));
        assertThat(nameIsNull.isPresent(), is(false));
        assertThat(parameterTypeIsNull.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredPublicField() {
        Optional<Field> result = Reflections.getDeclaredField(System.class, "in");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredProtectedField() {
        Optional<Field> result = Reflections.getDeclaredField(FilterInputStream.class, "in");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredMissingField() {
        Optional<Field> result = Reflections.getDeclaredField(System.class, "missing");

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredFieldIsNullSafe() {
        Optional<Field> declaringClassIsNull = Reflections.getDeclaredField(null, "in");
        Optional<Field> nameIsNull = Reflections.getDeclaredField(System.class, null);

        assertThat(declaringClassIsNull.isPresent(), is(false));
        assertThat(nameIsNull.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredPublicConstructor() {
        Optional<Constructor<String>> result = Reflections.getDeclaredConstructor(String.class, String.class);

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredProtectedConstructor() {
        Optional<Constructor<Exception>> result = Reflections.getDeclaredConstructor(Exception.class, String.class,
                Throwable.class, boolean.class, boolean.class);

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredMissingConstructor() {
        Optional<Constructor<String>> result = Reflections.getDeclaredConstructor(String.class, Object.class);

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredConstructorIsNullSafe() {
        Optional<Constructor<String>> declaringClassIsNull = Reflections.getDeclaredConstructor(null, String.class);
        Optional<Constructor<String>> parameterTypeIsNull = Reflections.getDeclaredConstructor(String.class,
                (Class[]) null);

        assertThat(declaringClassIsNull.isPresent(), is(false));
        assertThat(parameterTypeIsNull.isPresent(), is(false));
    }
}

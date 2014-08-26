package org.eclipse.recommenders.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FilterInputStream;
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
}

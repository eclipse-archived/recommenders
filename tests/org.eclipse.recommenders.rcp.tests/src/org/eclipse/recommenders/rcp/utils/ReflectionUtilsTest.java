package org.eclipse.recommenders.rcp.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FilterInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

import com.google.common.base.Optional;

public class ReflectionUtilsTest {

    @Test
    public void testGetDeclaredPublicMethod() {
        Optional<Method> result = ReflectionUtils.getDeclaredMethod(Object.class, "hashCode");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredProtectedMethod() {
        Optional<Method> result = ReflectionUtils.getDeclaredMethod(Object.class, "clone");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredMissingMethod() {
        Optional<Method> result = ReflectionUtils.getDeclaredMethod(Object.class, "missing");

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void testGetDeclaredPublicField() {
        Optional<Field> result = ReflectionUtils.getDeclaredField(System.class, "in");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredProtectedField() {
        Optional<Field> result = ReflectionUtils.getDeclaredField(FilterInputStream.class, "in");

        assertThat(result.isPresent(), is(true));
    }

    @Test
    public void testGetDeclaredMissingField() {
        Optional<Field> result = ReflectionUtils.getDeclaredField(System.class, "missing");

        assertThat(result.isPresent(), is(false));
    }
}

package org.eclipse.recommenders.tests.wala;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class CompilerUtilTest {

    @Test
    public void test() throws IOException {
        final CompilerUtil sut = new CompilerUtil();
        final File out = sut.getClassesLocation();
        assertNotNull(out);
        final File cu = new File(out, "MyClass.class");
        assertFalse(cu.exists());
        assertTrue(sut.compile("public class MyClass {}"));
        assertTrue(cu.exists());
    }
}

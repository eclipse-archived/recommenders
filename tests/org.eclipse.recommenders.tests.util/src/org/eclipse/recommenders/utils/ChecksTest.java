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
package org.eclipse.recommenders.utils;

import static org.eclipse.recommenders.utils.Checks.ensureEquals;
import static org.eclipse.recommenders.utils.Checks.ensureExists;
import static org.eclipse.recommenders.utils.Checks.ensureIsDirectory;
import static org.eclipse.recommenders.utils.Checks.ensureIsFalse;
import static org.eclipse.recommenders.utils.Checks.ensureIsFile;
import static org.eclipse.recommenders.utils.Checks.ensureIsGreaterOrEqualTo;
import static org.eclipse.recommenders.utils.Checks.ensureIsInRange;
import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotEmpty;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotInstanceOf;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotZero;
import static org.eclipse.recommenders.utils.Checks.ensureIsNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ChecksTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNotNullWithNullValue() {
        ensureIsNotNull(null);
    }

    @Test
    public void testEnsureIsNotNullWithNonNullValue() {
        ensureIsNotNull("non-null");
        // success with non-null
    }

    @Test
    public void testEnsureEquals() {
        final String value = "message";
        final String expected = new String("message");
        ensureEquals(value, expected, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureEqualsFails() {
        final String value = "message-different";
        final String expected = new String("message");
        ensureEquals(value, expected, "");
    }

    @Test
    public void testEnsureExists() {
        final File file = getTempDir();
        ensureExists(file);
    }

    private File getTempDir() {
        final String tmp = System.getProperties().getProperty("java.io.tmpdir");
        final File file = new File(tmp);
        return file;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureExistsFails() {
        final String tmp = System.getProperties().getProperty("java.io.tmpdir") + "-invalid";
        final File file = new File(tmp);
        ensureExists(file);
    }

    @Test
    public void testEnsureIsDirectory() {
        final File file = getTempDir();
        ensureIsDirectory(file);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsDirectoryFails() {
        final String tmp = System.getProperties().getProperty("java.io.tmpdir") + "-invalid";
        final File file = new File(tmp);
        ensureIsDirectory(file);
    }

    @Test
    public void testEnsureIsFalse() {
        ensureIsFalse(false, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsFalseFails() {
        ensureIsFalse(true, "");
    }

    @Test
    public void testEnsureIsGreaterOrEqualTo1() {
        ensureIsGreaterOrEqualTo(0, 0, "");
    }

    @Test
    public void testEnsureIsGreaterOrEqualTo2() {
        ensureIsGreaterOrEqualTo(0, -0.01, "");
    }

    @Test
    public void testEnsureIsInRange_Pass() {
        ensureIsInRange(1.0, 0, 2, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsInRange_Fails_TooLow() {
        ensureIsInRange(0, 1, 2, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsInRange_Fails_TooHigh() {
        ensureIsInRange(3, 1, 2, "");
    }

    @Test
    public void testEnsureIsInstance() {
        ensureIsInstanceOf("", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsInstance_GotSupertype() {
        ensureIsInstanceOf(new Object(), String.class);
    }

    @Test
    public void testEnsureIsInstance_GotSubtype() {
        ensureIsInstanceOf(Lists.newArrayList(), List.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNotEmpty_WithEmpty() {
        ensureIsNotEmpty("", "");
    }

    @Test
    public void testEnsureIsNotEmpty_WithOneElement() {
        ensureIsNotEmpty(Sets.newHashSet(""), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsEmpty_EmptyCollection() {
        ensureIsNotEmpty(Collections.emptyList(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsEmpty_NullCollection() {
        ensureIsNotEmpty((Collection<?>) null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNotEmpty_WithNull() {
        ensureIsNotEmpty((String) null, "");
    }

    @Test
    public void testEnsureIsNotEmpty_Pass() {
        ensureIsNotEmpty(" ", "");
    }

    @Test
    public void testEnsureIsNotInstanceof_Pass() {
        ensureIsNotInstanceOf(new Object(), List.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNotInstanceof_Fails() {
        ensureIsNotInstanceOf(new ArrayList<Object>(), List.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNotNullObject_WithNull() {
        ensureIsNotNull(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsFile_WithDir() {
        ensureIsFile(SystemUtils.getJavaIoTmpDir());
    }

    @Test
    public void testEnsureIsNull_WithNull() {
        ensureIsNull(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNull_WithNonNull() {
        ensureIsNull("");
    }

    @Test
    public void testEnsureIsFile_WithFile() throws IOException {
        final File file = File.createTempFile("test", ".test");
        file.deleteOnExit();
        ensureIsFile(file);
    }

    @Test
    public void testEnsureIsNotNullObject_NotNullArg() {
        ensureIsNotNull("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsNotZero_WithZero() {
        ensureIsNotZero(0);
    }

    @Test
    public void testEnsureIsNotZero_WithOne() {
        ensureIsNotZero(1);
    }

    @Test
    public void testEnsureIsTrueBoolean() {
        ensureIsTrue(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsTrueBoolean_WithFalse() {
        ensureIsTrue(false);
    }

    @Test
    public void testEnsureIsProbablity_Pass() {
        Checks.ensureIsProbability(0.0);
        Checks.ensureIsProbability(0.99);
        Checks.ensureIsProbability(1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsProbablity_Fail_01() {
        Checks.ensureIsProbability(-0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsProbablity_Fail_02() {
        Checks.ensureIsProbability(1.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureIsProbablity_Fail_03() {
        Checks.ensureIsProbability(100);
    }

}

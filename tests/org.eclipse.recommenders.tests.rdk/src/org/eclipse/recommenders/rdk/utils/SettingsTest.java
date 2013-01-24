package org.eclipse.recommenders.rdk.utils;

import static org.eclipse.recommenders.rdk.utils.Settings.getBool;
import static org.eclipse.recommenders.rdk.utils.Settings.getInt;
import static org.eclipse.recommenders.rdk.utils.Settings.getString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SettingsTest {

    private static final String UNDEFINED = "cr.test.undefined";
    private static final String DEFINED = "cr.test.defined";

    @Test
    public void testBool() {
        assertFalse(getBool(UNDEFINED).isPresent());

        System.setProperty(DEFINED, "true");
        assertTrue(getBool(DEFINED).get());

        System.setProperty(DEFINED, "false");
        assertFalse(getBool(DEFINED).get());
    }

    @Test(expected = RuntimeException.class)
    public void testBoolInvalidValueYes() {
        System.setProperty(DEFINED, "yes");
        getBool(DEFINED);
    }

    @Test(expected = RuntimeException.class)
    public void testBoolInvalidValueNo() {
        System.setProperty(DEFINED, "no");
        getBool(DEFINED);
    }

    @Test(expected = RuntimeException.class)
    public void testBoolInvalidValueOther() {
        System.setProperty(DEFINED, "other");
        getBool(DEFINED);
    }

    @Test
    public void testString() {
        assertFalse(getString(UNDEFINED).isPresent());
        System.setProperty(DEFINED, "true");
        assertTrue(getString(DEFINED).isPresent());
    }

    @Test
    public void testInt() {
        assertFalse(Settings.getInt(UNDEFINED).isPresent());
        System.setProperty(DEFINED, "23");
        assertTrue(getInt(DEFINED).isPresent());
    }

    @Test(expected = RuntimeException.class)
    public void testIntInvalidValue() {
        System.setProperty(DEFINED, "true");
        getInt(DEFINED).isPresent();
    }

    @Test
    public void testFile() {
        assertFalse(Settings.getFile(UNDEFINED).isPresent());
        System.setProperty(DEFINED, "c:/test");
        assertTrue(Settings.getFile(DEFINED).isPresent());
    }

}

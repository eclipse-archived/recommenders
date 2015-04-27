package org.eclipse.recommenders.models;

import static org.eclipse.recommenders.coordinates.Coordinates.isValidId;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class CoordinatesTest {

    @Test
    public void testInvalidIds() {
        assertFalse(isValidId(null));
        assertFalse(isValidId(""));
    }
}

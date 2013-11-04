package org.eclipse.recommenders.tests.jayes.io.utils;

import static org.hamcrest.number.IsCloseTo.closeTo;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class IsCloseTo extends TypeSafeMatcher<double[]> {

    private double[] array;
    private double tolerance;

    private IsCloseTo(double[] array, double tolerance) {
        this.array = array;
        this.tolerance = tolerance;

    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(array);
    }

    @Override
    protected boolean matchesSafely(double[] item) {
        if (array == item) {
            return true;
        }
        if (array == null || item == null) {
            return false;
        }
        if (item.length != array.length) {
            return false;
        }
        for (int i = 0; i < item.length; i++) {
            if (!closeTo(item[i], tolerance).matches(array[i])) {
                return false;
            }
        }
        return true;
    }

    public static IsCloseTo isCloseTo(final double[] array, double tolerance) {
        return new IsCloseTo(array, tolerance);
    }

}

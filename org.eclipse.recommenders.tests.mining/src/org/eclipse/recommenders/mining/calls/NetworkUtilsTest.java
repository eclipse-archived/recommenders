package org.eclipse.recommenders.mining.calls;

import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_MAX;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_MIN;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.ensureAllProbabilitiesInValidRange;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.recommenders.mining.calls.generation.NetworkUtils;
import org.junit.Test;

public class NetworkUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testRangeChecks_ZeroValue() throws Exception {
        final double[] actual = make(0);
        ensureAllProbabilitiesInValidRange(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeChecks_ZeroLength() throws Exception {
        final double[] actual = make();
        NetworkUtils.ensureAllProbabilitiesInValidRange(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeChecks_OneValue() throws Exception {
        final double[] actual = make(1.0);
        NetworkUtils.ensureAllProbabilitiesInValidRange(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRangeChecks_OneValueAtLastPosition() throws Exception {
        final double[] actual = make(0.5, 1.0);
        NetworkUtils.ensureAllProbabilitiesInValidRange(actual);
    }

    @Test
    public void testRangeChecks_ValidMinactual() throws Exception {
        final double[] actual = make(P_MIN, P_MIN, P_MIN);
        NetworkUtils.ensureAllProbabilitiesInValidRange(actual);
    }

    @Test
    public void testRangeChecks_ValidMaxactual() throws Exception {
        final double[] actual = make(P_MAX, P_MAX, P_MAX);
        NetworkUtils.ensureAllProbabilitiesInValidRange(actual);
    }

    @Test
    public void testRangeChecks_Validactual() throws Exception {
        final double[] actual = make(0.4, 0.9, 0.2322);
        NetworkUtils.ensureAllProbabilitiesInValidRange(actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScaleactual_SingleMaxValue() {
        final double[] actual = make(P_MAX);
        NetworkUtils.scaleMaximalValue(actual);
    }

    @Test
    public void testScale_Min_Max() {
        final double[] actual = make(P_MAX, P_MIN);
        final double[] expected = make(P_MAX, P_MIN);
        NetworkUtils.scaleMaximalValue(actual);
        assertArrayEquals(expected, actual, P_MIN);
    }

    @Test
    public void testScale_ExactOne() {
        final double[] actual = make(P_MAX, P_MIN);
        final double[] expected = make(P_MAX, P_MIN);
        NetworkUtils.scaleMaximalValue(actual);
        assertArrayEquals(expected, actual, P_MIN);
    }

    @Test
    public void testScale_BitTooHigh() {
        final double[] actual = make(P_MAX, P_MIN, P_MIN);
        final double[] expected = make(P_MAX - P_MIN, P_MIN, P_MIN);
        NetworkUtils.scaleMaximalValue(actual);
        assertArrayEquals(expected, actual, P_MIN);
    }

    @Test
    public void testSafeDiv() {
        final double resZero = NetworkUtils.safeDivMaxMin(1, 0);
        assertEquals(P_MIN, resZero, 0.0001);
        final double resOne = NetworkUtils.safeDivMaxMin(1, 1);
        assertEquals(P_MAX, resOne, 0.0001);
        final double resUnderMin = NetworkUtils.safeDivMaxMin(1, 1000000);
        assertEquals(P_MIN, resUnderMin, 0.0001);
    }

    @Test
    public void testScale_BitTooLow() {
        final double[] actual = make(P_MAX - P_MIN, P_MIN);
        final double[] expected = make(P_MAX - P_MIN, 2 * P_MIN);
        NetworkUtils.scaleMaximalValue(actual);
        assertArrayEquals(expected, actual, P_MIN);
    }

    private double[] make(final double... values) {
        return values;
    }
}

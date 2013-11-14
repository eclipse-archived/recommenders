package org.eclipse.recommenders.tests.jayes;

import org.eclipse.recommenders.jayes.BayesNet;
import org.junit.Test;

public class BayesNetTest {

    @Test(expected = NullPointerException.class)
    public void testNamesMayNotBeNull() {
        BayesNet net = new BayesNet();
        net.createNode(null);
    }

}

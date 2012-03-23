package org.eclipse.recommenders.tests.completion.rcp.calls;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.BayesNetWrapper;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Stopwatch;

public class ModelLoadingTest {

    private static BayesNetWrapper sut;

    @BeforeClass
    public static void beforeClass() throws IOException, ClassNotFoundException {
        Stopwatch w = new Stopwatch();
        w.start();
        String pkg = ModelLoadingTest.class.getPackage().getName().replace('.', '/') + "/Text.data.gz_";
        InputStream s = ModelLoadingTest.class.getClassLoader().getResourceAsStream(pkg);
        InputStream is = new GZIPInputStream(s);
        ObjectInputStream ois = new ObjectInputStream(is);
        BayesianNetwork net = (BayesianNetwork) ois.readObject();
        sut = new BayesNetWrapper(VmTypeName.BYTE, net);
        w.stop();
        System.out.println("loading model took: " + w);
    }

    @Test
    public void testSmoke() throws IOException, ClassNotFoundException {
        sut.clearEvidence();
        sut.getActiveCalls();
        sut.getActiveContext();
        sut.getActiveDefinition();
        sut.getActiveKind();
        sut.getContexts();
        sut.getDefinitions();
        sut.getMethodCalls();
        sut.getPatternsWithProbability();
        sut.getRecommendedMethodCalls(0.1d);
    }
}

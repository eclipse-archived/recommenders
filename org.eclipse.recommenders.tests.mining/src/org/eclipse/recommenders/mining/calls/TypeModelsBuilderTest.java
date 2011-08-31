package org.eclipse.recommenders.mining.calls;

import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.eclipse.recommenders.commons.utils.Tuple.newTuple;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.CTX_DUMMY;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.P_MIN;
import static org.eclipse.recommenders.mining.calls.generation.NetworkUtils.createNewDefinitionState;
import static org.eclipse.recommenders.mining.calls.generation.ReceiverCallGroupsContainer.newGroup;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.recommenders.commons.bayesnet.Node;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.mining.calls.generation.ReceiverCallGroupsContainer;
import org.eclipse.recommenders.mining.calls.generation.TypeModelsWithContextBuilder;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("unchecked")
public class TypeModelsBuilderTest {

    private static final Tuple[] EMPTY = new Tuple[0];
    IMethodName call1 = VmMethodName.get("LM.m1()V");
    IMethodName call2 = VmMethodName.get("LM.m2()V");
    IMethodName call3 = VmMethodName.get("LM.m3()V");
    IMethodName call4 = VmMethodName.get("LM.m4()V");

    IMethodName ctx1 = VmMethodName.get("LM.c1()V");
    IMethodName ctx2 = VmMethodName.get("LM.c2()V");
    IMethodName ctx3 = VmMethodName.get("LM.c3()V");
    IMethodName ctx4 = VmMethodName.get("LM.c4()V");

    IMethodName def1 = VmMethodName.get("LM.def1()V");
    IMethodName def2 = VmMethodName.get("LM.def2()V");
    // TODO
    IMethodName def3 = VmMethodName.get("LM.def3()V");
    IMethodName def4 = VmMethodName.get("LM.def4()V");

    Set<IMethodName> s1 = Sets.newHashSet(call1, call2);
    Set<IMethodName> s2 = Sets.newHashSet(call3, call4);
    Set<IMethodName> s3 = Sets.newHashSet(call1, call3);

    ReceiverCallGroupsContainer grp1 = newGroup(s1, toArray(newTuple(ctx1, 3), newTuple(ctx2, 1)),
            toArray(newTuple(createNewDefinitionState(def1), 4)));

    ReceiverCallGroupsContainer grp2 = newGroup(s2, toArray(newTuple(ctx1, 2), newTuple(ctx3, 2)),
            toArray(newTuple(createNewDefinitionState(def2), 3)));

    ReceiverCallGroupsContainer grp3 = newGroup(s3, toArray(newTuple(ctx4, 4)),
            toArray(newTuple(createNewDefinitionState(def3), 2), newTuple(createNewDefinitionState(def4), 2)));

    List<ReceiverCallGroupsContainer> dataset = Lists.newArrayList(grp1, grp2, grp3);

    @Test
    public void testBuildContextNode() {
        //
        // setup:
        final TypeModelsWithContextBuilder sut = new TypeModelsWithContextBuilder(null, dataset);
        //
        // exercise:
        final Node node = sut.buildCallingContextNode();
        //
        // verify:
        final String[] expectedStates = ArrayUtils.toArray(CTX_DUMMY.getIdentifier(), ctx1.getIdentifier(),
                ctx2.getIdentifier(), ctx3.getIdentifier(), ctx4.getIdentifier());
        final String[] actualStates = node.getStates();
        assertArrayEquals(expectedStates, actualStates);

        // calling contexts computed as expected?
        // TODO MARCEL: discuss is MIN for null context appropriate?
        final double[] actualProbs = node.getProbabilities();
        final double[] expectedProbs = new double[] { 1 - 4 * P_MIN, P_MIN, P_MIN, P_MIN, P_MIN, };
        assertArrayEquals(expectedProbs, actualProbs, 0.001);
    }

    @Test
    public void testBuildCallGroupsNode() {
        //
        // setup:
        final TypeModelsWithContextBuilder sut = new TypeModelsWithContextBuilder(null, dataset);
        //
        // exercise:
        final Node callingContextNode = sut.buildCallingContextNode();
        final Node callgroupsNode = sut.buildCallGroupsNode();
        //
        // verify:
        final String[] expectedStates = ArrayUtils.toArray("group 0", "group 1", "group 2", "group 3");
        final String[] actualStates = callgroupsNode.getStates();
        assertArrayEquals(expectedStates, actualStates);

        // calling contexts computed as expected?
        final double[] actualProbs = callgroupsNode.getProbabilities();
        //
        // state order: DUMMY_CTX, c1 c2 c3 c4
        final double[] expectedProbs = new double[] {
                // dummy
                div(1, 1), 0, 0, 0,
                // c1
                0, div(3, 5), div(2, 5), 0,
                // c2
                0, div(1, 1), 0, 0,
                // c3:
                0, 0, div(2, 2), 0,
                // c4:
                0, 0, 0, div(4, 4), };
        final int expectedNumberOfProbs = callgroupsNode.getStates().length * callingContextNode.getStates().length;
        assertEquals(expectedNumberOfProbs, actualProbs.length);
        assertArrayEquals(expectedProbs, actualProbs, 0.001);
    }

    @Test
    public void testCallNodes() {
        //
        // setup:
        final TypeModelsWithContextBuilder sut = new TypeModelsWithContextBuilder(null, dataset);
        //
        // exercise:
        final Node callingContextNode = sut.buildCallingContextNode();
        final Node callgroupsNode = sut.buildCallGroupsNode();
        final List<Node> methodCallNodes = sut.buildMethodCallNodes();
        //
        // verify:
        final String[] expectedStates = toArray("True", "False");
        for (final Node n : methodCallNodes) {
            final String[] actualStates = n.getStates();
            assertArrayEquals(expectedStates, actualStates);
        }

        // check each node's probabilities

    }

    /**
     * Returns "a/b";
     */
    private static double div(final int a, final int b) {
        return a / (double) b;
    }
}

/*******************************************************************************
 * Copyright (c) 2013 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Michael Kutschke - initial API and implementation
 ******************************************************************************/
package org.eclipse.recommenders.calls;

import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.NEW;
import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.RETURN;
import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.STRING_LITERAL;
import static org.eclipse.recommenders.utils.Constants.N_NODEID_CALL_GROUPS;
import static org.eclipse.recommenders.utils.Constants.N_NODEID_CONTEXT;
import static org.eclipse.recommenders.utils.Constants.N_NODEID_DEF;
import static org.eclipse.recommenders.utils.Constants.N_NODEID_DEF_KIND;
import static org.eclipse.recommenders.utils.Constants.N_STATE_FALSE;
import static org.eclipse.recommenders.utils.Constants.N_STATE_TRUE;
import static org.eclipse.recommenders.utils.names.VmMethodName.NULL;
import static org.eclipse.recommenders.utils.names.VmTypeName.STRING;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Recommendations;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class JayesCallModelTest {

    private static final IMethodName STRING_VALUE_OF = VmMethodName.get("Ljava/lang/String.valueOf(Ljava/lang/String;)Ljava/lang/String;");
    private static final IMethodName STRING_TO_STRING = VmMethodName.get("Ljava/lang/String.toString()Ljava/lang/String;");

    @Test
    public void testPrediction() {
        BayesNet net = new BayesNet();
        BayesNode callgroupNode = net.createNode(N_NODEID_CALL_GROUPS);
        callgroupNode.addOutcomes("group1", "group2");
        callgroupNode.setProbabilities(0.5, 0.5);

        BayesNode defkindNode = net.createNode(N_NODEID_DEF_KIND);
        defkindNode.addOutcomes(NEW.name(), RETURN.name());
        defkindNode.setParents(Arrays.asList(callgroupNode));
        defkindNode.setProbabilities(1.0, 0.0, 0.0, 1.0);

        BayesNode contextNode = net.createNode(N_NODEID_CONTEXT);
        contextNode.addOutcomes(NULL.toString(), STRING_VALUE_OF.toString());
        contextNode.setParents(Arrays.asList(callgroupNode));
        contextNode.setProbabilities(0.5, 0.5, 0.5, 0.5);

        BayesNode defNode = net.createNode(N_NODEID_DEF);
        defNode.setParents(Arrays.asList(callgroupNode));
        defNode.addOutcomes(NULL.toString(), STRING_VALUE_OF.toString());
        defNode.setProbabilities(0.5, 0.5, 0.5, 0.5);

        BayesNode valueOfNode = net.createNode(STRING_VALUE_OF.toString());
        valueOfNode.addOutcomes(N_STATE_TRUE, N_STATE_FALSE);
        valueOfNode.setParents(Arrays.asList(callgroupNode));
        valueOfNode.setProbabilities(0.9, 0.1, 0.1, 0.9);

        BayesNode toStringNode = net.createNode(STRING_TO_STRING.toString());
        toStringNode.addOutcomes(N_STATE_TRUE, N_STATE_FALSE);
        toStringNode.setParents(Arrays.asList(callgroupNode));
        toStringNode.setProbabilities(0.1, 0.9, 0.9, 0.1);

        JayesCallModel model = new JayesCallModel(STRING, net);
        model.setObservedCalls(ImmutableSet.of(STRING_VALUE_OF));

        assertThat(getTopPatterns(model).get(0).getProposal(), is("group1"));
        assertThat(getTopPatterns(model).get(1).getProposal(), is("group2"));

        assertThat(Recommendations.top(model.recommendCalls(), 1).get(0).getProposal(),
                is(STRING_TO_STRING));

        model.reset();
        model.setObservedCalls(ImmutableSet.of(STRING_TO_STRING));
        model.setObservedDefinitionKind(STRING_LITERAL);

        assertThat(getTopPatterns(model).get(0).getProposal(), is("group2"));
        assertThat(getTopPatterns(model).get(1).getProposal(), is("group1"));

        assertThat(Recommendations.top(model.recommendCalls(), 1).get(0).getProposal(),
                is(STRING_VALUE_OF));
    }

    private List<Recommendation<String>> getTopPatterns(JayesCallModel model) {
        return Recommendations.top(model.recommendPatterns(), 2);
    }
}

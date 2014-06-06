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
package org.eclipse.recommenders.jayes.io;

import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class Equality {

    public static Matcher<BayesNet> equalTo(final BayesNet net1) {
        return new TypeSafeDiagnosingMatcher<BayesNet>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(net1);
            }

            @Override
            protected boolean matchesSafely(BayesNet net2, Description mismatchDescription) {
                if (!net1.getName().equals(net2.getName())) {
                    mismatchDescription.appendText("net had name: " + net2.getName());
                    return false;
                }
                if (net1.getNodes().size() != net2.getNodes().size()) {
                    mismatchDescription.appendText("nodes: " + net2.getNodes().size());
                    return false;
                }
                for (int i = 0; i < net1.getNodes().size(); i++) {
                    if (!Equality.equalTo(net1.getNode(i)).matches(net2.getNode(i))) {
                        Equality.equalTo(net1.getNode(i)).describeMismatch(net2.getNode(i), mismatchDescription);
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static Matcher<BayesNode> equalTo(final BayesNode node1) {
        return new TypeSafeDiagnosingMatcher<BayesNode>() {

            @Override
            public void describeTo(Description description) {
                description.appendText(node1.getName());
                description.appendValue(node1.getProbabilities());

            }

            @Override
            protected boolean matchesSafely(BayesNode node2, Description mismatchDescription) {
                if (!node1.getName().equals(node2.getName())) {
                    mismatchDescription.appendText(node2.getName());
                    return false;
                }

                if (!node1.getOutcomes().equals(node2.getOutcomes())) {
                    return false;
                }

                if (node1.getParents().size() != node2.getParents().size()) {
                    return false;
                }

                if (!IsCloseTo.isCloseTo(node1.getProbabilities(), 1e-9).matches(node2.getProbabilities())) {
                    mismatchDescription.appendValue(node2.getProbabilities());
                    return false;
                }
                return true;
            }
        };
    }
}

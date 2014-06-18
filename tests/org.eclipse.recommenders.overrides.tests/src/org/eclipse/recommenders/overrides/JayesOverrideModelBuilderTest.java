/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.overrides;

import static org.eclipse.recommenders.testing.RecommendationMatchers.recommendation;
import static org.eclipse.recommenders.utils.names.VmTypeName.OBJECT;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class JayesOverrideModelBuilderTest {

    private static final IMethodName OBJECT_HASHCODE = VmMethodName.get("Ljava/lang/Object.hashCode()I");
    private static final IMethodName OBJECT_WAIT = VmMethodName.get("Ljava/lang/Object.wait()V");

    @Test(expected = IllegalArgumentException.class)
    public void testNoObservation() {
        @SuppressWarnings("unused")
        JayesOverrideModelBuilder sut = new JayesOverrideModelBuilder(OBJECT,
                Collections.<OverrideObservation>emptyList());
    }

    @Test
    public void testOneObservation() {
        JayesOverrideModelBuilder sut = new JayesOverrideModelBuilder(OBJECT, ImmutableList.of(observation(1,
                OBJECT_HASHCODE)));

        IOverrideModel model = sut.build();

        assertThat(model.getKnownMethods().size(), is(1));
        assertThat(model.getKnownMethods(), hasItem(OBJECT_HASHCODE));

        assertThat(model.getKnownPatterns().size(), is(2));

        assertThat(model.recommendOverrides().size(), is(1));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_HASHCODE, 1.0)));
    }

    @Test
    public void testTwoNonOverlappingObservations() {
        JayesOverrideModelBuilder sut = new JayesOverrideModelBuilder(OBJECT, ImmutableList.of(
                observation(2, OBJECT_HASHCODE), observation(1, OBJECT_WAIT)));

        IOverrideModel model = sut.build();

        assertThat(model.getKnownMethods().size(), is(2));
        assertThat(model.getKnownMethods(), hasItem(OBJECT_HASHCODE));
        assertThat(model.getKnownMethods(), hasItem(OBJECT_WAIT));

        assertThat(model.getKnownPatterns().size(), is(3));

        assertThat(model.recommendOverrides().size(), is(2));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_HASHCODE, 0.66)));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_WAIT, 0.33)));

        model.setObservedMethod(OBJECT_WAIT);

        assertThat(model.recommendOverrides().size(), is(1));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_HASHCODE, 0.0)));
    }

    @Test
    public void testTwoOverlappingObservations() {
        JayesOverrideModelBuilder sut = new JayesOverrideModelBuilder(OBJECT, ImmutableList.of(
                observation(2, OBJECT_HASHCODE), observation(1, OBJECT_HASHCODE, OBJECT_WAIT)));

        IOverrideModel model = sut.build();

        assertThat(model.getKnownMethods().size(), is(2));
        assertThat(model.getKnownMethods(), hasItem(OBJECT_HASHCODE));
        assertThat(model.getKnownMethods(), hasItem(OBJECT_WAIT));

        assertThat(model.getKnownPatterns().size(), is(3));

        assertThat(model.recommendOverrides().size(), is(2));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_HASHCODE, 1.0)));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_WAIT, 0.33)));

        model.setObservedMethod(OBJECT_WAIT);

        assertThat(model.recommendOverrides().size(), is(1));
        assertThat(model.recommendOverrides(), hasItem(recommendation(OBJECT_HASHCODE, 1.0)));
    }

    private OverrideObservation observation(int frequency, IMethodName... overriddenMethods) {
        OverrideObservation observation = new OverrideObservation();
        observation.frequency = frequency;
        observation.overriddenMethods.addAll(Arrays.asList(overriddenMethods));
        return observation;
    }
}

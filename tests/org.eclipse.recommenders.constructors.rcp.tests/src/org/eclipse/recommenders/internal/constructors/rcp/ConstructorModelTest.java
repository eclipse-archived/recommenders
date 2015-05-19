/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import static org.eclipse.recommenders.utils.names.VmTypeName.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.*;

import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@RunWith(Parameterized.class)
public class ConstructorModelTest {

    private static final IMethodName OBJECT_HASHCODE = VmMethodName.get("Ljava/lang/Object.hashCode()I");
    private static final IMethodName OBJECT_TO_STRING = VmMethodName
            .get("Ljava/lang/Object.toString()Ljava/lang/String;");

    private final ConstructorModel first;
    private final ConstructorModel second;
    private final boolean equality;

    public ConstructorModelTest(ConstructorModel first, ConstructorModel second, boolean equality) {
        this.first = first;
        this.second = second;
        this.equality = equality;
    }

    @Parameters
    public static Iterable<Object[]> scenarios() {
        LinkedList<Object[]> scenarios = Lists.newLinkedList();

        ConstructorModel emptyObjectModel = new ConstructorModel(OBJECT, Collections.<IMethodName, Integer>emptyMap());
        ConstructorModel anotherEmptyObjectModel = new ConstructorModel(OBJECT,
                Collections.<IMethodName, Integer>emptyMap());
        ConstructorModel emptyStringModel = new ConstructorModel(STRING, Collections.<IMethodName, Integer>emptyMap());
        ConstructorModel oneHashCodeModel = new ConstructorModel(OBJECT, ImmutableMap.<IMethodName, Integer>of(
                OBJECT_HASHCODE, 1));
        ConstructorModel anotherOneHashCodeModel = new ConstructorModel(OBJECT, ImmutableMap.<IMethodName, Integer>of(
                OBJECT_HASHCODE, 1));
        ConstructorModel twoHashCodeModel = new ConstructorModel(OBJECT, ImmutableMap.<IMethodName, Integer>of(
                OBJECT_HASHCODE, 2));
        ConstructorModel oneToStringModel = new ConstructorModel(OBJECT, ImmutableMap.<IMethodName, Integer>of(
                OBJECT_TO_STRING, 1));

        scenarios.add(scenario(emptyObjectModel, null, false));

        ConstructorModel sameEmptyObjectModel = emptyObjectModel;
        scenarios.add(scenario(emptyObjectModel, sameEmptyObjectModel, true));

        scenarios.add(scenario(emptyObjectModel, anotherEmptyObjectModel, true));

        scenarios.add(scenario(emptyObjectModel, emptyStringModel, false));

        scenarios.add(scenario(oneHashCodeModel, anotherOneHashCodeModel, true));

        scenarios.add(scenario(oneHashCodeModel, twoHashCodeModel, false));

        scenarios.add(scenario(oneHashCodeModel, oneToStringModel, false));

        return scenarios;
    }

    private static <T> Object[] scenario(ConstructorModel first, ConstructorModel second, boolean equality) {
        return new Object[] { first, second, equality };
    }

    @Test
    public void testEquals() {
        assertThat(first.equals(second), is(equalTo(equality)));
    }

    @Test
    public void testEqualsIsSymmetric() {
        assumeNotNull(second);

        assertThat(first.equals(second), is(equalTo(second.equals(first))));
    }

    @Test
    public void testHashCodeIsConsistent() {
        assumeNotNull(second);
        assumeThat(first.equals(second), is(equalTo(true)));

        assertThat(first.hashCode(), is(equalTo(second.hashCode())));
    }
}

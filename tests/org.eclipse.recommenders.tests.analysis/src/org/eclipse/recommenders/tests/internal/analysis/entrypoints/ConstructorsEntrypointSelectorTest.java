/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.internal.analysis.entrypoints;

import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPrivateMethodMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createProtectedConstructorMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createProtectedStaticMethodMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPublicClinitMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPublicFinalMethodMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPublicNativeMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.createPublicStaticMethodMock;
import static org.eclipse.recommenders.tests.analysis.WalaMockUtils.mockClassGetDeclareMethods;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.internal.analysis.entrypoints.ConstructorEntrypointSelector;
import org.eclipse.recommenders.internal.analysis.entrypoints.IEntrypointSelector;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

public class ConstructorsEntrypointSelectorTest {

    IEntrypointSelector sut = new ConstructorEntrypointSelector();

    @Test
    public void testSelectEntryPoints() {
        // setup
        IClass clazz = createClassMock();
        Set<IMethod> expecteds = Sets.newHashSet(createProtectedConstructorMock(), createProtectedConstructorMock());
        List<IMethod> declaredMethods = Lists.newArrayList(createPublicClinitMock(), createPrivateMethodMock(),
                createProtectedStaticMethodMock(), createPublicClinitMock(), createPublicFinalMethodMock(),
                createPublicNativeMock(), createPublicStaticMethodMock());
        declaredMethods.addAll(expecteds);
        mockClassGetDeclareMethods(clazz, declaredMethods);
        // exercise
        List<Entrypoint> selectedMethods = sut.selectEntrypoints(clazz);
        // verify
        assertEquals(expecteds.size(), selectedMethods.size());
        for (Entrypoint entrypoint : selectedMethods) {
            IMethod actual = entrypoint.getMethod();
            assertTrue(expecteds.contains(actual));
        }
    }
}

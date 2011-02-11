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
package org.eclipse.recommenders.internal.commons.analysis.entrypoints;

import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPrivateMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createProtectedMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicClinitMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicConstructorMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicFinalMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockClassGetDeclareMethods;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

public class PublicMethodsEntrypointSelectorTest {

  PublicMethodsEntrypointSelector sut = new PublicMethodsEntrypointSelector();

  @Test
  public void testSelectEntryPoints() {
    // setup
    final IClass clazz = createClassMock();
    final IMethod expected = createPublicFinalMethodMock();
    final List<IMethod> declaredMethods = Lists.newArrayList(createPublicClinitMock(), createPrivateMethodMock(),
        createProtectedMethodMock(), createPublicConstructorMock(), expected);
    mockClassGetDeclareMethods(clazz, declaredMethods);
    // exercise
    final List<Entrypoint> selectedMethods = sut.selectEntrypoints(clazz);
    // verify
    assertEquals(1, selectedMethods.size());
    assertEquals(expected, Iterables.getFirst(selectedMethods, null).getMethod());
  }
}

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
package org.eclipse.recommenders.internal.commons.analysis;

import static org.junit.Assert.fail;

import org.eclipse.recommenders.tests.commons.analysis.utils.TestdataClassHierarchyFixture;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

public class TestdataClassHierarchyFixtureTest {

    @Test
    public void testFixture() {
        final IClassHierarchy cha = TestdataClassHierarchyFixture.getInstance();
        for (final IClass clazz : cha) {
            final ClassLoaderReference clRef = clazz.getClassLoader().getReference();
            if (clRef.equals(ClassLoaderReference.Application)) {
                return;
            }
        }
        fail("no classes loaded with application class loader. Classpath is not working properly");
    }
}

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
package org.eclipse.recommenders.tests.internal.analysis;

import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicClass;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createSomeApplicationTypeReference;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createSomePrimordialTypeReference;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createStaticClassMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassGetName;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassGetSuperclass;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassIsApplication;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassIsPrimordial;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.recommenders.internal.analysis.utils.ClassUtils;
import org.junit.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeReference;

public class ClassUtilsTest {

    @Test
    public void testGetAllSuperclasses() {
        // setup
        final IClass baseclass = createClassMock();
        final IClass superclass = createClassMock();
        final IClass superSuperclass = createClassMock();
        mockClassGetSuperclass(baseclass, superclass);
        mockClassGetSuperclass(superclass, superSuperclass);
        final List<IClass> expecteds = Arrays.asList(superclass, superSuperclass);
        // exercise
        final List<IClass> actuals = ClassUtils.getAllSuperclasses(baseclass);
        // verify
        assertEquals(expecteds, actuals);
    }

    @Test
    public void testInSamePackage() {
        // setup
        final IClass c1 = createClassMock();
        final IClass c2 = createClassMock();
        final IClass c3 = createClassMock();
        mockClassGetName(c1, "Lpackage/C1");
        mockClassGetName(c2, "Lpackage/C2");
        mockClassGetName(c3, "Lother/package/C3");
        // exercise & verify
        assertTrue(ClassUtils.inSamePackage(c1, c2));
        assertFalse(ClassUtils.inSamePackage(c1, c3));
    }

    // @Test
    // public void testFindClassClassOfQIClassHierarchy() {
    // IClassHierarchy cha = JREOnlyClassHierarchyFixture.getInstance();
    // IClass actual = ClassUtils.findClass(String.class, cha);
    // assertNotNull(actual);
    // }
    //
    // @Test
    // public void testFindClassStringIClassHierarchy() {
    // IClassHierarchy cha = JREOnlyClassHierarchyFixture.getInstance();
    // IClass actual = ClassUtils.findClass("Ljava/lang/String", cha);
    // assertNotNull(actual);
    // }

    @Test
    public void testIsNestedClass_WithDefaultPackage() {
        final IClass container = createClassMock("name1");
        final IClass nested = createClassMock("name1$2");
        final IClass other = createClassMock("name3");
        assertTrue(ClassUtils.isNestedClass(nested, container));
        assertFalse(ClassUtils.isNestedClass(nested, nested));
        assertFalse(ClassUtils.isNestedClass(nested, other));
    }

    @Test
    public void testIsNestedClass_HappyPath() {
        final IClass container = createClassMock("Lmy/package/Name1");
        final IClass nested = createClassMock("Lmy/package/Name1$2");
        final IClass other = createClassMock("Lmy/other/package/Name1");
        assertTrue(ClassUtils.isNestedClass(nested, container));
        assertFalse(ClassUtils.isNestedClass(nested, nested));
        assertFalse(ClassUtils.isNestedClass(nested, other));
    }

    @Test
    public void testIsPrimordialIClass() {
        // setup
        final IClass c1 = createClassMock();
        final IClass c2 = createClassMock();
        mockClassIsPrimordial(c1);
        mockClassIsApplication(c2);
        // exercise & verify
        assertTrue(ClassUtils.isPrimordial(c1));
        assertFalse(ClassUtils.isPrimordial(c2));
    }

    @Test
    public void testIsPrimordialTypeReference() {
        // setup
        final TypeReference r1 = createSomePrimordialTypeReference();
        final TypeReference r2 = createSomeApplicationTypeReference();
        // exercise & verify
        assertTrue(ClassUtils.isPrimordial(r1));
        assertFalse(ClassUtils.isPrimordial(r2));
    }

    @Test
    public void testIsStatic() {
        final IClass c1 = createStaticClassMock();
        assertTrue(ClassUtils.isStatic(c1));
        final IClass c2 = createPublicClass();
        assertFalse(ClassUtils.isStatic(c2));
    }
}

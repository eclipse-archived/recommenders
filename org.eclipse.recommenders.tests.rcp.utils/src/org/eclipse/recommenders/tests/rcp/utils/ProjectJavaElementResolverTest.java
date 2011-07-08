/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.rcp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.rcp.utils.ProjectJavaElementResolver;
import org.eclipse.recommenders.tests.commons.ui.utils.FixtureUtil;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.AnonymousListenerDeclaredAsField;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.AnonymousListenerDeclaredAsStaticField;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.AnonymousListenerDeclaredInMethod;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.AnonymousListenerDeclaredInMethodBody;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.AnonymousListenerDeclaredInStaticMethodBody;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.ComparatorWithGenerics;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.InnerClassContainer;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.InnerClassContainer.InnerClass2;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.InnerClassContainer.InnerClass2.InnerClass3;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.Methods;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.SimpleClass;
import org.eclipse.recommenders.tests.fixtures.rcp.utils.StaticInnerClassContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProjectJavaElementResolverTest {

    private static final String fixtureProjectName = "org.eclipse.recommenders.tests.fixtures.rcp.utils";
    private static IJavaProject project;
    private ProjectJavaElementResolver sut;

    @BeforeClass
    public static void beforeClass() throws IOException, CoreException {
        final IProject simpleProject = FixtureUtil.copyProjectToWorkspace(fixtureProjectName);
        project = JavaCore.create(simpleProject);
    }

    @Before
    public void beforeTest() {
        createFreshSut();
    }

    public void createFreshSut() {
        sut = new ProjectJavaElementResolver(project);
    }

    @Test
    public void testSimpleClass() {
        find(SimpleClass.class);
    }

    @Test
    public void testStaticInnerClass() {
        find(StaticInnerClassContainer.InnerClass2.class);
        find(StaticInnerClassContainer.InnerClass2.InnerClass3.class);
        find(StaticInnerClassContainer.InnerClass3.class);
        find(StaticInnerClassContainer.InnerClass3.InnerClass4.class);
        find(StaticInnerClassContainer.InnerClass4.class);
    }

    @Test
    public void testInnerClass() {
        final InnerClassContainer base = new InnerClassContainer();
        final InnerClass2 inner2 = base.new InnerClass2();
        final InnerClass3 inner3 = inner2.new InnerClass3();

        find(inner2.getClass());
        find(inner3.getClass());
    }

    @Test
    public void testComparator() {
        find(ComparatorWithGenerics.class);
    }

    @Test
    public void testAnonymousListenerAsMethodArgument() {
        final String base = AnonymousListenerDeclaredInMethod.class.getName();
        find(base + "$AdapterClassInit");
        find(base + "$AdapterMember");
        find(base + "$AdapterStatic");
        find(base + "$AdapterStatic$AdapterStatic2");
        find(base + "$AdapterConstructor");
    }

    @Test
    public void testAnonymousListenerInMethodBody() {
        find(AnonymousListenerDeclaredInMethodBody.class.getName() + "$1");
        find(AnonymousListenerDeclaredInMethodBody.class.getName() + "$Adapter");
    }

    @Test
    public void testAnonymousListenerDeclaredAsField() {
        find(AnonymousListenerDeclaredAsField.class.getName() + "$1");
    }

    @Test
    public void testAnonymousListenerDeclaredAsStaticField() {
        find(AnonymousListenerDeclaredAsStaticField.class.getName() + "$1");
    }

    @Test
    public void testAnonymousListenerDeclaredInStaticMethod() {
        find(AnonymousListenerDeclaredInStaticMethodBody.class.getName() + "$1");
    }

    @Test
    public void testMethods() {
        for (final Method m : Methods.class.getDeclaredMethods()) {
            final IMethodName name = Names.java2vmType(m);
            final IMethod jdtMethod = sut.toJdtMethod(name);
            assertNotNull("Expected method: " + name, jdtMethod);
            createFreshSut();
            final IMethodName recMethod = sut.toRecMethod(jdtMethod);
            assertEquals(name, recMethod);
        }

    }

    private void find(final Class<?> clazz) {
        final ITypeName name = Names.java2vmType(clazz);
        findAndCheck(name);
    }

    private void find(final String className) {
        final ITypeName name = VmTypeName.get(Names.src2vmType(className));
        findAndCheck(name);
    }

    private void findAndCheck(final ITypeName name) {
        final IType type = sut.toType(name);
        assertNotNull(type);
        testReverseLookup(name, type);
    }

    private void testReverseLookup(final ITypeName name, final IType type) {
        createFreshSut();
        final ITypeName reverse = sut.toRecType(type);
        assertEquals(name, reverse);
    }
}

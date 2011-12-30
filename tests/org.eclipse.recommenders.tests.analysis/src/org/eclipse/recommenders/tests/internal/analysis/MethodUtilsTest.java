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
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createMethodWithBooleanParameterMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createMethodWithIntegerParameterMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPackageVisibleMethodMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPrivateMethodMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createProtectedMethodMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicClinitMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicConstructorMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicFinalMethodMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicMethodMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicNativeMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.createPublicStaticMethodMock;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassGetDeclareMethods;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassGetMethodWithAnySelector;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockClassGetSuperclass;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockMethodGetDeclaringClass;
import static org.eclipse.recommenders.tests.wala.WalaMockUtils.mockMethodName;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.recommenders.internal.analysis.utils.MethodUtils;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;

public class MethodUtilsTest {

    @Test
    public void getDeclaredFinalAndPublicOrProtectedMethods() {
        final IClass clazz = createClassMock();
        final IMethod expected = createPublicFinalMethodMock();
        final Set<IMethod> someMethods = Sets.newHashSet(createPublicConstructorMock(), createPublicMethodMock(),
                createPrivateMethodMock(), createProtectedMethodMock(), expected);
        mockClassGetDeclareMethods(clazz, someMethods);
        final Collection<IMethod> actual = MethodUtils.findDeclaredFinalAndPublicOrProtectedMethods(clazz);
        assertEquals(Collections.singleton(expected), actual);
    }

    @Test
    public void testFindAllDeclaredPublicInstanceMethods() {
        // setup
        final IClass clazz = createClassMock();
        final Set<IMethod> expecteds = Sets.newHashSet(createPublicMethodMock(), createPublicFinalMethodMock());
        final Set<IMethod> someMethods = Sets.newHashSet(createPublicConstructorMock(), createPrivateMethodMock(),
                createPublicClinitMock(), createPublicNativeMock());
        someMethods.addAll(expecteds);
        mockClassGetDeclareMethods(clazz, someMethods);
        // exercise
        final Collection<IMethod> actuals = Sets.newHashSet(MethodUtils
                .findAllDeclaredPublicInstanceMethodsWithImplementation(clazz));
        // verify
        assertEquals(actuals, expecteds);
    }

    @Test
    public void testGetDeclaredOverridableMethods() {
        // setup
        final IClass clazz = createClassMock();
        final Set<IMethod> expected = Sets.newHashSet(createPublicMethodMock(), createProtectedMethodMock());
        final Set<IMethod> someMethods = Sets.newHashSet(createPublicConstructorMock(), createPublicFinalMethodMock(),
                createPrivateMethodMock());
        someMethods.addAll(expected);
        mockClassGetDeclareMethods(clazz, someMethods);
        // exercise
        final Collection<IMethod> actual = MethodUtils.findDeclaredOverridableMethods(clazz);
        // verify
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDeclaredConstructors() {
        // setup
        final IClass clazz = createClassMock();
        final Set<IMethod> expected = Sets.newHashSet(createPublicConstructorMock(), createPublicConstructorMock());
        mockClassGetDeclareMethods(clazz, expected);
        // exercise
        final Collection<IMethod> actual = MethodUtils.findDeclaredConstructors(clazz);
        // verify
        assertEquals(expected, actual);
    }

    @Test
    public void testGetSuperImplementation() throws Exception {
        // setup
        final IMethod method1 = createMethodWithBooleanParameterMock();
        final IMethod method2 = createMethodWithIntegerParameterMock();
        // exercise
        final boolean actual = MethodUtils.haveEqualSelector(method1, method2);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testEqualSelectorsWithDifferentMethods() throws Exception {
        // setup
        final IMethod method1 = createMethodWithBooleanParameterMock();
        final IMethod method2 = createMethodWithIntegerParameterMock();
        // exercise
        final boolean actual = MethodUtils.haveEqualSelector(method1, method2);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testEqualSelectorsWithSameMethod() throws Exception {
        //
        // setup
        // - inheritance tree
        final IClass subclass = createClassMock();
        final IClass baseclass = createClassMock();
        mockClassGetSuperclass(subclass, baseclass);
        // - setup overrides relation
        final IMethod baseclassMethod = createPublicMethodMock();
        mockClassGetMethodWithAnySelector(baseclass, baseclassMethod);
        //
        final IMethod subclassMethod = createPublicMethodMock();
        mockMethodGetDeclaringClass(subclassMethod, subclass);
        //
        // exercise
        final IMethod actual = MethodUtils.findSuperImplementation(subclassMethod);
        // verify
        assertEquals(baseclassMethod, actual);
    }

    @Test
    public void testHaveSameParametersWithDifferentParamTypes() throws Exception {
        // setup
        final IMethod booleanMethod = createMethodWithBooleanParameterMock();
        final IMethod integerMethod = createMethodWithIntegerParameterMock();
        // exercise
        final boolean actual = MethodUtils.haveSameParameters(booleanMethod, integerMethod);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testHaveSameParametersWithNoArgsAndOneArgsMethod() throws Exception {
        // setup
        final IMethod noArgMethod = createPublicMethodMock();
        final IMethod oneArgMethod = createMethodWithBooleanParameterMock();
        // exercise
        final boolean actual = MethodUtils.haveSameParameters(noArgMethod, oneArgMethod);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testHaveSameParametersWithSameParamTypes() throws Exception {
        // setup
        final IMethod booleanMethod1 = createMethodWithBooleanParameterMock();
        final IMethod booleanMethod2 = createMethodWithBooleanParameterMock();
        // exercise
        final boolean actual = MethodUtils.haveSameParameters(booleanMethod1, booleanMethod2);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsOverridableWithPrivateMethod() throws Exception {
        // setup
        final IMethod method = createPrivateMethodMock();
        // exercise
        final boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsOverridableWithProtectedMethod() throws Exception {
        // setup
        final IMethod method = createProtectedMethodMock();
        // exercise
        final boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsOverridableWithPublicConstrcutor() throws Exception {
        // setup
        final IMethod method = createPublicConstructorMock();
        // exercise
        final boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsOverridableWithPublicFinalMethod() throws Exception {
        // setup
        final IMethod method = createPublicFinalMethodMock();
        // exercise
        final boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsOverridableWithPublicMethod() throws Exception {
        // setup
        final IMethod method = createPublicMethodMock();
        // exercise
        final boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsOverridableWithPublicStaticMethod() throws Exception {
        // setup
        final IMethod method = createPublicStaticMethodMock();
        // exercise
        final boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsPackageVisibleMethod() throws Exception {
        // setup
        final IMethod method = createPackageVisibleMethodMock();
        // exercise
        final boolean actual = MethodUtils.isPackageVisible(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsCompilerGeneratedStaticAccessMethod_WithStaticMethodAndNotMatchingMethodNamee() throws Exception {
        // setup
        final IMethod plainStaticMethod = createPublicStaticMethodMock();
        mockMethodName(plainStaticMethod, "access");
        // exercise
        final boolean actual = MethodUtils.isCompilerGeneratedStaticAccessMethod(plainStaticMethod);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsCompilerGeneratedStaticAccessMethod_HappyPath() throws Exception {
        // setup
        final IMethod plainStaticMethod = createPublicStaticMethodMock();
        mockMethodName(plainStaticMethod, "access$0");
        // exercise
        final boolean actual = MethodUtils.isCompilerGeneratedStaticAccessMethod(plainStaticMethod);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsPackageVisibleMethodWithPublicMethod() throws Exception {
        // setup
        final IMethod method = createPublicMethodMock();
        // exercise
        final boolean actual = MethodUtils.isPackageVisible(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPrivateMethod() throws Exception {
        // setup
        final IMethod method = createPrivateMethodMock();
        // exercise
        final boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPublicConstructor() throws Exception {
        // setup
        final IMethod method = createPublicConstructorMock();
        // exercise
        final boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtationSuperIsFinal() throws Exception {
        // setup
        final IMethod superDecl = createPublicFinalMethodMock();
        final IMethod decl = createPublicMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_BothPublic() throws Exception {
        // setup
        final IMethod superDecl = createPublicMethodMock();
        final IMethod decl = createPublicMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_BothProtected() throws Exception {
        // setup
        final IMethod superDecl = createProtectedMethodMock();
        final IMethod decl = createPublicMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperPackageSubPrivate() throws Exception {
        // setup
        final IMethod superDecl = createPackageVisibleMethodMock();
        final IMethod decl = createPrivateMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperPackageSubProtected() throws Exception {
        // setup
        final IMethod superDecl = createPackageVisibleMethodMock();
        final IMethod decl = createProtectedMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperProtectedSubPublic() throws Exception {
        // setup
        final IMethod superDecl = createProtectedMethodMock();
        final IMethod decl = createPublicMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperPublicSubProtectedPublic() throws Exception {
        // setup
        final IMethod superDecl = createPublicMethodMock();
        final IMethod decl = createProtectedMethodMock();
        // exercise
        final boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPublicFinalMethod() throws Exception {
        // setup
        final IMethod method = createPublicFinalMethodMock();
        // exercise
        final boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPublicStaticMethod() throws Exception {
        // setup
        final IMethod method = createPublicStaticMethodMock();
        // exercise
        final boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }
}

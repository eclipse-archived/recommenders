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

import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createClassMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createMethodWithBooleanParameterMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createMethodWithIntegerParameterMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPackageVisibleMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPrivateMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createProtectedMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicClinitMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicConstructorMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicFinalMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicNativeMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.createPublicStaticMethodMock;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockClassGetDeclareMethods;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockClassGetMethodWithAnySelector;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockClassGetSuperclass;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockMethodGetDeclaringClass;
import static org.eclipse.recommenders.tests.commons.analysis.utils.WalaMockUtils.mockMethodName;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.MethodUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;

public class MethodUtilsTest {

    @Test
    public void getDeclaredFinalAndPublicOrProtectedMethods() {
        IClass clazz = createClassMock();
        IMethod expected = createPublicFinalMethodMock();
        Set<IMethod> someMethods = Sets.newHashSet(createPublicConstructorMock(), createPublicMethodMock(),
                createPrivateMethodMock(), createProtectedMethodMock(), expected);
        mockClassGetDeclareMethods(clazz, someMethods);
        Collection<IMethod> actual = MethodUtils.findDeclaredFinalAndPublicOrProtectedMethods(clazz);
        assertEquals(Collections.singleton(expected), actual);
    }

    @Test
    public void testFindAllDeclaredPublicInstanceMethods() {
        // setup
        IClass clazz = createClassMock();
        List<IMethod> expecteds = Lists.newArrayList(createPublicMethodMock(), createPublicFinalMethodMock());
        Set<IMethod> someMethods = Sets.newHashSet(createPublicConstructorMock(), createPrivateMethodMock(),
                createPublicClinitMock(), createPublicNativeMock());
        someMethods.addAll(expecteds);
        mockClassGetDeclareMethods(clazz, someMethods);
        // exercise
        Collection<IMethod> actuals = MethodUtils.findAllDeclaredPublicInstanceMethodsWithImplementation(clazz);
        // verify
        assertEquals(Collections.emptyList(), CollectionUtils.disjunction(expecteds, actuals));
    }

    @Test
    public void testGetDeclaredOverridableMethods() {
        // setup
        IClass clazz = createClassMock();
        Set<IMethod> expected = Sets.newHashSet(createPublicMethodMock(), createProtectedMethodMock());
        Set<IMethod> someMethods = Sets.newHashSet(createPublicConstructorMock(), createPublicFinalMethodMock(),
                createPrivateMethodMock());
        someMethods.addAll(expected);
        mockClassGetDeclareMethods(clazz, someMethods);
        // exercise
        Collection<IMethod> actual = MethodUtils.findDeclaredOverridableMethods(clazz);
        // verify
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDeclaredConstructors() {
        // setup
        IClass clazz = createClassMock();
        Set<IMethod> expected = Sets.newHashSet(createPublicConstructorMock(), createPublicConstructorMock());
        mockClassGetDeclareMethods(clazz, expected);
        // exercise
        Collection<IMethod> actual = MethodUtils.findDeclaredConstructors(clazz);
        // verify
        assertEquals(expected, actual);
    }

    @Test
    public void testGetSuperImplementation() throws Exception {
        // setup
        IMethod method1 = createMethodWithBooleanParameterMock();
        IMethod method2 = createMethodWithIntegerParameterMock();
        // exercise
        boolean actual = MethodUtils.haveEqualSelector(method1, method2);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testEqualSelectorsWithDifferentMethods() throws Exception {
        // setup
        IMethod method1 = createMethodWithBooleanParameterMock();
        IMethod method2 = createMethodWithIntegerParameterMock();
        // exercise
        boolean actual = MethodUtils.haveEqualSelector(method1, method2);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testEqualSelectorsWithSameMethod() throws Exception {
        //
        // setup
        // - inheritance tree
        IClass subclass = createClassMock();
        IClass baseclass = createClassMock();
        mockClassGetSuperclass(subclass, baseclass);
        // - setup overrides relation
        IMethod baseclassMethod = createPublicMethodMock();
        mockClassGetMethodWithAnySelector(baseclass, baseclassMethod);
        //
        IMethod subclassMethod = createPublicMethodMock();
        mockMethodGetDeclaringClass(subclassMethod, subclass);
        //
        // exercise
        IMethod actual = MethodUtils.findSuperImplementation(subclassMethod);
        // verify
        assertEquals(baseclassMethod, actual);
    }

    @Test
    public void testHaveSameParametersWithDifferentParamTypes() throws Exception {
        // setup
        IMethod booleanMethod = createMethodWithBooleanParameterMock();
        IMethod integerMethod = createMethodWithIntegerParameterMock();
        // exercise
        boolean actual = MethodUtils.haveSameParameters(booleanMethod, integerMethod);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testHaveSameParametersWithNoArgsAndOneArgsMethod() throws Exception {
        // setup
        IMethod noArgMethod = createPublicMethodMock();
        IMethod oneArgMethod = createMethodWithBooleanParameterMock();
        // exercise
        boolean actual = MethodUtils.haveSameParameters(noArgMethod, oneArgMethod);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testHaveSameParametersWithSameParamTypes() throws Exception {
        // setup
        IMethod booleanMethod1 = createMethodWithBooleanParameterMock();
        IMethod booleanMethod2 = createMethodWithBooleanParameterMock();
        // exercise
        boolean actual = MethodUtils.haveSameParameters(booleanMethod1, booleanMethod2);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsOverridableWithPrivateMethod() throws Exception {
        // setup
        IMethod method = createPrivateMethodMock();
        // exercise
        boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsOverridableWithProtectedMethod() throws Exception {
        // setup
        IMethod method = createProtectedMethodMock();
        // exercise
        boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsOverridableWithPublicConstrcutor() throws Exception {
        // setup
        IMethod method = createPublicConstructorMock();
        // exercise
        boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsOverridableWithPublicFinalMethod() throws Exception {
        // setup
        IMethod method = createPublicFinalMethodMock();
        // exercise
        boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsOverridableWithPublicMethod() throws Exception {
        // setup
        IMethod method = createPublicMethodMock();
        // exercise
        boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsOverridableWithPublicStaticMethod() throws Exception {
        // setup
        IMethod method = createPublicStaticMethodMock();
        // exercise
        boolean actual = MethodUtils.isOverridable(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsPackageVisibleMethod() throws Exception {
        // setup
        IMethod method = createPackageVisibleMethodMock();
        // exercise
        boolean actual = MethodUtils.isPackageVisible(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsCompilerGeneratedStaticAccessMethod_WithStaticMethodAndNotMatchingMethodNamee() throws Exception {
        // setup
        IMethod plainStaticMethod = createPublicStaticMethodMock();
        mockMethodName(plainStaticMethod, "access");
        // exercise
        boolean actual = MethodUtils.isCompilerGeneratedStaticAccessMethod(plainStaticMethod);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testIsCompilerGeneratedStaticAccessMethod_HappyPath() throws Exception {
        // setup
        IMethod plainStaticMethod = createPublicStaticMethodMock();
        mockMethodName(plainStaticMethod, "access$0");
        // exercise
        boolean actual = MethodUtils.isCompilerGeneratedStaticAccessMethod(plainStaticMethod);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testIsPackageVisibleMethodWithPublicMethod() throws Exception {
        // setup
        IMethod method = createPublicMethodMock();
        // exercise
        boolean actual = MethodUtils.isPackageVisible(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPrivateMethod() throws Exception {
        // setup
        IMethod method = createPrivateMethodMock();
        // exercise
        boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPublicConstructor() throws Exception {
        // setup
        IMethod method = createPublicConstructorMock();
        // exercise
        boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtationSuperIsFinal() throws Exception {
        // setup
        IMethod superDecl = createPublicFinalMethodMock();
        IMethod decl = createPublicMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_BothPublic() throws Exception {
        // setup
        IMethod superDecl = createPublicMethodMock();
        IMethod decl = createPublicMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_BothProtected() throws Exception {
        // setup
        IMethod superDecl = createProtectedMethodMock();
        IMethod decl = createPublicMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperPackageSubPrivate() throws Exception {
        // setup
        IMethod superDecl = createPackageVisibleMethodMock();
        IMethod decl = createPrivateMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperPackageSubProtected() throws Exception {
        // setup
        IMethod superDecl = createPackageVisibleMethodMock();
        IMethod decl = createProtectedMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperProtectedSubPublic() throws Exception {
        // setup
        IMethod superDecl = createProtectedMethodMock();
        IMethod decl = createPublicMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testModifiersAllowBeingAReimplemtation_SuperPublicSubProtectedPublic() throws Exception {
        // setup
        IMethod superDecl = createPublicMethodMock();
        IMethod decl = createProtectedMethodMock();
        // exercise
        boolean actual = MethodUtils.modifiersAllowOverridingMethodDeclaration(superDecl, decl);
        // verify
        assertEquals(false, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPublicFinalMethod() throws Exception {
        // setup
        IMethod method = createPublicFinalMethodMock();
        // exercise
        boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(true, actual);
    }

    @Test
    public void testMayHaveSuperDeclarationPublicStaticMethod() throws Exception {
        // setup
        IMethod method = createPublicStaticMethodMock();
        // exercise
        boolean actual = MethodUtils.mayHaveSuperDeclaration(method);
        // verify
        assertEquals(false, actual);
    }
}

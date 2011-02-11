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
package org.eclipse.recommenders.tests.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.SyntheticMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.strings.Atom;

public class WalaMockUtils {

    public static CGNode createCGNodeMock() {
        final CGNode mock = mock(CGNode.class);
        return mock;
    }

    public static IClassHierarchy createClassHierarchyMock() {
        final IClassHierarchy mock = mock(IClassHierarchy.class);
        return mock;
    }

    public static void mockCGNodeGetClassHierarchy(final CGNode node, final IClassHierarchy returnValue) {
        when(node.getClassHierarchy()).thenReturn(returnValue);
    }

    public static void mockClassHierarchyLookupAnyType(final IClassHierarchy cha, final IClass returnValue) {
        final TypeReference any = any();
        when(cha.lookupClass(any)).thenReturn(returnValue);
    }

    public static IClass createClassMock() {
        return createClassMock("Lsome/mock/Class");
    }

    public static IClass createClassMock(final String name) {
        final IClass res = mock(IClass.class);
        final TypeName typeName = TypeName.findOrCreate(name);
        final TypeReference typeRef = TypeReference.findOrCreate(ClassLoaderReference.Primordial, typeName);
        when(res.getName()).thenReturn(typeName);
        when(res.getReference()).thenReturn(typeRef);
        return res;
    }

    public static void mockClassGetDeclaredFields(final IClass c, final List<IField> declaredFields) {
        when(c.getDeclaredInstanceFields()).thenReturn(declaredFields);
    }

    public static IMethod createMethodMock() {
        final IMethod res = mock(IMethod.class);
        when(res.getSignature()).thenReturn("LClass.mockedSignature()V");
        return res;
    }

    public static IMethod createSyntheticMethodMock() {
        final IMethod res = mock(SyntheticMethod.class);
        return res;
    }

    public static IMethod createMethod(final String declaringClass, final String methodName, final String signature) {
        final IMethod res = createSyntheticMethodMock();
        final MethodReference ref = MethodReference.findOrCreate(ClassLoaderReference.Application, declaringClass,
                methodName, signature);
        when(res.getReference()).thenReturn(ref);
        when(res.getSignature()).thenCallRealMethod();
        when(res.getSelector()).thenCallRealMethod();
        return res;
    }

    public static IMethod createMethodWithBooleanParameterMock() {
        final IMethod res = createPublicMethodMock();
        mockMethodName(res, "someName");
        mockMethodParameters(res, TypeReference.JavaLangBoolean);
        mockMethodReturn(res, TypeReference.Void);
        mockMethodSelector(res);
        return res;
    }

    public static Entrypoint createEntryPointMock(final IMethod mock) {
        return new DefaultEntrypoint(mock, null);
    }

    public static IMethod createMethodWithIntegerParameterMock() {
        final IMethod res = createPublicMethodMock();
        mockMethodName(res, "someName");
        mockMethodParameters(res, TypeReference.JavaLangInteger);
        mockMethodReturn(res, TypeReference.Void);
        mockMethodSelector(res);
        return res;
    }

    public static IMethod createPackageVisibleMethodMock() {
        final IMethod res = createMethodMock();
        when(res.isPublic()).thenReturn(false);
        when(res.isPrivate()).thenReturn(false);
        when(res.isProtected()).thenReturn(false);
        return res;
    }

    public static NewSiteReference createNewSiteMock() {
        final NewSiteReference mock = mock(NewSiteReference.class);
        return mock;
    }

    public static void mockNewSiteGetDeclaredType(final NewSiteReference site, final TypeReference returnValue) {
        when(site.getDeclaredType()).thenReturn(returnValue);
    }

    public static IMethod createPrivateMethodMock() {
        final IMethod res = createMethodMock();
        mockMethodIsPrivate(res, true);
        return res;
    }

    public static IMethod createProtectedMethodMock() {
        final IMethod res = createMethodMock();
        mockMethodIsProtected(res, true);
        return res;
    }

    public static IMethod createPublicClinitMock() {
        final IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsClinit(res, true);
        mockMethodIsStatic(res, true);
        return res;
    }

    public static IMethod createPublicNativeMock() {
        final IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsNative(res, true);
        mockMethodIsAbstract(res, true);
        return res;
    }

    public static IMethod createConstructor() {
        final IMethod res = createMethodMock();
        mockMethodIsInit(res, true);
        final Selector noArgsSelector = Selector.make("<init>()V");
        when(res.getSelector()).thenReturn(noArgsSelector);
        return res;
    }

    public static IMethod createPublicConstructorMock() {
        final IMethod res = createConstructor();
        mockMethodIsPublic(res, true);
        return res;
    }

    public static IMethod createProtectedConstructorMock() {
        final IMethod res = createConstructor();
        mockMethodIsProtected(res, true);
        return res;
    }

    public static IMethod createPublicFinalMethodMock() {
        final IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsFinal(res, true);
        return res;
    }

    public static IMethod createPublicMethodMock() {
        final IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        return res;
    }

    public static IMethod createPublicStaticMethodMock() {
        final IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsStatic(res, true);
        return res;
    }

    public static IMethod createProtectedStaticMethodMock() {
        final IMethod res = createMethodMock();
        mockMethodIsProtected(res, true);
        mockMethodIsStatic(res, true);
        return res;
    }

    public static IClass createPublicClass() {
        final IClass res = createClassMock();
        when(res.getModifiers()).thenReturn(Modifier.PUBLIC);
        return res;
    }

    public static IClass createStaticClassMock() {
        final IClass res = createClassMock();
        when(res.getModifiers()).thenReturn(Modifier.STATIC);
        return res;
    }

    public static IClass createInterface(final String name) {
        final IClass res = createClassMock();
        when(res.isInterface()).thenReturn(true);
        mockClassGetName(res, name);
        return res;
    }

    public static IField createPublicStringField() {
        final IField res = createField(TypeReference.JavaLangString, "stringField", null);
        mockFieldIsPublic(res);
        return res;
    }

    public static IField createPrivateIntegerField() {
        final IField res = createField(TypeReference.JavaLangInteger, "integerField", null);
        mockFieldIsPrivate(res);
        return res;
    }

    public static void mockFieldIsPublic(final IField res) {
        when(res.isPublic()).thenReturn(true);
    }

    public static void mockFieldIsPrivate(final IField res) {
        when(res.isPrivate()).thenReturn(true);
    }

    public static IField createField(final TypeReference type, final String fieldName, final IClass declaringClass) {
        final IField mock = mock(IField.class);
        when(mock.getDeclaringClass()).thenReturn(declaringClass);
        when(mock.getName()).thenReturn(Atom.findOrCreateUnicodeAtom(fieldName));
        when(mock.getFieldTypeReference()).thenReturn(type);
        return mock;
    }

    public static void mockClassGetDeclareMethods(final IClass clazz, final Collection<IMethod> returnedMethods) {
        when(clazz.getDeclaredMethods()).thenReturn(returnedMethods);
    }

    public static void mockClassGetMethodWithAnySelector(final IClass clazz, final IMethod returnValue) {
        final Selector any = (Selector) any();
        when(clazz.getMethod(any)).thenReturn(returnValue);
    }

    public static void mockClassGetSuperclass(final IClass subclass, final IClass superclass) {
        when(subclass.getSuperclass()).thenReturn(superclass);
    }

    public static void mockClassGetName(final IClass clazz, final String name) {
        final TypeName typeName = TypeName.findOrCreate(name);
        when(clazz.getName()).thenReturn(typeName);
    }

    public static void mockClassIsPrimordial(final IClass clazz) {
        mockClassClassLoader(clazz, ClassLoaderReference.Primordial);
    }

    public static void mockClassIsApplication(final IClass clazz) {
        mockClassClassLoader(clazz, ClassLoaderReference.Application);
    }

    public static void mockClassClassLoader(final IClass clazz, final ClassLoaderReference ref) {
        final IClassLoader cl = mock(IClassLoader.class);
        when(cl.getReference()).thenReturn(ref);
        when(clazz.getClassLoader()).thenReturn(cl);
    }

    public static TypeReference createSomeApplicationTypeReference() {
        return TypeReference.findOrCreate(ClassLoaderReference.Application, "Lsome/Type");
    }

    public static TypeReference createSomePrimordialTypeReference() {
        return TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/String");
    }

    public static void mockTypeReferenceClassLoader(final TypeReference type, final ClassLoaderReference ref) {
        when(type.getClassLoader()).thenReturn(ref);
    }

    public static void mockMethodGetDeclaringClass(final IMethod method, final IClass returnValue) {
        when(method.getDeclaringClass()).thenReturn(returnValue);
    }

    public static IMethod mockMethodIsClinit(final IMethod res, final boolean value) {
        when(res.isClinit()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsFinal(final IMethod res, final boolean value) {
        when(res.isFinal()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsInit(final IMethod res, final boolean value) {
        when(res.isInit()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsPrivate(final IMethod res, final boolean value) {
        when(res.isPrivate()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsProtected(final IMethod res, final boolean value) {
        when(res.isProtected()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsPublic(final IMethod res, final boolean value) {
        when(res.isPublic()).thenReturn(value);
        return res;
    }

    private static IMethod mockMethodIsNative(final IMethod method, final boolean value) {
        when(method.isNative()).thenReturn(value);
        return method;
    }

    private static IMethod mockMethodIsAbstract(final IMethod method, final boolean value) {
        when(method.isAbstract()).thenReturn(true);
        return method;
    }

    public static IMethod mockMethodIsStatic(final IMethod method, final boolean value) {
        when(method.isStatic()).thenReturn(value);
        return method;
    }

    public static IMethod mockMethodName(final IMethod method, final String name) {
        final Atom nameAtom = Atom.findOrCreateAsciiAtom(name);
        when(method.getName()).thenReturn(nameAtom);
        return method;
    }

    public static void mockMethodParameters(final IMethod res, final TypeReference... parameterTypes) {
        when(res.getNumberOfParameters()).thenReturn(parameterTypes.length);
        for (int i = parameterTypes.length; i-- > 0;) {
            when(res.getParameterType(i)).thenReturn(parameterTypes[i]);
        }
    }

    public static void mockMethodReturn(final IMethod res, final TypeReference returnValue) {
        when(res.getReturnType()).thenReturn(returnValue);
    }

    public static void mockMethodSelector(final IMethod res) {
        ensureIsNotNull(res.getReturnType());
        ensureIsNotNull(res.getName());
        final List<TypeName> paramTypeNames = Lists.newLinkedList();
        for (int i = 0; i < res.getNumberOfParameters(); i++) {
            final TypeReference parameterType = res.getParameterType(i);
            final TypeName name = parameterType.getName();
            paramTypeNames.add(name);
        }
        final Atom methodName = res.getName();
        final TypeName returnTypeName = res.getReturnType().getName();
        final Descriptor descriptor = Descriptor.findOrCreate(paramTypeNames.toArray(new TypeName[0]), returnTypeName);
        when(res.getSelector()).thenReturn(new Selector(methodName, descriptor));
    }

    public static SSAPropagationCallGraphBuilder createCallGraphBuilderMock() {
        final SSAPropagationCallGraphBuilder mock = mock(SSAPropagationCallGraphBuilder.class);
        return mock;
    }

    public static CallSiteReference createCallSiteReferenceMock() {
        final CallSiteReference mock = mock(CallSiteReference.class);
        return mock;
    }

    public static void mockCallSiteGetDeclaredTarget(final CallSiteReference call, final MethodReference returnValue) {
        when(call.getDeclaredTarget()).thenReturn(returnValue);
    }

    public static void mockCallSiteIsFixed(final CallSiteReference call, final boolean returnValue) {
        when(call.isFixed()).thenReturn(returnValue);
    }

    public static void mockCallSiteIsDispatch(final CallSiteReference call, final boolean returnValue) {
        when(call.isDispatch()).thenReturn(returnValue);
    }
}

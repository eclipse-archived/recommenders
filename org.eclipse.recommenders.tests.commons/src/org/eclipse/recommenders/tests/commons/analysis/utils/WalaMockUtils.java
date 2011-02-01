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
        CGNode mock = mock(CGNode.class);
        return mock;
    }

    public static IClassHierarchy createClassHierarchyMock() {
        IClassHierarchy mock = mock(IClassHierarchy.class);
        return mock;
    }

    public static void mockCGNodeGetClassHierarchy(CGNode node, IClassHierarchy returnValue) {
        when(node.getClassHierarchy()).thenReturn(returnValue);
    }

    public static void mockClassHierarchyLookupAnyType(IClassHierarchy cha, IClass returnValue) {
        TypeReference any = any();
        when(cha.lookupClass(any)).thenReturn(returnValue);
    }

    public static IClass createClassMock() {
        return createClassMock("Lsome/mock/Class");
    }

    public static IClass createClassMock(String name) {
        IClass res = mock(IClass.class);
        TypeName typeName = TypeName.findOrCreate(name);
        TypeReference typeRef = TypeReference.findOrCreate(ClassLoaderReference.Primordial, typeName);
        when(res.getName()).thenReturn(typeName);
        when(res.getReference()).thenReturn(typeRef);
        return res;
    }

    public static void mockClassGetDeclaredFields(IClass c, List<IField> declaredFields) {
        when(c.getDeclaredInstanceFields()).thenReturn(declaredFields);
    }

    public static IMethod createMethodMock() {
        IMethod res = mock(IMethod.class);
        when(res.getSignature()).thenReturn("LClass.mockedSignature()V");
        return res;
    }

    public static IMethod createSyntheticMethodMock() {
        IMethod res = mock(SyntheticMethod.class);
        return res;
    }

    public static IMethod createMethod(String declaringClass, String methodName, String signature) {
        IMethod res = createSyntheticMethodMock();
        MethodReference ref = MethodReference.findOrCreate(ClassLoaderReference.Application, declaringClass,
                methodName, signature);
        when(res.getReference()).thenReturn(ref);
        when(res.getSignature()).thenCallRealMethod();
        when(res.getSelector()).thenCallRealMethod();
        return res;
    }

    public static IMethod createMethodWithBooleanParameterMock() {
        IMethod res = createPublicMethodMock();
        mockMethodName(res, "someName");
        mockMethodParameters(res, TypeReference.JavaLangBoolean);
        mockMethodReturn(res, TypeReference.Void);
        mockMethodSelector(res);
        return res;
    }

    public static Entrypoint createEntryPointMock(IMethod mock) {
        return new DefaultEntrypoint(mock, null);
    }

    public static IMethod createMethodWithIntegerParameterMock() {
        IMethod res = createPublicMethodMock();
        mockMethodName(res, "someName");
        mockMethodParameters(res, TypeReference.JavaLangInteger);
        mockMethodReturn(res, TypeReference.Void);
        mockMethodSelector(res);
        return res;
    }

    public static IMethod createPackageVisibleMethodMock() {
        IMethod res = createMethodMock();
        when(res.isPublic()).thenReturn(false);
        when(res.isPrivate()).thenReturn(false);
        when(res.isProtected()).thenReturn(false);
        return res;
    }

    public static NewSiteReference createNewSiteMock() {
        NewSiteReference mock = mock(NewSiteReference.class);
        return mock;
    }

    public static void mockNewSiteGetDeclaredType(NewSiteReference site, TypeReference returnValue) {
        when(site.getDeclaredType()).thenReturn(returnValue);
    }

    public static IMethod createPrivateMethodMock() {
        IMethod res = createMethodMock();
        mockMethodIsPrivate(res, true);
        return res;
    }

    public static IMethod createProtectedMethodMock() {
        IMethod res = createMethodMock();
        mockMethodIsProtected(res, true);
        return res;
    }

    public static IMethod createPublicClinitMock() {
        IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsClinit(res, true);
        mockMethodIsStatic(res, true);
        return res;
    }

    public static IMethod createPublicNativeMock() {
        IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsNative(res, true);
        mockMethodIsAbstract(res, true);
        return res;
    }

    public static IMethod createConstructor() {
        IMethod res = createMethodMock();
        mockMethodIsInit(res, true);
        Selector noArgsSelector = Selector.make("<init>()V");
        when(res.getSelector()).thenReturn(noArgsSelector);
        return res;
    }

    public static IMethod createPublicConstructorMock() {
        IMethod res = createConstructor();
        mockMethodIsPublic(res, true);
        return res;
    }

    public static IMethod createProtectedConstructorMock() {
        IMethod res = createConstructor();
        mockMethodIsProtected(res, true);
        return res;
    }

    public static IMethod createPublicFinalMethodMock() {
        IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsFinal(res, true);
        return res;
    }

    public static IMethod createPublicMethodMock() {
        IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        return res;
    }

    public static IMethod createPublicStaticMethodMock() {
        IMethod res = createMethodMock();
        mockMethodIsPublic(res, true);
        mockMethodIsStatic(res, true);
        return res;
    }

    public static IMethod createProtectedStaticMethodMock() {
        IMethod res = createMethodMock();
        mockMethodIsProtected(res, true);
        mockMethodIsStatic(res, true);
        return res;
    }

    public static IClass createPublicClass() {
        IClass res = createClassMock();
        when(res.getModifiers()).thenReturn(Modifier.PUBLIC);
        return res;
    }

    public static IClass createStaticClassMock() {
        IClass res = createClassMock();
        when(res.getModifiers()).thenReturn(Modifier.STATIC);
        return res;
    }

    public static IClass createInterface(String name) {
        IClass res = createClassMock();
        when(res.isInterface()).thenReturn(true);
        mockClassGetName(res, name);
        return res;
    }

    public static IField createPublicStringField() {
        IField res = createField(TypeReference.JavaLangString, "stringField", null);
        mockFieldIsPublic(res);
        return res;
    }

    public static IField createPrivateIntegerField() {
        IField res = createField(TypeReference.JavaLangInteger, "integerField", null);
        mockFieldIsPrivate(res);
        return res;
    }

    public static void mockFieldIsPublic(IField res) {
        when(res.isPublic()).thenReturn(true);
    }

    public static void mockFieldIsPrivate(IField res) {
        when(res.isPrivate()).thenReturn(true);
    }

    public static IField createField(TypeReference type, String fieldName, IClass declaringClass) {
        IField mock = mock(IField.class);
        when(mock.getDeclaringClass()).thenReturn(declaringClass);
        when(mock.getName()).thenReturn(Atom.findOrCreateUnicodeAtom(fieldName));
        when(mock.getFieldTypeReference()).thenReturn(type);
        return mock;
    }

    public static void mockClassGetDeclareMethods(IClass clazz, Collection<IMethod> returnedMethods) {
        when(clazz.getDeclaredMethods()).thenReturn(returnedMethods);
    }

    public static void mockClassGetMethodWithAnySelector(IClass clazz, IMethod returnValue) {
        Selector any = (Selector) any();
        when(clazz.getMethod(any)).thenReturn(returnValue);
    }

    public static void mockClassGetSuperclass(IClass subclass, IClass superclass) {
        when(subclass.getSuperclass()).thenReturn(superclass);
    }

    public static void mockClassGetName(IClass clazz, String name) {
        TypeName typeName = TypeName.findOrCreate(name);
        when(clazz.getName()).thenReturn(typeName);
    }

    public static void mockClassIsPrimordial(IClass clazz) {
        mockClassClassLoader(clazz, ClassLoaderReference.Primordial);
    }

    public static void mockClassIsApplication(IClass clazz) {
        mockClassClassLoader(clazz, ClassLoaderReference.Application);
    }

    public static void mockClassClassLoader(IClass clazz, ClassLoaderReference ref) {
        IClassLoader cl = mock(IClassLoader.class);
        when(cl.getReference()).thenReturn(ref);
        when(clazz.getClassLoader()).thenReturn(cl);
    }

    public static TypeReference createSomeApplicationTypeReference() {
        return TypeReference.findOrCreate(ClassLoaderReference.Application, "Lsome/Type");
    }

    public static TypeReference createSomePrimordialTypeReference() {
        return TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/String");
    }

    public static void mockTypeReferenceClassLoader(TypeReference type, ClassLoaderReference ref) {
        when(type.getClassLoader()).thenReturn(ref);
    }

    public static void mockMethodGetDeclaringClass(IMethod method, IClass returnValue) {
        when(method.getDeclaringClass()).thenReturn(returnValue);
    }

    public static IMethod mockMethodIsClinit(IMethod res, boolean value) {
        when(res.isClinit()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsFinal(IMethod res, boolean value) {
        when(res.isFinal()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsInit(IMethod res, boolean value) {
        when(res.isInit()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsPrivate(IMethod res, boolean value) {
        when(res.isPrivate()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsProtected(IMethod res, boolean value) {
        when(res.isProtected()).thenReturn(value);
        return res;
    }

    public static IMethod mockMethodIsPublic(IMethod res, boolean value) {
        when(res.isPublic()).thenReturn(value);
        return res;
    }

    private static IMethod mockMethodIsNative(IMethod method, boolean value) {
        when(method.isNative()).thenReturn(value);
        return method;
    }

    private static IMethod mockMethodIsAbstract(IMethod method, boolean value) {
        when(method.isAbstract()).thenReturn(true);
        return method;
    }

    public static IMethod mockMethodIsStatic(IMethod method, boolean value) {
        when(method.isStatic()).thenReturn(value);
        return method;
    }

    public static IMethod mockMethodName(IMethod method, String name) {
        Atom nameAtom = Atom.findOrCreateAsciiAtom(name);
        when(method.getName()).thenReturn(nameAtom);
        return method;
    }

    public static void mockMethodParameters(IMethod res, TypeReference... parameterTypes) {
        when(res.getNumberOfParameters()).thenReturn(parameterTypes.length);
        for (int i = parameterTypes.length; i-- > 0;) {
            when(res.getParameterType(i)).thenReturn(parameterTypes[i]);
        }
    }

    public static void mockMethodReturn(IMethod res, TypeReference returnValue) {
        when(res.getReturnType()).thenReturn(returnValue);
    }

    public static void mockMethodSelector(IMethod res) {
        ensureIsNotNull(res.getReturnType());
        ensureIsNotNull(res.getName());
        List<TypeName> paramTypeNames = Lists.newLinkedList();
        for (int i = 0; i < res.getNumberOfParameters(); i++) {
            TypeReference parameterType = res.getParameterType(i);
            TypeName name = parameterType.getName();
            paramTypeNames.add(name);
        }
        Atom methodName = res.getName();
        TypeName returnTypeName = res.getReturnType().getName();
        Descriptor descriptor = Descriptor.findOrCreate(paramTypeNames.toArray(new TypeName[0]), returnTypeName);
        when(res.getSelector()).thenReturn(new Selector(methodName, descriptor));
    }

    public static SSAPropagationCallGraphBuilder createCallGraphBuilderMock() {
        SSAPropagationCallGraphBuilder mock = mock(SSAPropagationCallGraphBuilder.class);
        return mock;
    }

    public static CallSiteReference createCallSiteReferenceMock() {
        CallSiteReference mock = mock(CallSiteReference.class);
        return mock;
    }

    public static void mockCallSiteGetDeclaredTarget(CallSiteReference call, MethodReference returnValue) {
        when(call.getDeclaredTarget()).thenReturn(returnValue);
    }

    public static void mockCallSiteIsFixed(CallSiteReference call, boolean returnValue) {
        when(call.isFixed()).thenReturn(returnValue);
    }

    public static void mockCallSiteIsDispatch(CallSiteReference call, boolean returnValue) {
        when(call.isDispatch()).thenReturn(returnValue);
    }
}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Andreas Kaluza - modified implementation
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import static org.eclipse.recommenders.commons.utils.Checks.cast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.internal.rcp.codecompletion.CompilerBindings;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

@SuppressWarnings("restriction")
public class ChainCompletionContext {

  private final IIntelligentCompletionContext ctx;
  private final List<IChainElement> accessibleFields = Lists.newLinkedList();
  private final List<IChainElement> accessibleMethods = Lists.newLinkedList();
  private final List<IChainElement> accessibleLocals = Lists.newLinkedList();
  private final JavaElementResolver javaElementResolver;
  private final IClassHierarchyService walaChaService;
  private IClass receiverType;
  private List<Tuple<IClass, Integer>> expectedTypeList;
  private IClass enclosingType;
  private IMethod enclosingMethod;
  private List<String> localNames;
  private static IClassLoader loader;

  public ChainCompletionContext(final IIntelligentCompletionContext ctx, final JavaElementResolver javaElementResolver,
      final IClassHierarchyService walaChaService) {
    this.ctx = ctx;
    this.javaElementResolver = javaElementResolver;
    this.walaChaService = walaChaService;
    initializeAccessibleElements();
  }

  private void initializeAccessibleElements() {
    if (!findEnclosingClass()) {
      return;
    }
    if (!findEnclosingMethod()) {
      // return;
    }
    if (!findReceiverClass()) {
      return;
    }
    computeAccessibleMethods();

    if (!findExpectedClass()) {
      accessibleMethods.clear();
      return;
    }

    computeAccessibleLocals();
    computeAccessibleFields();

  }

  private boolean findEnclosingClass() {
    final ITypeName name = ctx.getEnclosingType();
    enclosingType = toWalaClass(name);
    loader = enclosingType.getClassLoader();
    return enclosingType != null;
  }

  public IClass toWalaClass(final ITypeName typeName) {
    if (typeName == null) {
      return null;
    }
    final IType jdtType = javaElementResolver.toJdtType(typeName);
    if (jdtType == null) {
      return null;
    }
    final IClass walaType = walaChaService.getType(jdtType);
    return walaType;
  }

  private boolean findEnclosingMethod() {
    final IMethodName name = ctx.getEnclosingMethod();
    enclosingMethod = toWalaMethod(name);
    return enclosingMethod != null;
  }

  private IMethod toWalaMethod(final IMethodName methodName) {
    if (methodName == null) {
      return null;
    }
    final org.eclipse.jdt.core.IMethod jdtMethod = javaElementResolver.toJdtMethod(methodName);
    if (jdtMethod == null) {
      return null;
    }
    final IMethod walaMethod = walaChaService.getMethod(jdtMethod);
    return walaMethod;
  }

  private boolean findExpectedClass() {
    final ITypeName expectedTypeName = ctx.getExpectedType();
    if (expectedTypeName == null) {
      if (ctx.getCompletionNode() instanceof CompletionOnMessageSend) {
        CompletionOnMessageSend completiononMessageSend = (CompletionOnMessageSend) ctx.getCompletionNode();
        computeExpectedMethodTypes(completiononMessageSend);
        return expectedTypeList.size() > 0;
      }
      return false;
    }
    expectedTypeList = new ArrayList<Tuple<IClass, Integer>>();
    IClass expectedType = toWalaClass(expectedTypeName.isArrayType() ? expectedTypeName.getArrayBaseType()
        : expectedTypeName);
    if (expectedType == null) {
      expectedType = boxPrimitive(expectedTypeName.getClassName());
    }
    if (expectedType != null && expectedType.getReference().getName().getClassName().toString().equals("Object")) {
      expectedType = null;
    }
    int expectedTypeArrayDimension = expectedTypeName.getArrayDimensions();
    expectedTypeList.add(Tuple.create(expectedType, expectedTypeArrayDimension));
    return expectedType != null;
  }

  public IClass getRevieverType() {
    return receiverType;
  }

  private boolean findReceiverClass() {

    if (ctx.isReceiverImplicitThis()) {
      receiverType = enclosingType;
    } else if (ctx.getCompletionNode() instanceof CompletionOnSingleNameReference) {
      receiverType = enclosingType;
    } else if (ctx.getCompletionNode() instanceof CompletionOnMessageSend) {
      receiverType = enclosingType;
    } else {
      final ITypeName receiverTypeName = ctx.getReceiverType();
      receiverType = toWalaClass(receiverTypeName);
    }
    return receiverType != null;
  }

  private void computeAccessibleFields() {
    for (final IField field : receiverType.getAllFields()) {
      if (!isAccessible(field)) {
        continue;
      }
      if (!matchesPrefix(field)) {
        continue;
      }
      if (ctx.getCompletionNodeParent() instanceof FieldDeclaration) {
        final FieldDeclaration node = cast(ctx.getCompletionNodeParent());
        final String name = String.valueOf(node.name);
        if (name.equals(field.getName().toString())) {
          continue;
        }
      }

      final FieldChainElement chainElement = new FieldChainElement(field, 0);
      if (localNames.contains(chainElement.getCompletion())) {
        chainElement.setThisQualifier(true);
      }
      chainElement.setRootElement(true);
      accessibleFields.add(chainElement);
    }
  }

  private boolean isAccessible(final IMember member) {

    if (ctx.isReceiverImplicitThis()) {
      if (enclosingMethod != null && enclosingMethod.isStatic() && !member.isStatic()
          && !(member instanceof IMethod && ((IMethod) member).getReturnType().isPrimitiveType())
          && !(member instanceof IField && ((IField) member).getFieldTypeReference().isPrimitiveType())) {
        return false;
      }
    }

    final Variable var = ctx.getVariable();
    if (var == null && !(ctx.getCompletionNode() instanceof CompletionOnMessageSend)) {
      return member.isStatic();
    }

    if (member.getDeclaringClass() == receiverType) {
      return true;
    }

    if (!member.isPrivate()) {
      return true;
    }
    return false;
  }

  private boolean matchesPrefix(final IMember member) {
    final String prefixToken = ctx.getPrefixToken();
    final String name = member.getName().toString();
    return name.startsWith(prefixToken);
  }

  private void computeAccessibleMethods() {

    for (final IMethod method : receiverType.getAllMethods()) {
      if (method.isVoid()) {
        continue;
      }

      if (!isAccessible(method)) {
        continue;
      }
      if (!matchesPrefix(method)) {
        continue;
      }

      final MethodChainElement chainElement = new MethodChainElement(method, 0);
      chainElement.setRootElement(true);
      if (unwantedNames(chainElement.getCompletion())) {
        continue;
      }
      accessibleMethods.add(chainElement);
    }
  }

  private void computeAccessibleLocals() {
    localNames = new ArrayList<String>();
    if (ctx.getVariable() == null) {
      return;
    }

    if (ctx.getVariable() == null || !ctx.getVariable().isThis()) {
      return;
    }

    if (ctx.getCompletionNodeParent() instanceof AbstractVariableDeclaration) {
      AbstractVariableDeclaration decl = (AbstractVariableDeclaration) ctx.getCompletionNodeParent();
      localNames.add(new String(decl.name));
    }
    for (final LocalDeclaration local : ctx.getLocalDeclarations()) {
      final ITypeName typeName = CompilerBindings.toTypeName(local.type);
      IClass localType = toWalaClass(typeName);
      boolean isPrimitive = false;
      if (localType == null) {
        localType = boxPrimitive(typeName.getClassName());
        isPrimitive = true;
      }
      if (localType == null) {
        continue;
      }
      final String localName = String.valueOf(local.name);
      if (!localName.startsWith(ctx.getPrefixToken())) {
        continue;
      }
      localNames.add(localName);

      if (ctx.getCompletionNodeParent() instanceof LocalDeclaration) {
        final LocalDeclaration node = cast(ctx.getCompletionNodeParent());
        final String name = String.valueOf(node.name);
        if (name.equals(localName)) {
          continue;
        }
      }

      final LocalChainElement element = new LocalChainElement(localName, localType, 0);
      element.setArrayDimension(typeName.getArrayDimensions());
      element.setRootElement(true);
      element.setPrimitive(isPrimitive);
      accessibleLocals.add(element);
    }
  }

  private void computeExpectedMethodTypes(CompletionOnMessageSend completionOnMessageSend) {
    String methodName = new String(completionOnMessageSend.selector);
    List<ITypeName> methodArguments = new LinkedList<ITypeName>();
    if (completionOnMessageSend.arguments != null) {
      for (Expression exp : completionOnMessageSend.arguments) {
        methodArguments.add(CompilerBindings.toTypeName(exp.resolvedType));
      }
    }
    List<IMethod> possibleMethods = findMethodInContext(methodArguments, methodName);
    expectedTypeList = new ArrayList<Tuple<IClass, Integer>>();
    for (IMethod method : possibleMethods) {
      oneMethod(method, methodArguments);
    }
  }

  private void oneMethod(IMethod method, List<ITypeName> methodArguments) {
    int usedParameters = methodArguments.size();
    if (!method.isStatic()) {
      usedParameters++;
    }
    TypeReference expectedTypeReference = method.getParameterType(usedParameters);
    if (expectedTypeReference.isPrimitiveType()) {
      return;
    }
    storeToExpectedTypeList(method, expectedTypeReference);
  }

  private void storeToExpectedTypeList(IMethod method, TypeReference expectedTypeReference) {
    IClass expectedType = method.getClassHierarchy().lookupClass(expectedTypeReference);
    if (expectedType == null || expectedType.getReference().getName().getClassName().toString().equals("Object")) {
      return;
    }
    int expectedTypeArrayDimension = expectedType.getReference().getDimensionality();
    expectedTypeList.add(Tuple.create(expectedType, expectedTypeArrayDimension));
  }

  private List<IMethod> findMethodInContext(List<ITypeName> methodArguments, String methodName) {
    List<IMethod> possibleMethods = Lists.newArrayList();
    for (IChainElement e : accessibleMethods) {
      MethodChainElement element = (MethodChainElement) e;
      if (!checkMethodRelevance(element, methodArguments, methodName)) {
        continue;
      }
      possibleMethods.add(element.getMethod());
    }
    return possibleMethods;
  }

  private boolean checkMethodRelevance(MethodChainElement element, List<ITypeName> methodArguments, String methodName) {
    int numberOfParameters = element.getMethod().getNumberOfParameters();
    int usedParameters = methodArguments.size();
    if (!element.getMethod().isStatic()) {
      usedParameters++;
    }
    if (numberOfParameters <= usedParameters) {
      return false;
    }
    for (int i = 0; i < methodArguments.size(); i++) {
      IClass typeClass = toWalaClass(methodArguments.get(i));
      if (!typeClass.getName().equals(
          element.getMethod().getParameterType(i + (element.getMethod().isStatic() ? 0 : 1)).getName())) {
        return false;
      }
    }
    return element.getCompletion().equals(methodName);
  }

  public List<Tuple<IClass, Integer>> getExpectedTypeList() {
    return expectedTypeList;
  }

  public IClass getCallingContext() {
    return enclosingType;
  }

  public List<IChainElement> getProposedFields() {
    return accessibleFields;
  }

  public List<IChainElement> getProposedMethods() {
    return accessibleMethods;
  }

  public List<IChainElement> getProposedVariables() {
    return accessibleLocals;
  }

  public static IClass boxPrimitive(String primitiveName) {
    if (primitiveName.equals("boolean") || primitiveName.equals("Z")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Boolean"));
    } else if (primitiveName.equals("byte") || primitiveName.equals("B")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Byte"));
    } else if (primitiveName.equals("char") || primitiveName.equals("C")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Character"));
    } else if (primitiveName.equals("double") || primitiveName.equals("D")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Double"));
    } else if (primitiveName.equals("float") || primitiveName.equals("F")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Float"));
    } else if (primitiveName.equals("int") || primitiveName.equals("I")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Integer"));
    } else if (primitiveName.equals("long") || primitiveName.equals("J")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Long"));
    } else if (primitiveName.equals("void") || primitiveName.equals("V")) {
      return null;
    } else if (primitiveName.equals("shoart") || primitiveName.equals("S")) {
      return loader.lookupClass(TypeName.findOrCreateClassName("java/lang", "Short"));
    }
    return null;
  }

  public static boolean unwantedNames(String name) {
    boolean toString = name.equals("toString");
    boolean hashCode = name.equals("hashCode");
    return toString || hashCode;
  }
}

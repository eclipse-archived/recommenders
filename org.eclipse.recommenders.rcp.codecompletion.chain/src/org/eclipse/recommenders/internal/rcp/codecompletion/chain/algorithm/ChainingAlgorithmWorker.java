/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;

import com.ibm.wala.classLoader.ArrayClass;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeReference;

@SuppressWarnings("restriction")
public class ChainingAlgorithmWorker implements Callable<Void> {

  private IChainElement workingElement;

  private final List<Tuple<IClass, Integer>> expectedTypeList;

  private final ChainingAlgorithm internalProposalStore;

  private final IClass receiverType;

  public ChainingAlgorithmWorker(ChainingAlgorithm chainingAlgorithm, List<Tuple<IClass, Integer>> expectedTypeList,
      IClass receiverType) {
    this.receiverType = receiverType;
    this.workingElement = null;
    this.internalProposalStore = chainingAlgorithm;
    this.expectedTypeList = expectedTypeList;
  }

  private void inspectType() throws JavaModelException {
    this.workingElement = internalProposalStore.getWorkingElement();
    final IClass typeToCheck = workingElement.getType();
    // check type if searched type --> store for proposal
    if (storeForProposal(typeToCheck)) {
      return;
    }
    // check fields of type --> store in search map && and create new worker
    processCheckingAndStoring(typeToCheck);
  }

  private void processCheckingAndStoring(final IClass typeToCheck) throws JavaModelException {
    if (!typeToCheck.isArrayClass()) {
      final List<IChainElement> list = computeFields(typeToCheck);
      // check methods of type --> store in search map && and create new
      // worker
      list.addAll(computeMethods(typeToCheck));
      ChainingAlgorithm.getSearchMap().put(typeToCheck, list);
    } else {
      handleArrayType(typeToCheck);
    }
  }

  private void handleArrayType(final IClass typeToCheck) throws JavaModelException {
    final ArrayClass arrayTypeToCheck = (ArrayClass) typeToCheck;
    final IClass classOfArray = arrayTypeToCheck.getInnermostElementClass();
    if (classOfArray == null) {
      final TypeReference elementType = arrayTypeToCheck.getReference().getArrayElementType();
      if (elementType.isPrimitiveType()) {
        return;
      }
    }
    final List<IChainElement> list = computeFields(classOfArray);
    // check methods of type --> store in search map && and create new
    // worker
    list.addAll(computeMethods(classOfArray));
    ChainingAlgorithm.getSearchMap().put(typeToCheck, list);
  }

  /*
   * computes all methods from type to check
   */
  private List<IChainElement> computeMethods(final IClass typeToCheck) throws JavaModelException {
    final List<IChainElement> list = new ArrayList<IChainElement>();
    for (final IMethod m : typeToCheck.getAllMethods()) {
      final IChainElement result = createMethodWorker(m);
      if (result != null) {
        list.add(result);
        ChainingAlgorithm.getStoreElementList().add(result);
      }
    }
    return list;
  }

  /*
   * computes all fields from type to check
   */
  private List<IChainElement> computeFields(final IClass typeToCheck) throws JavaModelException {
    final List<IChainElement> list = new ArrayList<IChainElement>();
    for (final IField f : typeToCheck.getAllFields()) {
      final IChainElement result = createFieldWorker(typeToCheck, f);
      if (result != null) {
        list.add(result);
      }
    }
    return list;
  }

  /*
   * if the chain has to be extended by a method
   */
  private IChainElement createMethodWorker(final IMethod m) throws JavaModelException {
    if (checkVisibility(m)) {
      // XXX: Case: If calling context is subtype of typeToCheck than
      // field/method can be protected or package private

      if (m.getReturnType().isPrimitiveType() || m.getName().toString().equals("toString")) {
        return null;// return
      }
      MethodChainElement methodChainElement = new MethodChainElement(m, workingElement.getChainDepth() + 1);

      for (IChainElement element : ChainingAlgorithm.getStoreElementList()) {
        if (element.getCompletion().equals(methodChainElement.getCompletion())) {
          if (!element.previousElements().contains(workingElement)) {
            element.addPrevoiusElement(workingElement);
          }
          return null;
        }
      }
      methodChainElement.addPrevoiusElement(workingElement);
      storeListToProposalStore(methodChainElement);
      // ChainingAlgorithm.boxPrimitiveTyp(m.getDeclaringClass(),
      // m.getReturnType().getName().toString().toCharArray());
      return methodChainElement;
    }
    return null;
  }

  /*
   * if the chain has to be extended by a field
   */
  private IChainElement createFieldWorker(final IClass typeToCheck, final IField f) throws JavaModelException {
    if (checkVisibility(f)) {
      FieldChainElement fieldChainElement = new FieldChainElement(f, workingElement.getChainDepth() + 1);
      if (f.getFieldTypeReference().isPrimitiveType()) {
        return null;
      }

      for (IChainElement element : ChainingAlgorithm.getStoreElementList()) {
        if (element.getCompletion().equals(fieldChainElement.getCompletion())) {
          element.addPrevoiusElement(workingElement);
          return null;
        }
      }
      fieldChainElement.addPrevoiusElement(workingElement);
      storeListToProposalStore(fieldChainElement);
      return fieldChainElement;
    }
    return null;
  }

  private boolean checkVisibility(final IMember m) {
    if (m.isPublic()) {
      return m.isPublic();
    } else if (m.isPrivate()) {
      return receiverType.equals(m.getDeclaringClass());
    } else if (m.isProtected()) {
      try {
        boolean defaultVisibility = receiverType.getName().getPackage()
            .equals(m.getDeclaringClass().getName().getPackage());
        boolean subtypeVisibility = InheritanceHierarchyCache.isSubtype(m.getDeclaringClass(), receiverType, 0);
        return defaultVisibility || subtypeVisibility;
      } catch (JavaModelException e) {
        return false;
      }
    } else {
      // seems to be default
      return receiverType.getName().getPackage().equals(m.getDeclaringClass().getName().getPackage());
    }
  }

  private void storeListToProposalStore(final IChainElement element) {
    if (element.getChainDepth() + 1 > Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
      return;
    }
    // if (!ChainingAlgorithm.getSearchMap().containsKey(element.getType())) {
    internalProposalStore.addWorkingElement(element);
    // }
  }

  /*
   * function to check if the call chain meets the expectations, so that it can
   * be processed for proposal computation
   */
  private boolean storeForProposal(final IClass typeToCheck) throws JavaModelException {
    if (typeToCheck == null) {
      return true;
    }
    for (Tuple<IClass, Integer> expectedType : expectedTypeList) {
      final int testResult = InheritanceHierarchyCache.equalityTest(typeToCheck, expectedType.getFirst(),
          expectedType.getSecond());
      // if both types equal
      if ((testResult & InheritanceHierarchyCache.RESULT_EQUAL) > 0) {
        internalProposalStore.storeLastChainElementForProposal(workingElement, expectedType.getFirst(),
            expectedType.getSecond(), null);
        continue;

      }
      // if typeToCheck is primitive return
      if ((testResult & InheritanceHierarchyCache.RESULT_PRIMITIVE) > 0) {
        continue;
      }

      // Consult type hierarchy for sub-/supertypes
      if (InheritanceHierarchyCache.isSubtype(typeToCheck, expectedType.getFirst(), expectedType.getSecond())
          && !((testResult & InheritanceHierarchyCache.RESULT_EQUAL) > 0)) {
        internalProposalStore.storeLastChainElementForProposal(workingElement, expectedType.getFirst(),
            expectedType.getSecond(), expectedType.getFirst());
        continue;
      }
      /* else */
      if (InheritanceHierarchyCache.isSupertype(typeToCheck, expectedType.getFirst(), expectedType.getSecond())
          && !((testResult & InheritanceHierarchyCache.RESULT_EQUAL) > 0)) {
        internalProposalStore.storeLastChainElementForProposal(workingElement, expectedType.getFirst(),
            expectedType.getSecond(), null);
        continue;
      }
    }
    // not equal, not in a hierarchical relation, not primitive
    return false;
  }

  @Override
  public Void call() throws Exception {
    try {
      while (!internalProposalStore.isCanceled()) {
        internalProposalStore.notifyThreadWorking();
        inspectType();
        internalProposalStore.notifyThreadPausing();
        tryTerminateExecutor();
      }
    } catch (final JavaModelException e) {
      JavaPlugin.log(e);
    }
    return null;
  }

  /*
   * when ever one chain is finished the terminator should be tried to be
   * terminated. The reason is, that the terminator only exits, if the max. run
   * time is reached. But we want to have the optimal run time.
   */
  private void tryTerminateExecutor() {
    if (internalProposalStore.getWorkingElementsSize() == 0 && internalProposalStore.getCountWorkingThreads() == 0) {
      internalProposalStore.shutDownExecutor();
    }
  }

}

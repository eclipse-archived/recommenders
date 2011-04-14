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
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainElement.ChainElementType;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;

@SuppressWarnings("restriction")
public class ChainingAlgorithmWorker implements Callable<Void> {

  private IChainElement workingElement;

  private final List<Tuple<IClass, Integer>> expectedTypeList;

  private final ChainingAlgorithm internalProposalStore;

  private final IClass receiverType;

  public ChainingAlgorithmWorker(final ChainingAlgorithm chainingAlgorithm,
      final List<Tuple<IClass, Integer>> expectedTypeList, final IClass receiverType) {
    this.receiverType = receiverType;
    this.workingElement = null;
    this.internalProposalStore = chainingAlgorithm;
    this.expectedTypeList = expectedTypeList;
  }

  private void inspectType() throws JavaModelException {
    this.workingElement = internalProposalStore.getWorkingElement();
    if (workingElement.getType() == null || internalProposalStore.containsWorkingElement(workingElement)) {
      return;
    }
    if (workingElement.getChainDepth() >= Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
      internalProposalStore.shutDownExecutor();
    }
    // check type if searched type --> store for proposal
    if (storeForProposal()) {
      return;
    }

    // check fields of type --> store in search map
    processCheckingAndStoring();
  }

  private void processCheckingAndStoring() throws JavaModelException {
    final IClass typeToCheck = workingElement.getType();
    if (ChainingAlgorithm.getSearchMap().containsKey(typeToCheck)) {
      final List<IChainElement> list = ChainingAlgorithm.getSearchMap().get(typeToCheck);
      for (final IChainElement element : list) {
        refreshMember(element);
      }
    } else {
      final List<IChainElement> list = computeFields(typeToCheck);
      list.addAll(computeMethods(typeToCheck));
      ChainingAlgorithm.getSearchMap().put(typeToCheck, list);
    }
  }

  /*
   * computes all methods from type to check
   */
  private List<IChainElement> computeMethods(final IClass typeToCheck) throws JavaModelException {
    final List<IChainElement> list = new ArrayList<IChainElement>();
    for (final IMethod m : typeToCheck.getAllMethods()) {
      if (m.getName().toString().equals(workingElement.getCompletion())
          && workingElement.getElementType().equals(ChainElementType.METHOD) || m.isStatic()) {
        continue;
      }
      final IChainElement result = createMethodWorker(m);
      if (result != null) {
        list.add(result);
        ChainingAlgorithm.getGraph().add(result);
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
      if (f.getName().toString().equals(workingElement.getCompletion())
          && workingElement.getElementType().equals(ChainElementType.FIELD) || f.isStatic()) {
        continue;
      }
      final IChainElement result = createFieldWorker(typeToCheck, f);
      if (result != null) {
        list.add(result);
        ChainingAlgorithm.getGraph().add(result);
      }
    }
    return list;
  }

  /*
   * if the chain has to be extended by a method
   */
  private IChainElement createMethodWorker(final IMethod m) throws JavaModelException {
    if (ChainCompletionContext.unwantedMethodNames(m.getName().toString())) {
      return null;// return
    }

    if (checkVisibility(m)) {
      final MethodChainElement methodChainElement = new MethodChainElement(m, workingElement.getChainDepth() + 1);
      if (methodChainElement.getType() == null || !refreshMember(methodChainElement)) {
        return null;
      }
      methodChainElement.addPrevoiusElement(workingElement);
      storeListToProposalStore(methodChainElement);
      return methodChainElement;
    }
    return null;
  }

  private boolean refreshMember(final IChainElement chainElement) {
    for (final IChainElement element : ChainingAlgorithm.getGraph()) {
      // same element in store?
      if (element.getCompletion().equals(chainElement.getCompletion())
          && element.getElementType().equals(chainElement.getElementType())
          && element.getType().equals(chainElement.getType()) && element.isPrimitive() == chainElement.isPrimitive()
          && element.getArrayDimension().equals(chainElement.getArrayDimension())) {
        // check for redundancy
        if (!element.previousElements().contains(workingElement)) {
          element.addPrevoiusElement(workingElement);
        }
        // if (!element.isRootElement() && chainElement.isRootElement()) {
        // element.setRootElement(true);
        // }
        return false;
      }
    }
    return true;
  }

  /*
   * if the chain has to be extended by a field
   */
  private IChainElement createFieldWorker(final IClass typeToCheck, final IField f) throws JavaModelException {
    if (ChainCompletionContext.unwantedFieldNames(f.getName().toString())) {
      return null;// return
    }

    if (checkVisibility(f)) {
      final FieldChainElement fieldChainElement = new FieldChainElement(f, workingElement.getChainDepth() + 1);

      if (!refreshMember(fieldChainElement)) {
        return null;
      }
      fieldChainElement.addPrevoiusElement(workingElement);
      storeListToProposalStore(fieldChainElement);
      return fieldChainElement;
    }
    return null;
  }

  private boolean checkVisibility(final IMember m) {
    if (m.getName().toString().contains("$")) {
      return false;
    }
    if (m.isPublic()) {
      return m.isPublic();
    } else if (m.isPrivate()) {
      return receiverType.equals(m.getDeclaringClass());
    } else if (m.isProtected()) {
      try {
        final boolean defaultVisibility = receiverType.getName().getPackage()
            .equals(m.getDeclaringClass().getName().getPackage());
        final boolean subtypeVisibility = InheritanceHierarchyCache.isSubtype(m.getDeclaringClass(), receiverType, 0);
        return defaultVisibility || subtypeVisibility;
      } catch (final JavaModelException e) {
        return false;
      }
    } else {
      // seems to be default
      return receiverType.getName().getPackage().equals(m.getDeclaringClass().getName().getPackage());
    }
  }

  private void storeListToProposalStore(final IChainElement element) {
    // if (element.getChainDepth() + 1 >
    // Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
    // return;
    // }
    if (!internalProposalStore.containsWorkingElement(element)) {
      internalProposalStore.addWorkingElement(element);
    }
  }

  /*
   * function to check if the call chain meets the expectations, so that it can
   * be processed for proposal computation
   */
  private boolean storeForProposal() throws JavaModelException {
    for (final Tuple<IClass, Integer> expectedType : expectedTypeList) {
      final int testResult = InheritanceHierarchyCache.equalityTest(workingElement.getType(),
          workingElement.getArrayDimension(), expectedType.getFirst(), expectedType.getSecond());
      // if both types equal
      if ((testResult & InheritanceHierarchyCache.RESULT_EQUAL) > 0) {
        internalProposalStore.storeLastChainElementForProposal(workingElement, expectedType.getFirst(),
            expectedType.getSecond(), null);
        return true;
      }
      // if typeToCheck is primitive return
      if ((testResult & InheritanceHierarchyCache.RESULT_PRIMITIVE) > 0) {
        return true;
      }

      if (InheritanceHierarchyCache.isSupertype(workingElement.getType(), expectedType.getFirst(),
          expectedType.getSecond())
          && !((testResult & InheritanceHierarchyCache.RESULT_EQUAL) > 0)) {
        internalProposalStore.storeLastChainElementForProposal(workingElement, expectedType.getFirst(),
            expectedType.getSecond(), null);
        return true;
      }
    }
    // not equal, not in a hierarchical relation, not primitive
    return false;
  }

  @Override
  public Void call() throws Exception {
    try {
      while (!internalProposalStore.isCanceled()) {
        final long i = System.currentTimeMillis();
        internalProposalStore.notifyThreadWorking();
        inspectType();
        internalProposalStore.notifyThreadPausing();
        tryTerminateExecutor();
        // System.out.println(workingElement.getCompletion() + " " +
        // workingElement.getChainDepth() + ": "
        // + (System.currentTimeMillis() - i) + " left: " +
        // internalProposalStore.getWorkingElementsSize());
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

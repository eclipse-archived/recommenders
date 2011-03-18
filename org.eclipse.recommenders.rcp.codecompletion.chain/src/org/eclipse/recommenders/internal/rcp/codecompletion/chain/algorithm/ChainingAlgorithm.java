/**
 * Copyright (c) 2010 Gary Fritz, Andreas Kaluza, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gary Fritz - initial API and implementation.
 *    Andreas Kaluza - modified implementation to use WALA
 *    Marcel Bruch - moved implementation to use recommenders' code completion context
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.commons.utils.Triple;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.ChainTemplateProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.ProposalNameGenerator;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.ibm.wala.classLoader.IClass;

@SuppressWarnings("restriction")
public class ChainingAlgorithm {

  private class PriorityComparator implements Comparator<IChainElement> {

    @Override
    public int compare(final IChainElement arg0, final IChainElement arg1) {
      return Integer.valueOf(arg0.getChainDepth().compareTo(arg1.getChainDepth()));
    }

  }

  // iteration queue
  private final PriorityBlockingQueue<IChainElement> workingElement;

  private List<Tuple<IClass, Integer>> expectedTypeList;

  // Output
  private final List<ChainTemplateProposal> proposals;

  // leads to output
  // lastElement, List<Tuple<expectedTyp, expectedTypeDimension, casting class>>
  private final Map<IChainElement, List<Triple<IClass, Integer, IClass>>> lastChainElementForProposal;

  private ExecutorService executor;

  private final IClassHierarchyService walaService;

  private ChainCompletionContext ctx;

  private static Map<IClass, List<IChainElement>> searchMap;

  private static List<IChainElement> storeElementList;

  private final JavaElementResolver javaelementResolver;

  private volatile AtomicInteger countWorkingThreads = new AtomicInteger(0);

  private IClass receiverType;

  public ChainingAlgorithm() {
    expectedTypeList = null;
    workingElement = new PriorityBlockingQueue<IChainElement>(100, new PriorityComparator());
    proposals = Collections.synchronizedList(new ArrayList<ChainTemplateProposal>());
    walaService = InjectionService.getInstance().requestInstance(IClassHierarchyService.class);
    javaelementResolver = InjectionService.getInstance().requestInstance(JavaElementResolver.class);
    searchMap = Collections.synchronizedMap(new HashMap<IClass, List<IChainElement>>());
    storeElementList = Collections.synchronizedList(new ArrayList<IChainElement>());
    lastChainElementForProposal = Collections
        .synchronizedMap(new HashMap<IChainElement, List<Triple<IClass, Integer, IClass>>>());
  }

  public void execute(final IIntelligentCompletionContext ictx) throws JavaModelException {
    if (!ictx.expectsReturnValue()) {
      return;
    }
    initializeChainCompletionContext(ictx);
    processMembers();
    waitForThreadPoolTermination();
    computeProposalChains();
  }

  private void computeProposalChains() {
    // for each result element type
    for (Entry<IChainElement, List<Triple<IClass, Integer, IClass>>> lastElement : lastChainElementForProposal
        .entrySet()) {
      // for each expected type
      List<LinkedList<IChainElement>> resultChains = computeLastChainsElementForProposal(lastElement.getKey());
      for (Triple<IClass, Integer, IClass> expectedTypeAndCast : lastElement.getValue()) {
        if (expectedTypeAndCast.getThird() == null) {
          this.addNonCastedProposal(resultChains, expectedTypeAndCast.getFirst(), expectedTypeAndCast.getSecond());
        } else {
          this.addCastedProposal(resultChains, expectedTypeAndCast.getFirst(), expectedTypeAndCast.getSecond());
        }
      }
    }
  }

  private List<LinkedList<IChainElement>> computeLastChainsElementForProposal(IChainElement workingElement) {
    List<LinkedList<IChainElement>> resultChains = new ArrayList<LinkedList<IChainElement>>();
    LinkedList<IChainElement> chain = new LinkedList<IChainElement>();
    chain.add(workingElement);
    resultChains.add(chain);
    resultChains = computeProposalChainsForLastElement(resultChains);
    return resultChains;
  }

  private List<LinkedList<IChainElement>> computeProposalChainsForLastElement(
      List<LinkedList<IChainElement>> resultChains) {
    for (int i = Constants.AlgorithmSettings.MAX_CHAIN_DEPTH; i >= Constants.AlgorithmSettings.MIN_CHAIN_DEPTH; i--) {
      List<LinkedList<IChainElement>> tempChains = new ArrayList<LinkedList<IChainElement>>();
      for (LinkedList<IChainElement> list : resultChains) {
        IChainElement firstListElement = list.getFirst();
        if (firstListElement.isRootElement() && list.size() >= Constants.AlgorithmSettings.MIN_CHAIN_DEPTH) {
          tempChains.add(list);
        }
        List<IChainElement> elements = firstListElement.previousElements();
        for (IChainElement element : elements) {
          if (!(element.getChainDepth() <= i) || i == Constants.AlgorithmSettings.MIN_CHAIN_DEPTH
              && !element.isRootElement()) {
            continue;
          }
          LinkedList<IChainElement> linkedList = new LinkedList<IChainElement>(list);
          if (checkRedundance(list, element)) {
            continue;
          }
          linkedList.add(0, element);
          tempChains.add(linkedList);
        }
      }
      ArrayList<LinkedList<IChainElement>> list = new ArrayList<LinkedList<IChainElement>>(tempChains);
      resultChains = list;

    }
    return resultChains;
  }

  private boolean checkRedundance(LinkedList<IChainElement> list, IChainElement element) {
    for (IChainElement e : list) {
      if (e.getCompletion().equals(element.getCompletion())) {
        return true;
      }
    }
    return false;
  }

  private void initializeChainCompletionContext(final IIntelligentCompletionContext ictx) {
    ctx = new ChainCompletionContext(ictx, javaelementResolver, walaService);
    expectedTypeList = ctx.getExpectedTypeList();
    receiverType = ctx.getRevieverType();
  }

  private void processMembers() throws JavaModelException {
    ProposalNameGenerator.resetProposalNameGenerator();
    processInitialFields();
    processLocalVariables();
    processMethods();
  }

  private void waitForThreadPoolTermination() {
    if (workingElement.size() > 0) {
      executor = Executors.newFixedThreadPool(1);// Runtime.getRuntime().availableProcessors()
      try {
        executor.invokeAll(Collections.nCopies(1, new ChainingAlgorithmWorker(this, expectedTypeList, receiverType)),
            Constants.AlgorithmSettings.EXECUTOR_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS);
        executor.awaitTermination(Constants.AlgorithmSettings.EXECUTOR_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS);
        executor.shutdownNow();
      } catch (final InterruptedException e) {
        JavaPlugin.log(e);
      }
    }
  }

  private void processMethods() throws JavaModelException {
    for (final IChainElement methodProposal : ctx.getProposedMethods()) {
      storeElementList.add(methodProposal);
      workingElement.add(methodProposal);
    }
  }

  private void processLocalVariables() throws JavaModelException {
    for (final IChainElement variableProposal : ctx.getProposedVariables()) {
      storeElementList.add(variableProposal);
      workingElement.add(variableProposal);
      ProposalNameGenerator.markVariableNameAsUsed(variableProposal.getCompletion());
    }
  }

  private void processInitialFields() throws JavaModelException {
    for (final IChainElement fieldProposal : ctx.getProposedFields()) {
      storeElementList.add(fieldProposal);
      workingElement.add(fieldProposal);
      ProposalNameGenerator.markVariableNameAsUsed(fieldProposal.getCompletion());
    }
  }

  public void storeLastChainElementForProposal(IChainElement element, IClass expectedType,
      Integer expectedTypeDimension, IClass castingType) {
    if (!lastChainElementForProposal.containsKey(element)) {
      List<Triple<IClass, Integer, IClass>> list = lastChainElementForProposal.get(element);
      if (list == null) {
        list = new ArrayList<Triple<IClass, Integer, IClass>>();
      }
      list.add(Triple.create(expectedType, expectedTypeDimension, castingType));
      lastChainElementForProposal.put(element, list);

    }
  }

  private void addCastedProposal(final List<LinkedList<IChainElement>> workingChains, final IClass expectedType,
      Integer expectedTypeDimension) {
    addProposal(workingChains, expectedType, expectedTypeDimension, true);
  }

  private void addProposal(final List<LinkedList<IChainElement>> workingChains, final IClass expectedType,
      Integer expectedTypeDimension, boolean casting) {
    synchronized (proposals) {
      for (LinkedList<IChainElement> workingChain : workingChains) {
        ChainTemplateProposal chainTemplateProposal = new ChainTemplateProposal(workingChain, expectedType,
            expectedTypeDimension, casting);
        // for (ChainTemplateProposal proposal : proposals) {
        // if (chainTemplateProposal.equals(proposal)) {
        // return;
        // }
        // }
        proposals.add(chainTemplateProposal);
      }
    }
  }

  private void addNonCastedProposal(final List<LinkedList<IChainElement>> workingChains, IClass expectedType,
      Integer expectedTypeDimension) {
    addProposal(workingChains, expectedType, expectedTypeDimension, false);
  }

  public List<ChainTemplateProposal> getProposals() {
    return proposals;
  }

  public static Map<IClass, List<IChainElement>> getSearchMap() {
    return searchMap;
  }

  public boolean addWorkingElement(IChainElement element) {
    return workingElement.add(element);
  }

  public IChainElement getWorkingElement() {
    return workingElement.poll();
  }

  public void shutDownExecutor() {
    executor.shutdownNow();
  }

  public int getWorkingElementsSize() {
    return workingElement.size();
  }

  /**
   * This method should be called by worker threads before they are actively
   * processing a queue element.
   */
  public void notifyThreadWorking() {
    countWorkingThreads.getAndIncrement();
  }

  /**
   * This method should be called by worker threads after having processed a
   * queue element.
   */
  public void notifyThreadPausing() {
    countWorkingThreads.getAndDecrement();
  }

  /**
   * @return the number of threads currently working on a queue element
   */
  public int getCountWorkingThreads() {
    return countWorkingThreads.get();
  }

  public boolean isCanceled() {
    return executor.isShutdown();
  }

  public static List<IChainElement> getStoreElementList() {
    return storeElementList;
  }

}

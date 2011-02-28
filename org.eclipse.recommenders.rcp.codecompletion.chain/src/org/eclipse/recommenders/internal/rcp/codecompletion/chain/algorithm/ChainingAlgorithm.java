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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.ChainTemplateProposal;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMember;

@SuppressWarnings("restriction")
public class ChainingAlgorithm {

  private IClass expectedType;

  private final List<ChainTemplateProposal> proposals;

  private ThreadPoolExecutor executor;

  private final IClassHierarchyService walaService;

  private ChainCompletionContext ctx;

  private static Map<IClass, Map<IMember, IClass>> searchMap;

  private final JavaElementResolver javaelementResolver;

  public ChainingAlgorithm() {
    expectedType = null;

    proposals = Collections.synchronizedList(new LinkedList<ChainTemplateProposal>());
    walaService = InjectionService.getInstance().requestInstance(IClassHierarchyService.class);
    javaelementResolver = InjectionService.getInstance().requestInstance(JavaElementResolver.class);
    searchMap = Collections.synchronizedMap(new HashMap<IClass, Map<IMember, IClass>>());
  }

  public void execute(final IIntelligentCompletionContext ictx) throws JavaModelException {
    initializeChainCompletionContext(ictx);

    if (canComputeProposals()) {
      initializeThreadPool();
      processMembers();
      waitForThreadPoolTermination();
    }
  }

  private void initializeChainCompletionContext(final IIntelligentCompletionContext ictx) {
    ctx = new ChainCompletionContext(ictx, javaelementResolver, walaService);
    expectedType = ctx.getExpectedType();
  }

  private boolean canComputeProposals() {
    return expectedType != null;
  }

  private void initializeThreadPool() {

    executor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(),
        Constants.AlgorithmSettings.WORKER_KEEP_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS,
        new PriorityBlockingQueue<Runnable>(11, new Comparator<Runnable>() {

          // sort the workers according to their priority
          @Override
          public int compare(final Runnable o1, final Runnable o2) {
            return ((ChainingAlgorithmWorker) o1).getPriority() - ((ChainingAlgorithmWorker) o2).getPriority();
          }
        }));
    executor.allowCoreThreadTimeOut(true);
  }

  private void processMembers() throws JavaModelException {
    processInitialFields();
    processLocalVariables();
    processMethods();
  }

  private void waitForThreadPoolTermination() {
    if (executor.getTaskCount() > 0) {
      try {
        executor.awaitTermination(Constants.AlgorithmSettings.EXECUTOR_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS);
      } catch (final InterruptedException e) {
        JavaPlugin.log(e);
      }
    }
  }

  private void processMethods() throws JavaModelException {
    for (final IChainElement methodProposal : ctx.getProposedMethods()) {
      final LinkedList<IChainElement> proposalElementList = new LinkedList<IChainElement>();
      proposalElementList.add(methodProposal);
      executor.execute(new ChainingAlgorithmWorker(proposalElementList, 0, this, executor, expectedType));
    }
  }

  private void processLocalVariables() throws JavaModelException {
    for (final IChainElement variableProposal : ctx.getProposedVariables()) {
      final LinkedList<IChainElement> proposalElementList = new LinkedList<IChainElement>();
      proposalElementList.add(variableProposal);
      executor.execute(new ChainingAlgorithmWorker(proposalElementList, 0, this, executor, expectedType));
    }
  }

  private void processInitialFields() throws JavaModelException {
    for (final IChainElement fieldProposal : ctx.getProposedFields()) {
      final LinkedList<IChainElement> proposalElementList = new LinkedList<IChainElement>();
      proposalElementList.add(fieldProposal);
      executor.execute(new ChainingAlgorithmWorker(proposalElementList, 0, this, executor, expectedType));
    }
  }

  public void addCastedProposal(final LinkedList<IChainElement> workingChain, final IClass expectedType) {
    synchronized (proposals) {
      proposals.add(new ChainTemplateProposal(workingChain, expectedType));
    }
  }

  public void addProposal(final LinkedList<IChainElement> workingChain) {
    synchronized (proposals) {
      proposals.add(new ChainTemplateProposal(workingChain));
    }
  }

  public List<ChainTemplateProposal> getProposals() {
    return proposals;
  }

  public static void setSearchMap(final Map<IClass, Map<IMember, IClass>> searchMap) {
    ChainingAlgorithm.searchMap = searchMap;
  }

  public static Map<IClass, Map<IMember, IClass>> getSearchMap() {
    return searchMap;
  }
}

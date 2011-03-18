/**
 * Copyright (c) 2010 Gary Fritz, and Andreas Kaluza.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gary Fritz - initial API and implementation.
 *    Andreas Kaluza - modified implementation to use WALA 
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.ChainingAlgorithm;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IntelligentCompletionContextResolver;

import com.google.inject.Inject;

/**
 * This is the default implementation of the plug-in's
 * {@link IJavaCompletionProposalComputer} interface
 */
@SuppressWarnings({ "restriction" })
public class ChainCompletionProposalComputer implements IJavaCompletionProposalComputer {

  private JavaContentAssistInvocationContext jCtx;
  private StopWatch sw;
  private final IntelligentCompletionContextResolver contextResolver;
  private IIntelligentCompletionContext iCtx;

  @Inject
  public ChainCompletionProposalComputer(final IntelligentCompletionContextResolver contextResolver) {
    this.contextResolver = contextResolver;
  }

  /**
   * Executes the default chaining algorithm and returns a list of template
   * proposals
   */
  @SuppressWarnings("rawtypes")
  @Override
  public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
    long i = System.currentTimeMillis();
    if (context instanceof JavaContentAssistInvocationContext) {
      jCtx = (JavaContentAssistInvocationContext) context;
      if (contextResolver.hasProjectRecommendersNature(jCtx)) {
        iCtx = contextResolver.resolveContext(jCtx);
        List<IJavaCompletionProposal> doComputeCompletionProposals = doComputeCompletionProposals();
        System.out.println("Benchmark: " + (System.currentTimeMillis() - i));
        return doComputeCompletionProposals;
      }
    }
    return Collections.emptyList();
  }

  private List<IJavaCompletionProposal> doComputeCompletionProposals() {
    try {
      final ChainingAlgorithm algorithm = new ChainingAlgorithm();
      executeAlgorithm(algorithm);
      return computeProposals(algorithm);
    } catch (final Exception e) {
      JavaPlugin.log(e);
      return Collections.emptyList();
    }
  }

  private void executeAlgorithm(final ChainingAlgorithm algorithm) throws JavaModelException {
    sw = new StopWatch();
    sw.start();
    algorithm.execute(iCtx);
    sw.stop();
  }

  private List<IJavaCompletionProposal> computeProposals(final ChainingAlgorithm algorithm) {
    final ChainTemplateProposalGenerator chainProposalGenerator = new ChainTemplateProposalGenerator();
    return chainProposalGenerator.generateJavaCompletionProposals(algorithm.getProposals(), jCtx, sw.getTime());
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List computeContextInformation(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
    return Collections.emptyList();
  }

  @Override
  public String getErrorMessage() {
    return null;
  }

  @Override
  public void sessionStarted() {
  }

  @Override
  public void sessionEnded() {
  }
}

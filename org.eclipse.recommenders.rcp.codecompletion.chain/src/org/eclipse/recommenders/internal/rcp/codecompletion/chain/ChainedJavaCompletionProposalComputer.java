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

import org.apache.commons.lang.time.StopWatch;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.ChainingAlgorithm;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.proposals.TemplateProposalEngine;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.util.LookupUtilJdt;

/**
 * This is the default implementation of the plug-in's
 * {@link IJavaCompletionProposalComputer} interface
 */
@SuppressWarnings({ "restriction" })
public class ChainedJavaCompletionProposalComputer implements IJavaCompletionProposalComputer {

  // REVIEW: ExtensionFactory:classname.
  // class="org.eclipse.recommenders.commons.injection.ExtensionFactory:org.eclipse.recommenders.internal.rcp.CodeElementsAdapterFactory">

  private JavaContentAssistInvocationContext jctx;
  private StopWatch sw;

  /**
   * Executes the default chaining algorithm and returns a list of templated
   * proposals
   */
  @SuppressWarnings("rawtypes")
  @Override
  public List computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
    if (context instanceof JavaContentAssistInvocationContext) {
      final ChainingAlgorithm algorithm = createAlgorithm(context);
      try {
        executeAlgorithm(algorithm);
        return computeProposals(algorithm);
      } catch (final Exception e) {
        JavaPlugin.log(e);
      }
    }
    return Collections.emptyList();
  }

  private ChainingAlgorithm createAlgorithm(final ContentAssistInvocationContext context) {
    jctx = (JavaContentAssistInvocationContext) context;
    LookupUtilJdt.setProject(jctx.getCompilationUnit().getJavaProject());
    final ChainingAlgorithm algorithm = new ChainingAlgorithm();
    return algorithm;
  }

  private void executeAlgorithm(final ChainingAlgorithm algorithm) throws JavaModelException {
    sw = new StopWatch();
    sw.start();
    algorithm.execute(jctx);
    sw.stop();
  }

  private List<IJavaCompletionProposal> computeProposals(final ChainingAlgorithm algorithm) {
    final TemplateProposalEngine templateProposalEngine = new TemplateProposalEngine();
    return templateProposalEngine.generateJavaCompletionProposals(algorithm.getProposals(), jctx, sw.getTime());
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

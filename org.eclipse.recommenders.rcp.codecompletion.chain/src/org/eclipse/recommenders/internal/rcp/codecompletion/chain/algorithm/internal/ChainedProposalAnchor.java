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
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal;

/**
 * Default implementation of {@link IChainedProposalPart}
 */
public class ChainedProposalAnchor {
  private final String completion;

  private final char signature[];

  private final char[][] parameterNames;

  /**
   * Constructs a proposal part, denoted by completion, signature and parameter
   * names
   * 
   * @param completion
   * @param signature
   */
  public ChainedProposalAnchor(final char completion[], final char signature[], final char[][] parameterNames) {
    this.completion = new String(completion);
    this.signature = signature;
    this.parameterNames = parameterNames;
  }

  /**
   * Constructs a proposal part, denoted by completion, signature and parameter
   * names
   * 
   * @param completion
   * @param signature
   */
  public ChainedProposalAnchor(final String completion, final char signature[], final char[][] parameterNames) {
    this.completion = completion;
    this.signature = signature;
    this.parameterNames = parameterNames;
  }

  public String getCompletion() {
    return completion;
  }

  public char[] getSignature() {
    return signature;
  }

  public char[][] getParameterNames() {
    return parameterNames;
  }
}

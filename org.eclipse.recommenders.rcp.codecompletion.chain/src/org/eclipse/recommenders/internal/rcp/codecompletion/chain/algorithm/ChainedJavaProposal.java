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
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.util.List;

import org.eclipse.recommenders.commons.utils.Checks;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeReference;

/**
 * Default implementation of {@link IChainedJavaProposal}
 * 
 */
public class ChainedJavaProposal {
  private final List<IChainWalaElement> proposedChain;

  TypeReference resultingType;

  private final IClass castingType;

  private final boolean needsCast;

  /**
   * Constucts a proposal without cast
   * 
   * @param proposedChain
   *          list of proposed chain elements (fields and/or methods)
   */
  public ChainedJavaProposal(final List<IChainWalaElement> proposedChain) {
    this(proposedChain, null);
  }

  /**
   * Constructs a proposal that needs an up-cast
   * 
   * Example <br\>
   * <br>
   * <code>
   * class A {
   *  public A findMe;
   * }
   * 
   * class B extends A {
   * 
   * public void method () {
   * B triggerEventHere = *;
   * }
   * 
   * }
   * </code> <br\>
   * If you trigger the completion at <b>*</b> the proposal is
   * <code>(B) findMe</code>
   * 
   * @param proposedChain
   *          list of proposed chain elements (fields and/or methods)
   * @param castingType
   *          type to up-cast the chain's last element's resulting type to
   */
  public ChainedJavaProposal(final List<IChainWalaElement> proposedChain, final IClass castingType) {
    Checks.ensureIsNotNull(proposedChain);
    Checks.ensureIsTrue(proposedChain.size() >= 1);
    this.proposedChain = proposedChain;
    resultingType = proposedChain.get(proposedChain.size() - 1).getResultingType();
    needsCast = castingType != null;
    this.castingType = castingType;
  }

  public List<IChainWalaElement> getProposedChain() {
    return proposedChain;
  }

  public TypeReference getResultingType() {
    return resultingType;
  }

  public boolean needsCast() {
    return needsCast;
  }

  public IClass getCastingType() {
    return castingType;
  }
}

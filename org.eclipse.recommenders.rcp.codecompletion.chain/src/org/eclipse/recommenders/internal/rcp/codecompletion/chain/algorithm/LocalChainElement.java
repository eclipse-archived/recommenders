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
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.ibm.wala.classLoader.IClass;

public class LocalChainElement implements IChainElement {
  private final String name;
  private final IClass type;
  private final Integer chainDepth;
  private Integer arrayDimension = 0;
  private final List<IChainElement> prevoiusElements;
  private boolean rootElement = false;
  private boolean isPrimitive = false;

  // private List<LinkedList<IChainElement>> proposalChains = new
  // ArrayList<LinkedList<IChainElement>>();

  public LocalChainElement(final String name, final IClass type, final Integer chainDepth) {
    this.name = name;
    this.type = type;
    this.chainDepth = chainDepth;
    prevoiusElements = new ArrayList<IChainElement>();
  }

  @Override
  public IClass getType() {
    return type;
  }

  @Override
  public ChainElementType getElementType() {
    return ChainElementType.LOCAL;
  }

  @Override
  public String getCompletion() {
    return name;
  }

  @Override
  public Integer getChainDepth() {
    return chainDepth;
  }

  @Override
  public Integer getArrayDimension() {
    return arrayDimension;
  }

  public void setArrayDimension(Integer arrayDimension) {
    this.arrayDimension = arrayDimension;
  }

  @Override
  public void addPrevoiusElement(IChainElement prevoius) {
    prevoiusElements.add(prevoius);

  }

  @Override
  public List<IChainElement> previousElements() {
    return prevoiusElements;
  }

  @Override
  public void setRootElement(boolean rootElement) {
    this.rootElement = rootElement;
  }

  @Override
  public boolean isRootElement() {
    return rootElement;
  }

  @Override
  public boolean isPrimitive() {
    // TODO Auto-generated method stub
    return isPrimitive;
  }

  @Override
  public void setPrimitive(boolean isPrimitive) {
    this.isPrimitive = isPrimitive;

  }

  @Override
  public boolean isStatic() {
    return false;
  }

  // @Override
  // public List<LinkedList<IChainElement>> constructProposalChains(int
  // currentChainLength) {
  // if (proposalChains.isEmpty()) {
  // System.out.println(this.getCompletion());
  // List<LinkedList<IChainElement>> descendingChains = new
  // ArrayList<LinkedList<IChainElement>>();
  // if (currentChainLength <= Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
  // for (IChainElement element : previousElements()) {
  // if (element.getCompletion() != this.getCompletion()) {
  // descendingChains.addAll(element.constructProposalChains(currentChainLength
  // + 1));
  // }
  // }
  // }
  //
  // if (!this.isStatic()) {
  // List<LinkedList<IChainElement>> temp = new
  // ArrayList<LinkedList<IChainElement>>();
  // for (LinkedList<IChainElement> descendingElement : descendingChains) {
  // IChainElement firstElement = descendingElement.getFirst();
  // if (!(firstElement.getChainDepth() <= this.getChainDepth())
  // || currentChainLength == Constants.AlgorithmSettings.MIN_CHAIN_DEPTH &&
  // !firstElement.isRootElement()
  // || firstElement.isPrimitive() || descendingElement.contains(this)) {
  // continue;
  // }
  // LinkedList<IChainElement> linkedList = new
  // LinkedList<IChainElement>(descendingElement);
  // linkedList.addLast(this);
  // temp.add(linkedList);
  // }
  // descendingChains = temp;
  // }
  //
  // if (descendingChains.isEmpty() && this.isRootElement()) {
  // LinkedList<IChainElement> list = new LinkedList<IChainElement>();
  // list.add(this);
  // descendingChains.add(list);
  // }
  // proposalChains = descendingChains;
  // return proposalChains;
  // }
  // return proposalChains;
  // }
}
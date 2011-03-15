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
import com.ibm.wala.types.TypeReference;

public class LocalChainElement implements IChainElement {
  private final String name;
  private final IClass type;
  private final Integer chainDepth;
  private Integer arrayDimension = 0;
  private final List<IChainElement> prevoiusElements;
  private boolean rootElement = false;

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
  public TypeReference getResultingType() {
    return type.getReference();
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
}
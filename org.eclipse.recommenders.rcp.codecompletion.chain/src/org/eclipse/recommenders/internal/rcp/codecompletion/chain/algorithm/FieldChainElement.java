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

import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;

/**
 * This class is part of the internal call chain. It represents a field for
 * which the completion and the field references are stored.
 */
@SuppressWarnings("restriction")
public class FieldChainElement implements IChainElement {
  private String completion;
  private final IClassHierarchy classHierarchy;
  private IClass type;
  private Integer chainDepth;
  private boolean thisQualifier = false;
  private Integer arrayDimension = 0;
  private final List<IChainElement> prevoiusElements;
  private boolean rootElement = false;
  private boolean isPrimitive = false;
  private final IField field;

  private List<LinkedList<IChainElement>> proposalChains = new ArrayList<LinkedList<IChainElement>>();

  public FieldChainElement(final IField field, final Integer chainDepth) {
    this.field = field;
    prevoiusElements = new ArrayList<IChainElement>();
    this.chainDepth = chainDepth;
    try {
      completion = field.getName().toUnicodeString();
    } catch (final UTFDataFormatException e) {
      completion = field.getName().toString();
      JavaPlugin.log(e);
    }
    TypeReference fieldReference = field.getFieldTypeReference();
    classHierarchy = field.getClassHierarchy();
    if (fieldReference.isPrimitiveType()) {
      type = ChainCompletionContext.boxPrimitive(fieldReference.getName().toString());
      arrayDimension = 0;
      setPrimitive(true);
    } else {
      if (fieldReference.isArrayType() && fieldReference.getInnermostElementType().isPrimitiveType()) {
        type = ChainCompletionContext.boxPrimitive(fieldReference.getInnermostElementType().getName().toString());
      } else {
        type = classHierarchy.lookupClass(fieldReference);
        type = type.getClassLoader().lookupClass(type.getReference().getInnermostElementType().getName());
      }
      arrayDimension = fieldReference.getDimensionality();
    }
  }

  @Override
  public ChainElementType getElementType() {
    return ChainElementType.FIELD;
  }

  @Override
  public String getCompletion() {
    return completion;
  }

  @Override
  public IClass getType() {
    return type;
  }

  @Override
  public Integer getChainDepth() {
    return chainDepth;
  }

  @Override
  public void setChainDepth(Integer chainDepth) {
    this.chainDepth = chainDepth;
  }

  public boolean hasThisQualifier() {
    return thisQualifier;
  }

  public void setThisQualifier(boolean thisQualifier) {
    this.thisQualifier = thisQualifier;
  }

  @Override
  public Integer getArrayDimension() {
    return arrayDimension;
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
    return isPrimitive;

  }

  @Override
  public void setPrimitive(boolean isPrimitive) {
    this.isPrimitive = isPrimitive;

  }

  @Override
  public boolean isStatic() {
    return field.isStatic();
  }

  @Override
  public List<LinkedList<IChainElement>> constructProposalChains(int currentChainLength) {
    if (proposalChains.isEmpty()) {
      // System.out.println(getCompletion() + " " + chainDepth);
      List<LinkedList<IChainElement>> descendingChains = new ArrayList<LinkedList<IChainElement>>();
      if (currentChainLength < Constants.AlgorithmSettings.MAX_CHAIN_DEPTH - 1
      /* && currentChainLength + 1 > Constants.AlgorithmSettings.MIN_CHAIN_DEPTH */) {
        for (IChainElement element : previousElements()) {
          if (element.getCompletion() != this.getCompletion()) {
            descendingChains.addAll(element.constructProposalChains(currentChainLength + 1));
          }
        }
      }
      if (proposalChains.isEmpty()) {
        List<LinkedList<IChainElement>> temp = new ArrayList<LinkedList<IChainElement>>();
        for (LinkedList<IChainElement> descendingElement : descendingChains) {
          IChainElement lastDescendingElement = descendingElement.getLast();
          if (!(lastDescendingElement.getChainDepth() <= this.getChainDepth())
              || (currentChainLength == Constants.AlgorithmSettings.MIN_CHAIN_DEPTH && !lastDescendingElement
                  .isRootElement()) || lastDescendingElement.isPrimitive() || descendingElement.contains(this)
              || descendingElement.size() >= Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
            continue;
          }
          LinkedList<IChainElement> linkedList = new LinkedList<IChainElement>(descendingElement);
          linkedList.addLast(this);
          temp.add(linkedList);
        }
        descendingChains = temp;

        if (descendingChains.isEmpty() && this.isRootElement()) {
          LinkedList<IChainElement> list = new LinkedList<IChainElement>();
          list.add(this);
          descendingChains.add(list);
        }
        proposalChains = descendingChains;
      }

      return proposalChains;
    }
    // List<LinkedList<IChainElement>> temp = new
    // ArrayList<LinkedList<IChainElement>>();
    // for (LinkedList<IChainElement> element : proposalChains) {
    // LinkedList<IChainElement> list = new LinkedList<IChainElement>(element);
    // if (!element.contains(this) || !element.getLast().isPrimitive()) {
    // element.addLast(this);
    // }
    // temp.add(list);
    // }
    // proposalChains = temp;
    return proposalChains;
  }

}

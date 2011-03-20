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

import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;

/**
 * This class is part of the internal call chain. It represents a method for
 * which the completion and all parameter name strings are stored. Additionally
 * the type references the resulting type and all parameter types are stored,
 * too.
 * 
 */
@SuppressWarnings("restriction")
public class MethodChainElement implements IChainElement {
  private String completion;
  private TypeReference[] parameterTypes;
  private String[] parameterNames;
  private Integer chainDepth;
  private Integer arrayDimension = 0;
  private final List<IChainElement> prevoiusElements;
  private boolean rootElement = false;
  private final IMethod method;
  private boolean isPrimitive = false;
  private IClass type;

  private List<LinkedList<IChainElement>> proposalChains = new ArrayList<LinkedList<IChainElement>>();

  public MethodChainElement(final IMethod method, final Integer chainDepth) {
    this.method = method;
    prevoiusElements = new ArrayList<IChainElement>();
    this.chainDepth = chainDepth;
    try {
      completion = method.getName().toUnicodeString();
    } catch (final UTFDataFormatException e1) {
      completion = method.getName().toString();
    }
    try {
      final int parameterMinCount = getParameterMinCount(method);
      final TypeReference resultingType = method.getReturnType();
      final IClassHierarchy classHierarchy = method.getClassHierarchy();
      if (resultingType.isPrimitiveType()) {
        type = ChainCompletionContext.boxPrimitive(resultingType.getName().toString());
        arrayDimension = 0;
        setPrimitive(true);
      } else {
        if (resultingType.isArrayType() && resultingType.getInnermostElementType().isPrimitiveType()) {
          type = ChainCompletionContext.boxPrimitive(resultingType.getInnermostElementType().getName().toString());
        } else {
          type = classHierarchy.lookupClass(resultingType);
          if (type != null) {
            type = type.getClassLoader().lookupClass(type.getReference().getInnermostElementType().getName());
          }
        }
        arrayDimension = resultingType.getDimensionality();
      }
      computeParameterTypesAndNames(method, parameterMinCount);
    } catch (final Exception e) {
      parameterNames = new String[0];
      parameterTypes = new TypeReference[0];
      type = null;
      JavaPlugin.log(e);
    }
  }

  private void computeParameterTypesAndNames(final IMethod method, final int parameterMinCount) {
    parameterNames = new String[method.getNumberOfParameters() - parameterMinCount];
    parameterTypes = new TypeReference[method.getNumberOfParameters() - parameterMinCount];
    for (int i = parameterMinCount; i < method.getNumberOfParameters(); i++) {
      String name = null;
      if (!(method.isAbstract() || method.isNative())) {
        try {
          name = method.getLocalVariableName(0, i);
        } catch (final NullPointerException e) {
          // Andreas Kaluza: Marcel you said, you would like to look at this
          // particular code fragment. I have no clue how to solve this error.

          // this should never happen, but somehow it happens...
          // JavaPlugin.log(e); <-- there are to many exceptions to log
        }
      }
      if (name == null) {
        name = "arg" + Integer.toString(i - parameterMinCount);
      }
      parameterNames[i - parameterMinCount] = name;
      parameterTypes[i - parameterMinCount] = method.getParameterType(i);
    }
  }

  private int getParameterMinCount(final IMethod method) {
    int parameterMinCount = 0;
    if (!method.isStatic()) { // because 'this' parameter is stored as
                              // parameter type
      parameterMinCount = 1;
    }
    return parameterMinCount;
  }

  @Override
  public ChainElementType getElementType() {
    return ChainElementType.METHOD;
  }

  @Override
  public String getCompletion() {
    return completion;
  }

  /**
   * Returns array of types of formal parameters
   * 
   * @return array of type signatures
   */
  public TypeReference[] getParameterTypes() {
    return parameterTypes;
  }

  /**
   * Returns the names of formal parameters. Intension: if parameter names are
   * not known (e.g. in case of binary types or computation units), the names
   * are arg0, arg1, ...
   * 
   * @return array of names of formal parameters
   */
  public String[] getParameterNames() {
    return parameterNames;
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
  public void setChainDepth(final Integer chainDepth) {
    this.chainDepth = chainDepth;
  }

  @Override
  public Integer getArrayDimension() {
    return arrayDimension;
  }

  @Override
  public void addPrevoiusElement(final IChainElement prevoius) {
    prevoiusElements.add(prevoius);

  }

  @Override
  public List<IChainElement> previousElements() {
    return prevoiusElements;
  }

  @Override
  public void setRootElement(final boolean rootElement) {
    this.rootElement = rootElement;
  }

  @Override
  public boolean isRootElement() {
    return rootElement;
  }

  public IMethod getMethod() {
    return method;
  }

  @Override
  public boolean isPrimitive() {
    return isPrimitive;

  }

  @Override
  public void setPrimitive(final boolean isPrimitive) {
    this.isPrimitive = isPrimitive;

  }

  @Override
  public boolean isStatic() {
    return method.isStatic();
  }

  @Override
  public List<LinkedList<IChainElement>> constructProposalChains(final int currentChainLength) {
    if (proposalChains.isEmpty()) {
      // System.out.println(getCompletion() + " " + chainDepth);
      List<LinkedList<IChainElement>> descendingChains = new ArrayList<LinkedList<IChainElement>>();
      if (currentChainLength < Constants.AlgorithmSettings.MAX_CHAIN_DEPTH
      /* && currentChainLength + 1 > Constants.AlgorithmSettings.MIN_CHAIN_DEPTH */) {
        for (final IChainElement element : previousElements()) {
          if (element.getCompletion() != this.getCompletion()) {
            descendingChains.addAll(element.constructProposalChains(currentChainLength + 1));
          }
        }
      }

      if (proposalChains.isEmpty()) {
        final List<LinkedList<IChainElement>> temp = new ArrayList<LinkedList<IChainElement>>();
        for (final LinkedList<IChainElement> descendingElement : descendingChains) {
          final IChainElement lastDescendingElement = descendingElement.getLast();
          if (!(lastDescendingElement.getChainDepth() <= this.getChainDepth())
              || currentChainLength == Constants.AlgorithmSettings.MIN_CHAIN_DEPTH
              && !lastDescendingElement.isRootElement() || lastDescendingElement.isPrimitive()
              || descendingElement.contains(this)
              || descendingElement.size() >= Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
            continue;
          }
          final LinkedList<IChainElement> linkedList = new LinkedList<IChainElement>(descendingElement);
          linkedList.addLast(this);
          temp.add(linkedList);
        }
        descendingChains = temp;

        if (descendingChains.isEmpty() && this.isRootElement()) {
          final LinkedList<IChainElement> list = new LinkedList<IChainElement>();
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

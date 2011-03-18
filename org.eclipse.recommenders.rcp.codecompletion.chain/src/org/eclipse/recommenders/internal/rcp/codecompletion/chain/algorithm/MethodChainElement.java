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
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;

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

  private TypeReference resultingType;

  private String[] parameterNames;

  private final IClassHierarchy classHierarchy;

  private final Integer chainDepth;

  private Integer arrayDimension = 0;

  private final List<IChainElement> prevoiusElements;

  private boolean rootElement = false;

  private final IMethod method;

  public MethodChainElement(final IMethod method, final Integer chainDepth) {
    this.method = method;
    prevoiusElements = new ArrayList<IChainElement>();
    this.chainDepth = chainDepth;
    classHierarchy = method.getClassHierarchy();
    try {
      completion = method.getName().toUnicodeString();
    } catch (final UTFDataFormatException e1) {
      completion = method.getName().toString();
    }
    try {
      final int parameterMinCount = getParameterMinCount(method);
      resultingType = method.getReturnType();
      arrayDimension = resultingType.getDimensionality();
      computeParameterTypesAndNames(method, parameterMinCount);
    } catch (final Exception e) {
      parameterNames = new String[0];
      parameterTypes = new TypeReference[0];
      resultingType = null;
      JavaPlugin.log(e);
    }
  }

  private void computeParameterTypesAndNames(final IMethod method, final int parameterMinCount) {
    parameterNames = new String[method.getNumberOfParameters() - parameterMinCount];
    parameterTypes = new TypeReference[method.getNumberOfParameters() - parameterMinCount];
    for (int i = parameterMinCount; i < method.getNumberOfParameters(); i++) {
      String name = null;
      try {
        name = method.getLocalVariableName(0, i);
      } catch (final NullPointerException e) {
        // Andreas Kaluza: Marcel you said, you would like to look at this
        // particular code fragment. I have no clue how to solve this error.

        // this should never happen, but somehow it happens...
        // JavaPlugin.log(e); <-- there are to many exceptions to log
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

  @Override
  public TypeReference getResultingType() {
    return resultingType;
  }

  // @Override
  // public String getResultingTypeName() {
  // return resultingType.getName().toUnicodeString();
  // }

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
    if (getResultingType().isPrimitiveType()) {
      return null;
    }
    return classHierarchy.lookupClass(getResultingType());
  }

  @Override
  public Integer getChainDepth() {
    return chainDepth;
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

  public IMethod getMethod() {
    return method;
  }
}

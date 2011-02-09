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

import java.io.UTFDataFormatException;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainWalaElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.proposals.TemplateProposalEngine;

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
public class MethodChainWalaElement implements IChainWalaElement {
  private String completion;

  private TypeReference[] parameterTypes;

  private TypeReference resultingType;

  private String[] parameterNames;

  private final IClassHierarchy classHierarchy;

  @SuppressWarnings("restriction")
  public MethodChainWalaElement(final IMethod method) {
    classHierarchy = method.getClassHierarchy();
    try {
      completion = method.getName().toUnicodeString();
    } catch (final UTFDataFormatException e1) {
      completion = method.getName().toString();
    }
    try {
      final int parameterMinCount = getParameterMinCount(method);
      resultingType = method.getReturnType();
      computeParameterTypesAndNames(method, parameterMinCount);
    } catch (final Exception e) {
      parameterNames = new String[0];
      parameterTypes = new TypeReference[0];
      resultingType = null;
      JavaPlugin.log(e);
    }
  }

  @SuppressWarnings("restriction")
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

  @Override
  public String getResultingTypeName() {
    return resultingType.getName().toUnicodeString();
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
   * not known (e.g. in case of binary types or compulation units), the names
   * should be arg0, arg1, ... These names get used by
   * {@link TemplateProposalEngine#makeTemplatePartCode(IChainElement part)}
   * 
   * @return array of names of formal parameters
   */
  public String[] getParameterNames() {
    return parameterNames;
  }

  @Override
  public IClass getType() {
    if (getResultingType().isPrimitiveType())
      return null;
    return classHierarchy.lookupClass(getResultingType());
  }
}

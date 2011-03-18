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

import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.JavaPlugin;

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

  private final TypeReference fieldReference;

  private final IClassHierarchy classHierarchy;

  private IClass type;

  private final Integer chainDepth;

  private boolean thisQualifier = false;

  private Integer arrayDimension = 0;

  private final List<IChainElement> prevoiusElements;

  private boolean rootElement = false;

  public FieldChainElement(final IField field, final Integer chainDepth) {
    prevoiusElements = new ArrayList<IChainElement>();
    this.chainDepth = chainDepth;
    try {
      completion = field.getName().toUnicodeString();
    } catch (final UTFDataFormatException e) {
      completion = field.getName().toString();
      JavaPlugin.log(e);
    }
    fieldReference = field.getFieldTypeReference();
    classHierarchy = field.getClassHierarchy();
    if (fieldReference.isPrimitiveType()) {
      type = null;
    }
    type = classHierarchy.lookupClass(fieldReference);
    arrayDimension = fieldReference.getDimensionality();
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
  public TypeReference getResultingType() {
    return fieldReference;
  }

  @Override
  public IClass getType() {
    return type;
  }

  @Override
  public Integer getChainDepth() {
    return chainDepth;
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
}

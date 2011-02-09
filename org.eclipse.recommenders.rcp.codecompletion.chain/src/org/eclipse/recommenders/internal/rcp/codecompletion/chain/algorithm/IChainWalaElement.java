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

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeReference;

/**
 * Provides a door to extend this plug-in or to replace this plugin's default
 * implementation ({@link ChainingAlgorithm} ).
 * 
 */
public interface IChainWalaElement {
  /**
   * Type of element REVIEW: new information? skip this or make some important
   * points.
   */
  public enum ChainElementType {
    FIELD, METHOD
  };

  /**
   * Returns the type (field or method) of the element
   * 
   * @return on of the values of {@link ChainElementType}
   */
  public abstract ChainElementType getElementType();

  /**
   * The completion as string
   * 
   * @return the completion string
   */
  public abstract String getCompletion();

  /**
   * Returns the {@link TypeReference} this completion portion results in
   * 
   * @return the type reference this completion portion results in
   */
  public abstract TypeReference getResultingType();

  /**
   * Returns the name of the {@link IClass} this completion portion results in
   * 
   * @return name of the type this completion portion results in
   */
  public abstract String getResultingTypeName();

  /**
   * Returns the {@link IClass} this completion portion results in
   * 
   * @return the class this completion portion results in
   */
  public IClass getType();
}

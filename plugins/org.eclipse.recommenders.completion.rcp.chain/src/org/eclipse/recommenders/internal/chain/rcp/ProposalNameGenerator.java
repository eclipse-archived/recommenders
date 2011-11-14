/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.chain.rcp;

import java.util.HashSet;
import java.util.Set;

/**
 * The context in which a proposal chain is created. Particularly, this includes
 * used variables.
 * 
 */
public class ProposalNameGenerator {

  private static final Set<String> variableNames = new HashSet<String>();

  private static char nextFreeVariableName = 'i';

  private static int freeVariableSequenceCounter = 1;

  /**
   * Marks the specified variable name as "used" so that
   * {@link #variableNameIsFree(String)} will return false for this name.
   * 
   * @param name
   * @return true if the variable name was free
   */
  public static boolean markVariableNameAsUsed(final String name) {
    return variableNames.add(name);
  }

  /**
   * Generates a name for a new variable and marks this name as used.
   * 
   * @return a valid name for a new variable
   */
  public static String generateFreeVariableName() {
    while (true) {
      final String varName = getNextFreeVariableName();
      toggleFreeVariableName();
      if (!variableNames.contains(varName)) {
        return varName;
      }
    }
  }

  private static String getNextFreeVariableName() {
    final StringBuilder str = new StringBuilder(String.valueOf(nextFreeVariableName));
    if (freeVariableSequenceCounter > 1) {
      str.append(freeVariableSequenceCounter);
    }
    return str.toString();
  }

  private static void toggleFreeVariableName() {
    if (nextFreeVariableName < 'z') {
      nextFreeVariableName++;
    } else {
      nextFreeVariableName = 'i';
      freeVariableSequenceCounter++;
    }
  }

  public static void resetSequence() {
    nextFreeVariableName = 'i';
    freeVariableSequenceCounter = 1;
  }

  public static void resetProposalNameGenerator() {
    variableNames.clear();
    resetSequence();
  }
}

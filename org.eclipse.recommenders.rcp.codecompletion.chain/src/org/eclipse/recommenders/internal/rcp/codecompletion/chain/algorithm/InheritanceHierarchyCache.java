/**
 * Copyright (c) 2010 Andreas Kaluza.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.commons.analysis.utils.InheritanceUtils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeReference;

/**
 * Every wala lookup is done here. This class statically checks if two types are
 * in an sub or super type hierarchy and checks whether to types are equal or
 * not
 * 
 * XXX could be refactored to an util class
 */
public class InheritanceHierarchyCache {

  public static final int RESULT_PRIMITIVE = 0x2;

  public static final int RESULT_EQUAL = 0x4;

  /**
   * A cache for type hierarchies to avoid unnecessary computations
   */
  private static Map<IClass, List<IClass>> hierarchies = new HashMap<IClass, List<IClass>>();

  /**
   * Tests for supertype relation between two types in a (once created) type
   * hierarchy
   * 
   * @param context
   *          concrete type
   * @param supertype
   *          type to test to be a supertype or not
   * @return true, in case of supertype relation, else false
   * @throws JavaModelException
   */
  public static boolean isSupertype(final IClass context, final IClass supertype, Integer supertypeDimension) throws JavaModelException {
    List<IClass> superclasses = InheritanceHierarchyCache.hierarchies.get(context);
    if (superclasses == null) {
      superclasses = InheritanceUtils.getAllSuperclasses(context);
      superclasses.addAll(context.getAllImplementedInterfaces());
      for (final IClass clazz : superclasses) {
        if (isObject(clazz)) {
          superclasses.remove(clazz);
          break;
        }
      }
      synchronized (InheritanceHierarchyCache.hierarchies) {
        InheritanceHierarchyCache.hierarchies.put(context, superclasses);
      }
    }
    for (IClass clazz: superclasses) {
      if (clazz.getReference().getDimensionality() >= supertypeDimension) {
        if (clazz.getReference().getInnermostElementType().getName().equals(supertype.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Tests for subtype relation between two types in a (once created) type
   * hierarchy
   * 
   * @param context
   *          base type
   * @param subtype
   *          type to test to be a subtype or not
   * @param subTypeDimension 
   * @return true, in case of subtype relation, else false
   * @throws JavaModelException
   */
  public static boolean isSubtype(final IClass context, final IClass subtype, Integer subTypeDimension) throws JavaModelException {
    List<IClass> superclasses = InheritanceHierarchyCache.hierarchies.get(subtype);
    if (superclasses == null) {
      superclasses = InheritanceUtils.getAllSuperclasses(subtype);
      superclasses.addAll(subtype.getAllImplementedInterfaces());
      for (final IClass clazz : superclasses) {
        if (isObject(clazz)) {
          superclasses.remove(clazz);
          break;
        }
      }
      synchronized (InheritanceHierarchyCache.hierarchies) {
        InheritanceHierarchyCache.hierarchies.put(subtype, superclasses);
      }
    }
    if (context.isArrayClass()) {
      for (IClass clazz: superclasses) {
        if (context.getReference().getDimensionality() >= subTypeDimension) {
          if (clazz.getReference().getName().equals(context.getReference().getInnermostElementType().getName())) {
            return true;
          }
        }
      }
    }
    return superclasses.contains(context);
  }

  private static boolean isObject(final IClass clazz) {
    if (clazz.isArrayClass() && clazz.getReference().getInnermostElementType().getName().equals(TypeReference.JavaLangObject.getName())) {
      return true;
    } else {
      return clazz.getName().equals(TypeReference.JavaLangObject.getName());
    }
  }

  /**
   * Tests for type equality. Sets flag, if types are equivalent and at least
   * one of them is primitive
   * 
   * @param resultingType
   *          type 1
   * @param expectedType
   *          type 2
   * @return true, in case of equivalence, false, else
   */
  // XXX need to look closer on this... here happens magic :)
  // do not look at this... a magician never tells the trick ;)
  public static int equalityTest(final IClass resultingType, final IClass expectedType, Integer expectedTypeDimension) {
    TypeReference resultingReference = resultingType.getReference();
    TypeReference expectedReference = expectedType.getReference();
    int result = 0;
    if (resultingReference.isPrimitiveType()) {
      result |= InheritanceHierarchyCache.RESULT_PRIMITIVE;
    }
    if (resultingReference.getName().equals(expectedReference.getName())) {
      result |= InheritanceHierarchyCache.RESULT_EQUAL;
    } else if (resultingReference.getDimensionality() >= expectedTypeDimension){
      //array types
      if (resultingReference.getInnermostElementType().getName().equals(expectedReference.getName())){
        result |= InheritanceHierarchyCache.RESULT_EQUAL;
      }
    }

    // Types are not equal, but maybe they're related
    // i.e. one of them is in the type hierarchy of the other one
    return result;
  }
}

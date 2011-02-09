package org.eclipse.recommenders.internal.rcp.codecompletion.chain.util;

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
public class LookupUtilWala {

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
   * @param subtype
   *          type to test to be a supertype or not
   * @return true, in case of supertype relation, else false
   * @throws JavaModelException
   */
  public static boolean isSupertype(final IClass context, final IClass subtype) throws JavaModelException {
    List<IClass> superclasses = LookupUtilWala.hierarchies.get(context);
    if (superclasses == null) {
      superclasses = InheritanceUtils.getAllSuperclasses(context);
      superclasses.addAll(context.getAllImplementedInterfaces());
      for (final IClass clazz : superclasses) {
        if (clazz.getName().equals(TypeReference.JavaLangObject.getName())) {
          superclasses.remove(clazz);
          break;
        }
      }
      synchronized (LookupUtilWala.hierarchies) {
        LookupUtilWala.hierarchies.put(context, superclasses);
      }
    }
    return superclasses.contains(subtype);
  }

  /**
   * Tests for subtype relation between two types in a (once created) type
   * hierarchy
   * 
   * @param context
   *          base type
   * @param subtype
   *          type to test to be a subtype or not
   * @return true, in case of subtype relation, else false
   * @throws JavaModelException
   */
  public static boolean isSubtype(final IClass context, final IClass subtype) throws JavaModelException {
    List<IClass> superclasses = LookupUtilWala.hierarchies.get(subtype);
    if (superclasses == null) {
      superclasses = InheritanceUtils.getAllSuperclasses(subtype);
      superclasses.addAll(subtype.getAllImplementedInterfaces());
      for (final IClass clazz : superclasses) {
        if (clazz.getName().equals(TypeReference.JavaLangObject.getName())) {
          superclasses.remove(clazz);
          break;
        }
      }
      synchronized (LookupUtilWala.hierarchies) {
        LookupUtilWala.hierarchies.put(subtype, superclasses);
      }
    }
    return superclasses.contains(context);
  }

  /**
   * Tests for type equality. This method can cope with boxed/unboxed primitive
   * types. Sets flag, if types are equivalent and at least one of them is
   * primitive
   * 
   * @param resultingType
   *          type 1
   * @param expectedType
   *          type 2
   * @return true, in case of equivalence, false, else
   */
  // XXX need to look closer on this... here happens magic :)
  public static int equalityTest(final IClass resultingType, final IClass expectedType) {
    if (resultingType.getReference().isPrimitiveType() && resultingType.getReference().isPrimitiveType()) {
      if (resultingType.getReference().getName().equals(expectedType.getReference().getName()))
        return LookupUtilWala.RESULT_EQUAL | LookupUtilWala.RESULT_PRIMITIVE;
      else
        return LookupUtilWala.RESULT_PRIMITIVE;
    } else {
      if (resultingType.getReference().getName().equals(expectedType.getReference().getName()))
        return LookupUtilWala.RESULT_EQUAL;
    }
    // Types are not equal, but maybe they're related
    // i.e. one of them is in the type hierarchy of the other one
    return 0;
  }
}

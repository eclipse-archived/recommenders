package org.eclipse.recommenders.internal.rcp.codecompletion.chain.util;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.BinaryType;

/**
 * Every JDT lookup is done here. In this class there is an internal class which
 * mocks up the a primitive.
 * 
 * XXX Could be refactored to an util class.
 * 
 */
@SuppressWarnings("restriction")
public class LookupUtilJdt {

  private static IJavaProject project;

  private static final String JAVA_LANG_OBJECT = "java.lang.Object";

  /**
   * This class wraps a primitive type as {@link IType} so that it can be used
   * in completion detection
   */
  public static class PrimitiveType extends BinaryType {
    private static final IField NO_FIELDS[] = new IField[0];

    /**
     * This instance has no parent type
     * 
     * @param primitiveType
     *          type name
     */
    public PrimitiveType(final String primitiveType) {
      super(null, primitiveType);
    }

    /**
     * Returns no more than the simple type name
     */
    @Override
    public String getFullyQualifiedName() {
      return getElementName();
    }

    /**
     * A primitive type, of course, has no fields
     */
    @Override
    public IField[] getFields() throws JavaModelException {
      return PrimitiveType.NO_FIELDS;
    }

    /**
     * A primitive type, of course, has no children
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ArrayList getChildrenOfType(final int type) throws JavaModelException {
      return new ArrayList(Collections.EMPTY_LIST);
    }
  }

  /**
   * Informs whether given signature corresponds to a primitive type or an array
   * of primitive types
   * 
   * @param signature
   *          signature to evaluate
   * @return true for primitive types or arrays of one, else false
   */
  public static boolean isSignatureOfSimpleType(String signature) {
    signature = Signature.getElementType(signature);
    final String typesig = Signature.getSimpleName(signature);
    return typesig.length() == 1;
  }

  /**
   * Returns "int" for primitive integers ("I", "[I", ...), "float" for float
   * elements, ... The same for double, long, boolean and arrays of those.
   * 
   * @param typeSignature
   *          signature whose element type is to be tested
   * @return For primitive element types: lower-case string type name suitable
   *         for Java code, for other types the FQTN
   */
  public static String getPrimitiveTypeName(final char typeSignature[]) {
    final char sig[] = Signature.getElementType(typeSignature);
    return Signature.toString(String.valueOf(sig));
  }

  /**
   * Looks up an {@link IType} from a fully qualified signature rather than from
   * a context type
   * 
   * @param signature
   *          the signature for which the corresponding IType to be looked up
   * @return the IType, if found
   * @throws JavaModelException
   */
  public static IType lookupType(final char signature[]) throws JavaModelException {
    if (LookupUtilJdt.isSignatureOfGenericType(new String(signature)))
      return null;
    final String strSignature = new String(signature);
    if (LookupUtilJdt.isSignatureOfSimpleType(strSignature))
      return new LookupUtilJdt.PrimitiveType(Signature.toString(strSignature));
    final char signatureWithoutGenerics[] = Signature.getTypeErasure(signature);
    // Next the array notation will be deleted. Should it be supported?
    final char elementTypeSignature[] = Signature.getElementType(signatureWithoutGenerics);
    final char signatureQualifier[] = Signature.getSignatureQualifier(elementTypeSignature);
    final char signatureTypeName[] = Signature.getSignatureSimpleName(elementTypeSignature);
    final IType fullyQualifiedType = LookupUtilJdt.getProject().findType(new String(signatureQualifier),
        new String(signatureTypeName));
    return fullyQualifiedType;
  }

  /**
   * Informs whether given signature corresponds to a generic type or an array
   * of generic types
   * 
   * @param signature
   *          signature to evaluate
   * @return true for generic types or arrays of one, else false
   */
  private static boolean isSignatureOfGenericType(final String generic) {
    if ((generic.startsWith("[T") && (generic.length() == 4)) || (generic.startsWith("T") && (generic.length() == 3)))
      return true;
    return false;
  }

  /**
   * Used to end chaining algorithm on methods returning "void" of
   * "java.lang.Object"
   * 
   * @param resultingType
   *          return type to test
   * @return false, if algorithm shall stop, true to continue
   */
  public static boolean isWantedType(final IType resultingType) {
    final boolean ret = resultingType.getElementName().equals(Signature.toString(Signature.SIG_VOID))
        || resultingType.getFullyQualifiedName().equals(LookupUtilJdt.JAVA_LANG_OBJECT);
    return !ret;
  }

  public static void setProject(final IJavaProject JavaProject) {
    LookupUtilJdt.project = JavaProject;
  }

  public static IJavaProject getProject() {
    return LookupUtilJdt.project;
  }

}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.names;

import static org.eclipse.recommenders.utils.Checks.ensureIsInstanceOf;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;
import static org.eclipse.recommenders.utils.Throws.throwNotImplemented;
import static org.eclipse.recommenders.utils.names.VmTypeName.BOOLEAN;
import static org.eclipse.recommenders.utils.names.VmTypeName.BYTE;
import static org.eclipse.recommenders.utils.names.VmTypeName.CHAR;
import static org.eclipse.recommenders.utils.names.VmTypeName.DOUBLE;
import static org.eclipse.recommenders.utils.names.VmTypeName.FLOAT;
import static org.eclipse.recommenders.utils.names.VmTypeName.INT;
import static org.eclipse.recommenders.utils.names.VmTypeName.LONG;
import static org.eclipse.recommenders.utils.names.VmTypeName.SHORT;
import static org.eclipse.recommenders.utils.names.VmTypeName.VOID;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.collect.MapMaker;

public class VmMethodName implements IMethodName {
    private static final long serialVersionUID = 688964238062226061L;

    private static Map<String /* name */, VmMethodName> index = new MapMaker().weakValues().makeMap();

    public static synchronized VmMethodName get(final String vmFullQualifiedTypeName, final String vmMethodSignature) {
        return get(vmFullQualifiedTypeName + "." + vmMethodSignature);
    }

    /**
     * Creates a new {@link VmMethodName} from the given method argument but replaces the declaring type by the given
     * new base type.
     * <p>
     * Example: vmMethodName = "Ljava/lang/String.wait()V", vmBaseTypeName = "Ljava/lang/Object" --&gt; res =
     * "Ljava/lang/Object.wait()".
     * 
     * @param vmBaseTypeName
     * @param vmMethodName
     * @return
     */
    public static VmMethodName rebase(final ITypeName vmBaseTypeName, final IMethodName vmMethodName) {
        ensureIsInstanceOf(vmBaseTypeName, VmTypeName.class);
        ensureIsInstanceOf(vmMethodName, VmMethodName.class);
        return get(vmBaseTypeName.getIdentifier(), vmMethodName.getSignature());
    }

    public static synchronized VmMethodName get(final String vmFullQualifiedMethodName) {
        VmMethodName res = index.get(vmFullQualifiedMethodName);
        if (res == null) {
            if (vmFullQualifiedMethodName.startsWith("< ")) {
                throwIllegalArgumentException("invalid input: " + vmFullQualifiedMethodName);
            }
            res = new VmMethodName(vmFullQualifiedMethodName);
            index.put(vmFullQualifiedMethodName, res);
        }
        return res;
    }

    private static String removeGenerics(final String vmFullQualifiedMethodName) {
        final String replacement = vmFullQualifiedMethodName.replaceAll("<.*?>", "");
        return replacement;
    }

    public static final IMethodName NULL = VmMethodName.get("L_null.null()V");

    // public static String removeGenerics(final String typeName) {
    // return StringUtils.substringBefore(typeName, "<");
    // }
    private String identifier;

    /**
     * @see #get(String)
     */
    @Testing("Outside of tests, VmMethodNames should be canonicalized through VmMethodName#get(String)")
    protected VmMethodName(final String vmFullQualifiedMethodName) {
        identifier = vmFullQualifiedMethodName;
        // // perform syntax check by creating every possible element from this
        // string. If no exception is thrown everything should be ok...
        getDeclaringType();
        getParameterTypes();
        getReturnType();
    }

    @Override
    public ITypeName getDeclaringType() {
        final int bracket = identifier.lastIndexOf('(');
        final int methodSeperator = identifier.lastIndexOf('.', bracket);
        return VmTypeName.get(identifier.substring(0, methodSeperator));
    }

    @Override
    public String getDescriptor() {
        final int bracket = identifier.lastIndexOf('(');
        return identifier.substring(bracket);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        final int methodSeperator = identifier.lastIndexOf('.');
        final int argumentsSeperator = identifier.lastIndexOf('(');
        return identifier.substring(methodSeperator + 1, argumentsSeperator);
    }

    @Override
    public ITypeName[] getParameterTypes() {
        final ArrayList<VmTypeName> argTypes = new ArrayList<VmTypeName>();
        final int openingBracket = identifier.lastIndexOf('(');
        final char[] desc = identifier.substring(openingBracket + 1).toCharArray();
        int off = 0;
        while (true) {
            if (desc[off] == ')') {
                break;
            }
            switch (desc[off]) {
            case 'V':
                argTypes.add(VOID);
                break;
            case 'Z':
                argTypes.add(BOOLEAN);
                break;
            case 'C':
                argTypes.add(CHAR);
                break;
            case 'B':
                argTypes.add(BYTE);
                break;
            case 'S':
                argTypes.add(SHORT);
                break;
            case 'I':
                argTypes.add(INT);
                break;
            case 'F':
                argTypes.add(FLOAT);
                break;
            case 'J':
                argTypes.add(LONG);
                break;
            case 'D':
                argTypes.add(DOUBLE);
                break;
            case 'L': {
                final int start = off;
                do {
                    off++;
                    // TODO Marcel: Generics 'handling' is a bit strange... need
                    // to fix that here when fully supporting generics later on.
                    if (desc[off] == '<') {
                        off++;
                        int numberOfOpenGenerics = 1;
                        while (numberOfOpenGenerics != 0) {
                            switch (desc[off]) {
                            case '>':
                                numberOfOpenGenerics -= 1;
                                break;
                            case '<':
                                numberOfOpenGenerics += 1;
                                break;
                            }
                            off++;
                        }
                    }
                } while (desc[off] != ';');
                // off points to the ';' now
                final String argumentTypeName = new String(desc, start, off - start);
                argTypes.add(VmTypeName.get(argumentTypeName));
                break;
            }
            case '[': {
                final int start = off;
                off++;
                while (desc[off] == '[') {
                    // do we have an array? -> increase offset if we have
                    // multidimensional arrays:
                    // jump over all array counters
                    off++;
                }
                // now, off is guaranteed to point to the first letter of the
                // type: either 'L' or a primitive letter.
                // if we have an object type:
                if (desc[off] == 'L') {
                    off++;
                    while (desc[off] != ';') {
                        // go forward until the next semicolon
                        off++;
                    }
                    // off points directly on the ';' Thus
                    final String typeName = new String(desc, start, off - start);
                    argTypes.add(VmTypeName.get(typeName));
                } else {
                    // if it is not a declared type, off points directly on the
                    // primitive letter
                    final String typeName = new String(desc, start, off + 1 - start);
                    argTypes.add(VmTypeName.get(typeName));
                }
                break;
            }
            }
            off++;
        }
        return argTypes.toArray(new VmTypeName[argTypes.size()]);
    }

    @Override
    public ITypeName getReturnType() {
        String returnType = StringUtils.substringAfterLast(identifier, ")");
        // strip off throws type from method return
        returnType = StringUtils.substringBefore(returnType, "|");
        if (!returnType.endsWith(";")) {

            // be sure that if it does not end with a ';' is MUST be a primitive
            // or an array of primitives:
            final ITypeName res = VmTypeName.get(returnType);
            ensureIsTrue(res.isPrimitiveType() || res.isArrayType() && res.getArrayBaseType().isPrimitiveType());
            return res;
        } else {
            returnType = StringUtils.substring(returnType, 0, -1);
            return VmTypeName.get(returnType);
        }
    }

    @Override
    public String getSignature() {
        final int methodSeparator = identifier.lastIndexOf('.');
        return identifier.substring(methodSeparator + 1);
    }

    @Override
    public boolean isInit() {
        final String name = getName();
        return "<init>".equals(name) || "<subtype-init>".equals(name);
    }

    @Override
    public boolean isStaticInit() {
        return "<clinit>".equals(getName());
    }

    @Override
    public boolean isSynthetic() {
        return getName().contains("$");
    }

    @Override
    public boolean similar(final IMethodName other) {
        throw throwNotImplemented();
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public boolean isVoid() {
        return getReturnType().isVoid();
    }

    @Override
    public int compareTo(final IMethodName o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public boolean hasParameters() {
        return getParameterTypes().length > 0;
    }
}

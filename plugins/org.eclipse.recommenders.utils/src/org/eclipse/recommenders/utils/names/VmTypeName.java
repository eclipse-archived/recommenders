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

import static org.eclipse.recommenders.utils.Checks.ensureIsFalse;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Checks.ensureIsTrue;
import static org.eclipse.recommenders.utils.Throws.throwIllegalArgumentException;
import static org.eclipse.recommenders.utils.Throws.throwUnreachable;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.collect.MapMaker;

public class VmTypeName implements ITypeName {
    private static Map<String /* vmTypeName */, VmTypeName> index = new MapMaker().weakValues().makeMap();

    public static final VmTypeName OBJECT = VmTypeName.get("Ljava/lang/Object");

    public static final VmTypeName JavaLangNullPointerException = VmTypeName.get("Ljava/lang/NullPointerException");

    public static final VmTypeName JavaLangOutOfMemoryError = VmTypeName.get("Ljava/lang/OutOfMemoryError");

    public static final VmTypeName JavaLangString = VmTypeName.get("Ljava/lang/String");

    public static final VmTypeName JavaLangExceptionInInitializerError = VmTypeName
            .get("Ljava/lang/ExceptionInInitializerError");

    public static final VmTypeName STRING = VmTypeName.get("Ljava/lang/String");

    public static final VmTypeName NULL = get("Lnull");

    public static final VmTypeName BYTE = get("B");

    public static final VmTypeName BOOLEAN = get("Z");

    public static final VmTypeName CHAR = get("C");

    public static final VmTypeName DOUBLE = get("D");

    public static final VmTypeName FLOAT = get("F");

    public static final VmTypeName INT = get("I");

    public static final VmTypeName LONG = get("J");

    public static final VmTypeName SHORT = get("S");

    public static final VmTypeName VOID = get("V");

    public static synchronized VmTypeName get(String typeName) {
        typeName = removeGenerics(typeName);
        VmTypeName res = index.get(typeName);
        if (res == null) {
            res = new VmTypeName(typeName);
            index.put(typeName, res);
        }
        return res;
    }

    private static String removeGenerics(final String typeName) {
        return StringUtils.substringBefore(typeName, "<");
    }

    private String identifier;

    /**
     * @see #get(String)
     */
    @Testing("Outside of tests, VmTypeNames should be canonicalized through VmTypeName#get(String)")
    protected VmTypeName(final String vmTypeName) {
        ensureIsNotNull(vmTypeName);
        ensureIsFalse(vmTypeName.length() == 0, "empty size for type name not permitted");
        if (vmTypeName.length() == 1) {
            switch (vmTypeName.charAt(0)) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'V':
            case 'Z':
                break;
            default:
                throwUnreachable("Invalid type name: " + vmTypeName);
            }
        } else {
            switch (vmTypeName.charAt(0)) {
            case '[':
            case 'L':
                break;
            default:
                throwUnreachable("Invalid type name: " + vmTypeName);
            }
        }

        int off = 0;
        while (off < vmTypeName.length()) {
            final char c = vmTypeName.charAt(off);
            if (c == '[' || c == '/' || c == '-'/* as in 'package-info.class' */|| c == '<' || c == '>'
                    || Character.isJavaIdentifierPart(c)) {
                off++;
                continue;
            }
            throwIllegalArgumentException("Cannot parse '%s' as vm type name.", vmTypeName);
            break;
        }
        identifier = vmTypeName;
    }

    @Override
    public ITypeName getArrayBaseType() {
        ensureIsTrue(isArrayType(), "only array-types have a base type!");
        int start = 0;
        while (identifier.charAt(++start) == '[') {
            // start counter gets increased
        }
        return get(identifier.substring(start));
    }

    @Override
    public int getArrayDimensions() {
        int count = 0;
        int start = 0;
        while (identifier.charAt(start++) == '[') {
            count++;
        }
        return count;
    }

    @Override
    public String getClassName() {
        final int indexOf = identifier.lastIndexOf('/');
        if (indexOf < 0 && !isPrimitiveType()) {
            return identifier.substring(1);
        }
        final String classname = identifier.substring(indexOf + 1);
        return classname;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public IPackageName getPackage() {
        final int lastSlash = identifier.lastIndexOf('/');
        if (lastSlash == -1 || identifier.charAt(0) == '[') {
            return VmPackageName.DEFAULT_PACKAGE;
        }
        return VmPackageName.get(identifier.substring(1, lastSlash));
    }

    @Override
    public boolean isAnonymousType() {
        return identifier.matches(".*\\$\\d+");
    }

    @Override
    public boolean isArrayType() {
        return identifier.charAt(0) == '[';
    }

    @Override
    public boolean isDeclaredType() {
        return identifier.charAt(0) == 'L';
    }

    @Override
    public boolean isNestedType() {
        return identifier.contains("$");
    }

    @Override
    public boolean isPrimitiveType() {
        return !(isArrayType() || isDeclaredType());
    }

    @Override
    public boolean isVoid() {
        return this == VOID;
    }

    @Override
    public int compareTo(final ITypeName o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public IMethodName getDeclaringMethod() {
        ensureIsTrue(isNestedType(), "only valid on nested types");
        final int lastPathSegmentSeparator = identifier.lastIndexOf('/');
        final String path = identifier.substring(0, lastPathSegmentSeparator);
        final int bracket = path.lastIndexOf('(');
        final int methodSeparator = path.lastIndexOf('/', bracket);
        final String newFQName = path.substring(0, methodSeparator) + "." + path.substring(methodSeparator + 1);
        return VmMethodName.get(newFQName);
    }

    @Override
    public ITypeName getDeclaringType() {
        ensureIsTrue(isNestedType(), "only valid on nested types");
        final int lastIndexOf = identifier.lastIndexOf('$');
        final String declaringTypeName = identifier.substring(0, lastIndexOf);
        return get(declaringTypeName);
    }
}

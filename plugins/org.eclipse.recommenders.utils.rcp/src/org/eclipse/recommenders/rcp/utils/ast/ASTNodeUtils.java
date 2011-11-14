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
package org.eclipse.recommenders.rcp.utils.ast;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.inject.Inject;

public class ASTNodeUtils {
    @Inject
    private static JavaElementResolver resolver;

    public static boolean sameSimpleName(final Type jdtParam, final ITypeName crParam) {
        final String jdtSimpleName = jdtParam.toString();
        String crSimpleName = crParam.getClassName();
        if (crSimpleName.contains("$")) {
            crSimpleName = StringUtils.substringAfterLast(crSimpleName, "$");
        }
        return jdtSimpleName.equals(crSimpleName);
    }

    public static Type getBaseType(final Type jdtType) {
        if (!jdtType.isArrayType()) {
            return jdtType;
        }
        final ArrayType arrayType = (ArrayType) jdtType;
        return getBaseType(arrayType.getComponentType());
    }

    public static int getLineNumberOfNodeStart(final CompilationUnit cuNode, final ASTNode node) {
        final int startPosition = node.getStartPosition();
        final int lineNumber = cuNode.getLineNumber(startPosition);
        return lineNumber;
    }

    public static int getLineNumberOfNodeEnd(final CompilationUnit cuNode, final ASTNode node) {
        final int endPosition = node.getStartPosition() + node.getLength();
        final int lineNumber = cuNode.getLineNumber(endPosition);
        return lineNumber;
    }

    public static boolean haveSameNumberOfParameters(final List<SingleVariableDeclaration> jdtParams,
            final ITypeName[] crParams) {
        return crParams.length == jdtParams.size();
    }

    public static boolean sameSimpleName(final MethodDeclaration decl, final IMethodName crMethod) {
        final String methodName = decl.getName().toString();
        if (crMethod.isInit()) {
            final ITypeName declaringType = crMethod.getDeclaringType();
            final String className = declaringType.getClassName();
            final boolean sameMethodName = className.equals(methodName);
            return sameMethodName;
        }
        final boolean sameMethodName = methodName.equals(crMethod.getName());
        return sameMethodName;
    }

    public static boolean sameSimpleName(final MethodInvocation invoke, final IMethodName crMethod) {
        final String methodName = invoke.getName().toString();
        if (crMethod.isInit()) {
            final ITypeName declaringType = crMethod.getDeclaringType();
            final String className = declaringType.getClassName();
            final boolean sameMethodName = className.equals(methodName);
            return sameMethodName;
        }
        final boolean sameMethodName = methodName.equals(crMethod.getName());
        return sameMethodName;
    }

    public static boolean haveSameParameterTypes(final List<SingleVariableDeclaration> jdtParams,
            final ITypeName[] crParams) {
        for (int i = crParams.length; i-- > 0;) {
            Type jdtParam = jdtParams.get(i).getType();
            jdtParam = getBaseType(jdtParam);
            final ITypeName crParam = crParams[i];

            if (jdtParam.isArrayType()) {
                if (!crParam.isArrayType()) {
                    return false;
                }
                final ArrayType jdtArrayType = (ArrayType) jdtParam;
                final int jdtDimensions = jdtArrayType.getDimensions();
                final int crDimensions = crParam.getArrayDimensions();
                if (jdtDimensions != crDimensions) {
                    return false;
                }
                return !sameSimpleName(getBaseType(jdtArrayType), crParam.getArrayBaseType());
            }

            if (jdtParam.isPrimitiveType()) {
                continue;
            }
            if (jdtParam.isSimpleType() && !sameSimpleName(jdtParam, crParam)) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameTypes(final List<Type> jdtTypes, final ITypeName[] crTypes) {
        for (int i = crTypes.length; --i > 0;) {
            final Type jdtType = jdtTypes.get(i);
            final ITypeName crType = crTypes[i];
            if (!sameType(jdtType, crType)) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameType(final Type jdtType, final ITypeName crType) {
        if (jdtType == null || crType == null) {
            return false;
        }
        //
        if (jdtType.isArrayType() || jdtType.isPrimitiveType()) {
            return true;
        }
        if (jdtType.isSimpleType() && !sameSimpleName(jdtType, crType)) {
            return false;
        }
        final ITypeBinding jdtTypeBinding = jdtType.resolveBinding();
        return sameType(jdtTypeBinding, crType);
    }

    public static boolean sameType(final ITypeBinding jdtType, final ITypeName crType) {
        if (jdtType == null || crType == null) {
            return false;
        }
        final IType javaElement = (IType) jdtType.getJavaElement();
        if (javaElement == null) {
            return false;
        }
        final ITypeName jdt2crType = resolver.toRecType(javaElement);
        return jdt2crType == crType;
    }
}

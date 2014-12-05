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
package org.eclipse.recommenders.jdt;

import static com.google.common.base.Optional.*;
import static org.eclipse.recommenders.internal.jdt.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.List;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmFieldName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public final class AstBindings {

    private AstBindings() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static Optional<ITypeName> toTypeName(ITypeBinding b) {
        if (b == null) {
            return absent();
        }
        final StringBuilder sb = new StringBuilder();
        b = b.getErasure();
        if (b == null) {
            return absent();
        }
        if (b.isArray()) {
            for (int i = b.getDimensions(); i-- > 0;) {
                sb.append('[');
            }
            b = b.getElementType();
            if (b == null) {
                return absent();
            }
        }

        final String binaryName = b.getBinaryName();
        if (binaryName == null) {
            return absent();
        }
        if (b.isPrimitive()) {
            sb.append(binaryName);
        } else {
            sb.append('L').append(binaryName.replace('.', '/'));
        }

        try {
            final ITypeName res = VmTypeName.get(sb.toString());
            return of(res);
        } catch (final Exception e) {
            log(FAILED_TO_CREATE_TYPENAME, b, e);
            return absent();
        }
    }

    public static Optional<ITypeName> toTypeName(@Nullable final Type type) {
        if (type == null) {
            return absent();
        }
        final ITypeBinding b = type.resolveBinding();
        if (b == null) {
            return absent();
        }
        return toTypeName(b);
    }

    public static List<ITypeName> toTypeNames(final ITypeBinding[] interfaces) {
        final List<ITypeName> res = Lists.newLinkedList();
        for (final ITypeBinding b : interfaces) {
            final Optional<ITypeName> opt = toTypeName(b);
            if (opt.isPresent()) {
                res.add(opt.get());
            }
        }
        return res;
    }

    private static boolean needsColon(final ITypeBinding p) {
        if (p.isArray()) {
            return !p.getElementType().isPrimitive();
        }
        return !p.isPrimitive();
    }

    /**
     * Converts a method binding to its IMethodName counterpart. Note that type variables filled in by JDT are ignored,
     * i.e., the declaring method is used to find the IMethodName.
     *
     * @param b
     *            the binding to resolve.
     * @see IMethodBinding#getMethodDeclaration()
     */
    public static Optional<IMethodName> toMethodName(IMethodBinding b) {
        if (b == null) {
            return absent();
        }
        IMethodBinding decl = b.getMethodDeclaration();
        if (decl != null) {
            b = decl;
        }
        final StringBuilder sb = new StringBuilder();
        final ITypeName declaringType = toTypeName(b.getDeclaringClass()).orNull();
        if (declaringType == null) {
            return absent();
        }
        final String methodName = b.isConstructor() ? "<init>" : b.getName(); //$NON-NLS-1$
        sb.append(declaringType).append('.').append(methodName).append('(');
        for (final ITypeBinding param : b.getParameterTypes()) {
            final ITypeName paramType = toTypeName(param).orNull();
            if (paramType == null) {
                return absent();
            }
            sb.append(paramType);
            if (needsColon(param)) {
                sb.append(';');
            }
        }
        final ITypeBinding returnType = b.getReturnType();
        final ITypeName obj = toTypeName(returnType).orNull();
        if (obj == null) {
            return absent();
        }
        sb.append(')').append(obj);
        if (needsColon(returnType)) {
            sb.append(';');
        }

        IMethodName ref;
        try {
            ref = VmMethodName.get(sb.toString());
        } catch (final Exception e1) {
            log(FAILED_TO_CREATE_METHODNAME, b, e1);
            return absent();
        }
        return of(ref);
    }

    public static Optional<IVariableBinding> getVariableBinding(final Name name) {
        final IBinding b = name.resolveBinding();
        IVariableBinding res = (IVariableBinding) (b instanceof IVariableBinding ? b : null);
        return fromNullable(res);
    }

    public static Optional<IFieldName> toFieldName(IVariableBinding b) {
        if (b == null || !b.isField()) {
            return absent();
        }
        String name = b.getName();
        ITypeName declared = toTypeName(b.getDeclaringClass()).orNull();
        ITypeName type = toTypeName(b.getType()).orNull();
        if (declared == null || type == null) {
            return absent();
        }
        IFieldName res = VmFieldName.get(declared.getIdentifier() + '.' + name + ';' + type.getIdentifier());
        return of(res);
    }

    public static Optional<IPackageName> toPackageName(IPackageBinding pkg) {
        if (pkg == null) {
            return absent();
        }
        String s = pkg.getName().replace('.', '/');
        return Optional.of((IPackageName) VmPackageName.get(s));
    }

    public static Optional<IPackageName> toPackageName(ITypeBinding b) {
        if (b == null) {
            return absent();
        }
        return toPackageName(b.getPackage());
    }

    public static List<IPackageName> toPackageNames(final ITypeBinding[] types) {
        final List<IPackageName> res = Lists.newLinkedList();
        for (final ITypeBinding b : types) {
            final Optional<IPackageName> opt = toPackageName(b);
            if (opt.isPresent()) {
                res.add(opt.get());
            }
        }
        return res;
    }
}

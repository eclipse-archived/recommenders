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
package org.eclipse.recommenders.utils.rcp.ast;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class BindingUtils {

    public static Optional<ITypeName> toTypeName(ITypeBinding b) {
        if (b == null) {
            return absent();
        }
        final StringBuilder sb = new StringBuilder();
        b = b.getErasure();
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
            RecommendersUtilsPlugin.logError(e, "failed to create type name from %s", b);
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
        final String methodName = b.isConstructor() ? "<init>" : b.getName();
        sb.append(declaringType).append(".").append(methodName).append("(");
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
        sb.append(")").append(obj);
        if (needsColon(returnType)) {
            sb.append(';');
        }

        IMethodName ref;
        try {
            ref = VmMethodName.get(sb.toString());
        } catch (final Exception e1) {
            RecommendersUtilsPlugin.logError(e1, "failed to create IMethodName from binding '%s'", b);
            return absent();
        }
        return Optional.of(ref);
    }

    public static IVariableBinding getVariableBinding(final Name name) {
        final IBinding b = name.resolveBinding();
        return (IVariableBinding) (b instanceof IVariableBinding ? b : null);
    }

    public static Optional<IPackageName> toPackageName(IPackageBinding pkg) {
        if (pkg == null)
            return absent();
        String s = pkg.getName().replace('.', '/');
        return Optional.of((IPackageName) VmPackageName.get(s));
    }

    public static Optional<IPackageName> toPackageName(ITypeBinding b) {
        if (b == null)
            return absent();
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
    // public static Optional<IMethod> getMethod(final IMethodBinding b) {
    // // assertNotNull(b);
    // final IJavaElement element = resolveJavaElementQuietly(b);
    // if (!resolveSucceeded(element, IMethod.class)) {
    // return absent();
    // }
    // return of((IMethod) element);
    // }

    // public static Optional<IMethod> getMethod(final MethodDeclaration method) {
    // if (method == null) {
    // return null;
    // }
    // final IMethodBinding b = method.resolveBinding();
    // return getMethod(b);
    // }

    // private static boolean resolveSucceeded(final IJavaElement element, final Class<?> type) {
    // ensureIsNotNull(type);
    // return type.isInstance(element);
    // }

    // public static IJavaElement resolveJavaElementQuietly(final IBinding binding) {
    // if (binding == null) {
    // return null;
    // }
    // try {
    // return binding.getJavaElement();
    // } catch (final RuntimeException e) {
    // }
    // return null;
    // }

    // public static IMethodName toMethodName(final IMethod method) {
    // try {
    // return method == null ? null : resolver.toRecMethod(method).orNull();
    // } catch (final Exception e) {
    // final String msg = format("java element resolver failed with %s: %s", method.getKey(), e.toString());
    // RecommendersUtilsPlugin.log(StatusUtil.newStatus(IStatus.WARNING, msg, null));
    // return null;
    // }
    // }

    // public static ITypeName toTypeName(final IType type) {
    // try {
    // return type == null ? null : resolver.toRecType(type);
    // } catch (final Exception e) {
    // final String msg = format("java element resolver failed with %s: %s", type.getKey(), e.toString());
    // RecommendersUtilsPlugin.log(StatusUtil.newStatus(IStatus.WARNING, msg, null));
    // return null;
    // }
    // }

    // @Inject
    // private static JavaElementResolver resolver;
    //
    // @Testing
    // public static void testingInitializeResolver() {
    // resolver = new JavaElementResolver();
    // }

    // public static Optional<IType> getVariableType(final IVariableBinding b) {
    // if (b == null) {
    // return absent();
    // }
    // return getType(b.getType());
    // }

    // public static Optional<IType> getMethodReturn(final IMethodBinding b) {
    // // assertNotNull(b);
    // final ITypeBinding returnType = b.getReturnType();
    // return getType(returnType);
    // }

    // public static IType[] getMethodParameterTypes(final IMethodBinding b) {
    // ensureIsNotNull(b);
    // final ITypeBinding[] paramBindings = b.getParameterTypes();
    // final IType[] paramTypes = new IType[paramBindings.length];
    // for (int i = paramBindings.length; i-- > 0;) {
    // paramTypes[i] = getType(paramBindings[i]).orNull();
    // }
    // return paramTypes;
    // }

    // public static Optional<IType> getType(final ITypeBinding b) {
    // final IJavaElement element = resolveJavaElementQuietly(b);
    // if (element instanceof IType) {
    // return of((IType) element);
    // } else if (element instanceof TypeParameter) {
    // // do nothing.
    // // how should we deal with <T extends S>?
    // }
    // return absent();
    // }
}

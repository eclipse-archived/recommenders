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
import static java.lang.String.format;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class BindingUtils {

    @Inject
    private static JavaElementResolver resolver;

    public static Optional<IType> getVariableType(final IVariableBinding b) {
        if (b == null) {
            return absent();
        }
        return getType(b.getType());
    }

    public static Optional<IType> getMethodReturn(final IMethodBinding b) {
        // assertNotNull(b);
        final ITypeBinding returnType = b.getReturnType();
        return getType(returnType);
    }

    public static IType[] getMethodParameterTypes(final IMethodBinding b) {
        ensureIsNotNull(b);
        final ITypeBinding[] paramBindings = b.getParameterTypes();
        final IType[] paramTypes = new IType[paramBindings.length];
        for (int i = paramBindings.length; i-- > 0;) {
            paramTypes[i] = getType(paramBindings[i]).orNull();
        }
        return paramTypes;
    }

    public static Optional<IType> getType(final ITypeBinding b) {
        final IJavaElement element = resolveJavaElementQuietly(b);
        if (element instanceof IType) {
            return of((IType) element);
        } else if (element instanceof TypeParameter) {
            // do nothing.
            // how should we deal with <T extends S>?
        }
        return absent();
    }

    public static ITypeName toTypeName(@Nullable final Type type) {
        if (type == null) {
            return null;
        }
        final ITypeBinding b = type.resolveBinding();
        if (b == null) {
            return null;
        }
        return toTypeName(b);
    }

    public static IMethod getMethod(final IMethodBinding b) {
        // assertNotNull(b);
        final IJavaElement element = resolveJavaElementQuietly(b);
        if (!resolveSucceeded(element, IMethod.class)) {
            return null;
        }
        return (IMethod) element;
    }

    public static IMethod getMethod(final MethodDeclaration method) {
        if (method == null) {
            return null;
        }
        final IMethodBinding b = method.resolveBinding();
        return getMethod(b);
    }

    private static boolean resolveSucceeded(final IJavaElement element, final Class<?> type) {
        ensureIsNotNull(type);
        return type.isInstance(element);
    }

    public static IJavaElement resolveJavaElementQuietly(final IBinding binding) {
        if (binding == null) {
            return null;
        }
        try {
            return binding.getJavaElement();
        } catch (final RuntimeException e) {
        }
        return null;
    }

    public static IMethodName toMethodName(final IMethod method) {
        try {
            return method == null ? null : resolver.toRecMethod(method).orNull();
        } catch (final Exception e) {
            final String msg = format("java element resolver failed with %s: %s", method.getKey(), e.toString());
            RecommendersUtilsPlugin.log(StatusUtil.newStatus(IStatus.WARNING, msg, null));
            return null;
        }
    }

    public static ITypeName toTypeName(final IType type) {
        try {
            return type == null ? null : resolver.toRecType(type);
        } catch (final Exception e) {
            final String msg = format("java element resolver failed with %s: %s", type.getKey(), e.toString());
            RecommendersUtilsPlugin.log(StatusUtil.newStatus(IStatus.WARNING, msg, null));
            return null;
        }
    }

    public static IMethodName toMethodName(final IMethodBinding b) {
        final IMethodName ref = toMethodName(getMethod(b));
        if (ref == null && b != null) {
            try {
                // there is no matching source element (only a compiler-generated method)
                final ITypeBinding declaringClass = b.getDeclaringClass();
                final ITypeName typeName = toTypeName(declaringClass);
                final String key = b.getKey();
                String methodName = key.substring(key.lastIndexOf(";.") + 2);
                if (methodName.startsWith("(")) {
                    methodName = "<init>" + methodName;
                }
                return VmMethodName.get(typeName.getIdentifier(), methodName);
            } catch (final Exception e) {
            }
        }
        return ref;
    }

    public static ITypeName toTypeName(final ITypeBinding b) {
        final Optional<IType> type = getType(b);
        if (!type.isPresent()) {
            return VmTypeName.NULL;
        }
        final ITypeName ref = toTypeName(type.get());
        if (ref == null && b != null) {
            try {
                if (b.isPrimitive()) {
                    return VmTypeName.get(b.getBinaryName());
                }
                if (b.isArray()) {
                    return null;
                }
                final String name = b.getName();
                final String pkg = b.getPackage().getKey();
                final String fullQualifiedName = "L" + pkg + "/" + name;
                final VmTypeName fallback = VmTypeName.get(fullQualifiedName);
                return fallback;
            } catch (final Exception e) {
                // it's just a try to recover from invalid input... if it
                // doesn't work, don't do
                // anything
            }
        }
        return ref;
    }

    public static IVariableBinding getVariableBinding(final Name name) {
        final IBinding b = name.resolveBinding();
        return (IVariableBinding) (b instanceof IVariableBinding ? b : null);
    }

    public static List<ITypeName> toTypeNames(final ITypeBinding[] interfaces) {
        final List<ITypeName> res = Lists.newLinkedList();
        for (final ITypeBinding b : interfaces) {
            final ITypeName typeName = toTypeName(b);
            if (typeName != null) {
                res.add(typeName);
            }
        }
        return res;
    }

}

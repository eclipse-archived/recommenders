/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henß - initial API and implementation.
 */
package org.eclipse.recommenders.internal.chain.rcp;

import static org.eclipse.recommenders.internal.chain.rcp.l10n.LogMessages.WARNING_CANNOT_USE_AS_PARENT_OF_COMPLETION_LOCATION;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@SuppressWarnings("restriction")
public final class TypeBindingAnalyzer {

    private static final Predicate<FieldBinding> NON_STATIC_FIELDS_ONLY_FILTER = new Predicate<FieldBinding>() {

        @Override
        public boolean apply(final FieldBinding m) {
            return m.isStatic();
        }
    };

    private static final Predicate<MethodBinding> RELEVANT_NON_STATIC_METHODS_ONLY_FILTER = new Predicate<MethodBinding>() {

        @Override
        public boolean apply(final MethodBinding m) {
            return m.isStatic() || isVoid(m) || m.isConstructor() || hasPrimitiveReturnType(m);
        }
    };

    private static final Predicate<FieldBinding> STATIC_FIELDS_ONLY_FILTER = new Predicate<FieldBinding>() {

        @Override
        public boolean apply(final FieldBinding m) {
            return !m.isStatic();
        }
    };

    private static final Predicate<MethodBinding> STATIC_NON_VOID_NON_PRIMITIVE_METHODS_ONLY_FILTER = new Predicate<MethodBinding>() {

        @Override
        public boolean apply(final MethodBinding m) {
            return !m.isStatic() || isVoid(m) || m.isConstructor() || hasPrimitiveReturnType(m);
        }
    };

    private TypeBindingAnalyzer() {
    }

    static boolean isVoid(final MethodBinding m) {
        return hasPrimitiveReturnType(m) && m.returnType.constantPoolName()[0] == 'V';
    }

    static boolean hasPrimitiveReturnType(final MethodBinding m) {
        return m.returnType.constantPoolName().length == 1;
    }

    public static Collection<Binding> findVisibleInstanceFieldsAndRelevantInstanceMethods(final TypeBinding type,
            final InvocationSite invocationSite, final Scope scope) {
        return findFieldsAndMethods(type, invocationSite, scope, NON_STATIC_FIELDS_ONLY_FILTER,
                RELEVANT_NON_STATIC_METHODS_ONLY_FILTER);
    }

    public static Collection<Binding> findAllPublicStaticFieldsAndNonVoidNonPrimitiveStaticMethods(
            final TypeBinding type, final InvocationSite invocationSite, final Scope scope) {
        return findFieldsAndMethods(type, invocationSite, scope, STATIC_FIELDS_ONLY_FILTER,
                STATIC_NON_VOID_NON_PRIMITIVE_METHODS_ONLY_FILTER);
    }

    private static Collection<Binding> findFieldsAndMethods(final TypeBinding type, final InvocationSite invocationSite,
            final Scope scope, final Predicate<FieldBinding> fieldFilter, final Predicate<MethodBinding> methodFilter) {
        final Map<String, Binding> tmp = new LinkedHashMap<>();
        final TypeBinding receiverType = scope.classScope().referenceContext.binding;
        for (final ReferenceBinding cur : findAllSupertypesIncludeingArgument(type)) {
            for (final MethodBinding method : cur.methods()) {
                if (methodFilter.apply(method) || !method.canBeSeenBy(invocationSite, scope)) {
                    continue;
                }
                final String key = createMethodKey(method);
                if (!tmp.containsKey(key)) {
                    tmp.put(key, method);
                }
            }
            for (final FieldBinding field : cur.fields()) {
                if (fieldFilter.apply(field) || !field.canBeSeenBy(receiverType, invocationSite, scope)) {
                    continue;
                }
                final String key = createFieldKey(field);
                if (!tmp.containsKey(key)) {
                    tmp.put(key, field);
                }
            }
        }
        return tmp.values();
    }

    private static List<ReferenceBinding> findAllSupertypesIncludeingArgument(final TypeBinding type) {
        final TypeBinding base = removeArrayWrapper(type);
        if (!(base instanceof ReferenceBinding)) {
            return Collections.emptyList();
        }
        final List<ReferenceBinding> supertypes = new LinkedList<>();
        final LinkedList<ReferenceBinding> queue = new LinkedList<>();
        queue.add((ReferenceBinding) base);
        while (!queue.isEmpty()) {
            final ReferenceBinding superType = queue.poll();
            if (superType == null || supertypes.contains(superType)) {
                continue;
            }
            supertypes.add(superType);
            queue.add(superType.superclass());
            for (final ReferenceBinding interfc : superType.superInterfaces()) {
                queue.add(interfc);
            }
        }
        return supertypes;
    }

    private static String createFieldKey(final FieldBinding field) {
        return new StringBuilder().append(field.name).append(field.type.signature()).toString();
    }

    private static String createMethodKey(final MethodBinding method) {
        final String signature = String.valueOf(method.signature());
        final String signatureWithoutReturnType = StringUtils.substringBeforeLast(signature, ")"); //$NON-NLS-1$
        return new StringBuilder().append(method.readableName()).append(signatureWithoutReturnType).toString();
    }

    public static boolean isAssignable(final ChainElement edge, final TypeBinding expectedType,
            final int expectedDimension) {
        if (expectedDimension <= edge.getReturnTypeDimension()) {
            final TypeBinding base = removeArrayWrapper(edge.getReturnType());
            if (base instanceof BaseTypeBinding) {
                return false;
            }
            if (base.isCompatibleWith(expectedType)) {
                return true;
            }
            final LinkedList<ReferenceBinding> supertypes = new LinkedList<>();
            supertypes.add((ReferenceBinding) base);
            final String expectedSignature = String.valueOf(expectedType.signature());
            while (!supertypes.isEmpty()) {
                final ReferenceBinding type = supertypes.poll();
                if (String.valueOf(type.signature()).equals(expectedSignature)) {
                    return true;
                }
                final ReferenceBinding superclass = type.superclass();
                if (superclass != null) {
                    supertypes.add(superclass);
                }
                for (final ReferenceBinding intf : type.superInterfaces()) {
                    supertypes.add(intf);
                }
            }
        }
        return false;
    }

    public static TypeBinding removeArrayWrapper(final TypeBinding type) {
        TypeBinding base = type;
        while (base instanceof ArrayBinding) {
            base = ((ArrayBinding) base).elementsType();
        }
        return base;
    }

    public static List<Optional<TypeBinding>> resolveBindingsForExpectedTypes(final IRecommendersCompletionContext ctx,
            final Scope scope) {
        final InternalCompletionContext context = (InternalCompletionContext) ctx.getJavaContext().getCoreContext();
        final ASTNode parent = context.getCompletionNodeParent();
        final List<Optional<TypeBinding>> bindings = new LinkedList<>();
        if (parent instanceof LocalDeclaration) {
            bindings.add(Optional.fromNullable(((LocalDeclaration) parent).type.resolvedType));
        } else if (parent instanceof ReturnStatement) {
            bindings.add(resolveReturnStatement(context));
        } else if (parent instanceof FieldDeclaration) {
            bindings.add(Optional.fromNullable(((FieldDeclaration) parent).type.resolvedType));
        } else if (parent instanceof Assignment) {
            bindings.add(Optional.fromNullable(((Assignment) parent).resolvedType));
        } else if (isCompletionOnMethodParameter(context)) {
            for (final ITypeName type : ctx.getExpectedTypeNames()) {
                bindings.add(Optional.of(scope.getType(type.getClassName().toCharArray())));
            }
        } else {
            log(WARNING_CANNOT_USE_AS_PARENT_OF_COMPLETION_LOCATION, parent.getClass());
        }
        return bindings;
    }

    private static boolean isCompletionOnMethodParameter(final InternalCompletionContext context) {
        return context.getCompletionNode() instanceof CompletionOnQualifiedAllocationExpression
                || context.getCompletionNode() instanceof CompletionOnMessageSend
                || context.getCompletionNodeParent() instanceof MessageSend;
    }

    private static Optional<TypeBinding> resolveReturnStatement(final InternalCompletionContext context) {
        final String expected = String.valueOf(context.getExpectedTypesKeys()[0]);
        final ObjectVector methods = context.getVisibleMethods();
        for (int i = 0; i < methods.size; ++i) {
            final TypeBinding type = ((MethodBinding) methods.elementAt(i)).returnType;
            final String key = String.valueOf(type.computeUniqueKey());
            if (key.equals(expected)) {
                return Optional.of(type);
            }
        }
        return Optional.absent();
    }
}

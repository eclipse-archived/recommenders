/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.*;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.RECEIVER_NAME;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.utils.names.IMethodName;

@SuppressWarnings({ "rawtypes", "restriction" })
public class CallCompletionContextFunctions {

    // TODO need to rename
    public static final CompletionContextKey<IType> RECEIVER_TYPE2 = new CompletionContextKey<IType>();
    public static final CompletionContextKey<IMethodName> RECEIVER_DEF_BY = new CompletionContextKey<IMethodName>();
    public static final CompletionContextKey<List<IMethodName>> RECEIVER_CALLS = new CompletionContextKey<List<IMethodName>>();
    public static final CompletionContextKey<DefinitionKind> RECEIVER_DEF_TYPE = new CompletionContextKey<ICallModel.DefinitionKind>();

    public static Map<CompletionContextKey, ICompletionContextFunction> registerDefaults(
            Map<CompletionContextKey, ICompletionContextFunction> functions) {
        ReceiverCallsCompletionContextFunction f = new ReceiverCallsCompletionContextFunction();
        functions.put(RECEIVER_CALLS, f);
        functions.put(RECEIVER_DEF_BY, f);
        functions.put(RECEIVER_DEF_TYPE, f);
        functions.put(RECEIVER_TYPE2, new ReceiverTypeContextFunction());
        return functions;
    }

    public static class ReceiverCallsCompletionContextFunction implements ICompletionContextFunction {

        @SuppressWarnings("unchecked")
        @Override
        public Object compute(IRecommendersCompletionContext context, CompletionContextKey key) {
            List<IMethodName> calls = null;
            DefinitionKind defType = null;
            IMethodName defBy = null;

            MethodDeclaration method = context.get(CompletionContextKey.ENCLOSING_AST_METHOD, null);
            if (method != null) {
                String receiverName = context.get(RECEIVER_NAME, null);
                AstDefUseFinder f = new AstDefUseFinder(receiverName, method);
                calls = f.getCalls();
                defType = f.getDefinitionKind();
                defBy = f.getDefiningMethod().orNull();

                if (defType == DefinitionKind.UNKNOWN) {
                    // if the ast resolver could not find a definition of the
                    // variable, it's a method return value? Ask
                    // the context and try.
                    IMethodName def = context.getMethodDef().orNull();
                    if (def == null) {
                        if (receiverName.isEmpty()) {
                            defType = THIS;
                        } else {
                            defType = FIELD;
                        }
                    } else if (def.isInit()) {
                        defType = DefinitionKind.NEW;
                        defBy = def;
                    } else {
                        defType = RETURN;
                        defBy = def;
                    }
                }
            }
            context.set(RECEIVER_CALLS, calls);
            context.set(RECEIVER_DEF_TYPE, defType);
            context.set(RECEIVER_DEF_BY, defBy);
            return context.get(key).orNull();
        }
    }

    public static class ReceiverTypeContextFunction implements ICompletionContextFunction<Object> {

        @Override
        public Object compute(IRecommendersCompletionContext context, CompletionContextKey<Object> key) {
            IType receiverType = null;
            try {
                receiverType = findReceiver(context);
            } catch (Exception e) {
            }
            context.set(RECEIVER_TYPE2, receiverType);
            return receiverType;
        }

        private IType findReceiver(IRecommendersCompletionContext context) throws Exception {
            IType receiverType = context.getReceiverType().orNull();
            String receiverName = context.getReceiverName();
            if (isExplicitThis(receiverName) || isImplicitThis(receiverType, receiverName)) {
                final IMethod m = context.getEnclosingMethod().orNull();
                if (m == null || JdtFlags.isStatic(m)) {
                    return receiverType;
                }
                final IType type = m.getDeclaringType();
                final ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
                receiverType = hierarchy.getSuperclass(type);

                // XXX workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=441021
                if (receiverType == null) {
                    String superclassTypeSignature = type.getSuperclassTypeSignature();
                    if (superclassTypeSignature != null) {
                        receiverType = JdtUtils.findTypeFromSignature(superclassTypeSignature, type).orNull();
                    }
                }
            }
            return receiverType;
        }

        private boolean isImplicitThis(IType receiverType, String receiverName) {
            return receiverType == null && isEmpty(receiverName);
        }

        private boolean isExplicitThis(String receiverName) {
            return "this".equals(receiverName); //$NON-NLS-1$
        }
    }
}

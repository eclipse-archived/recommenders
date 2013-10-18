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

import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.*;
import static org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.CCTX_ENCLOSING_AST_METHOD;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings({ "rawtypes", "restriction" })
public class CallCompletionContextFunctions {
    private static final ImmutableSet<String> THIS_NAMES = ImmutableSet.of("", "this", "super");
    // TODO need to rename
    public static final String CCTX_RECEIVER_TYPE2 = "receiver-type2";
    public static final String CCTX_RECEIVER_DEF_BY = "receiver-def-by";
    public static final String CCTX_RECEIVER_CALLS = "receiver-calls";
    public static final String CCTX_RECEIVER_DEF_TYPE = "receiver-def-type";

    public static Map<String, ICompletionContextFunction> registerDefaults(
            Map<String, ICompletionContextFunction> functions) {
        ReceiverCallsCompletionContextFunction f = new ReceiverCallsCompletionContextFunction();
        functions.put(CCTX_RECEIVER_CALLS, f);
        functions.put(CCTX_RECEIVER_DEF_BY, f);
        functions.put(CCTX_RECEIVER_DEF_TYPE, f);
        functions.put(CCTX_RECEIVER_TYPE2, new ReceiverTypeContextFunction());
        return functions;
    }

    public static class ReceiverCallsCompletionContextFunction implements ICompletionContextFunction<Object> {

        @Override
        public Object compute(IRecommendersCompletionContext context, String key) {
            List<IMethodName> calls = null;
            DefinitionKind defType = null;
            IMethodName defBy = null;

            MethodDeclaration method = context.get(CCTX_ENCLOSING_AST_METHOD, null);
            if (method != null) {
                String receiverName = context.get(CompletionContextFunctions.CCTX_RECEIVER_NAME, null);
                AstDefUseFinder f = new AstDefUseFinder(receiverName, method);
                calls = f.getCalls();
                defType = f.getDefinitionKind();
                defBy = f.getDefiningMethod().orNull();

                if (defType == DefinitionKind.UNKNOWN) {
                    // if the ast resolver could not find a definition of the variable, it's a method return value? Ask
                    // the context and try.
                    IMethodName def = context.getMethodDef().orNull();
                    if (def == null) {
                        if ("".equals(receiverName)) {
                            defType = THIS;
                        } else {
                            defType = FIELD;
                        }
                    } else {
                        defType = RETURN;
                        defBy = def;
                    }
                }
            }
            context.set(CCTX_RECEIVER_CALLS, calls);
            context.set(CCTX_RECEIVER_DEF_TYPE, defType);
            context.set(CCTX_RECEIVER_DEF_BY, defBy);
            return context.get(key).orNull();
        }
    }

    public static class ReceiverTypeContextFunction implements ICompletionContextFunction<Object> {

        @Override
        public Object compute(IRecommendersCompletionContext context, String key) {
            IType receiverType = null;
            try {
                receiverType = findReceiver(context);
            } catch (Exception e) {
            }
            context.set(CCTX_RECEIVER_TYPE2, receiverType);
            return receiverType;
        }

        private IType findReceiver(IRecommendersCompletionContext context) throws Exception {
            IType receiverType = context.getReceiverType().orNull();
            String receiverName = context.getReceiverName();
            if (receiverType == null && THIS_NAMES.contains(receiverName)) {
                final IMethod m = context.getEnclosingMethod().orNull();
                if (m == null || JdtFlags.isStatic(m)) {
                    return receiverType;
                }
                final IType type = m.getDeclaringType();
                final ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
                receiverType = hierarchy.getSuperclass(type);
            }
            return receiverType;
        }

    }

}

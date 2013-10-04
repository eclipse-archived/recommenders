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

import static com.google.common.base.Optional.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.recommenders.calls.ICallModel.DefinitionKind.*;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findFirstDeclaration;

import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.calls.ICallModel.DefinitionKind;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.rcp.utils.ASTNodeUtils;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public class AstCallCompletionAnalyzer {

    private final IRecommendersCompletionContext ctx;
    private String receiverName;

    private Optional<IType> receiverType;
    private Optional<IMethod> overrides;
    private DefinitionKind defKind;
    private List<IMethodName> calls;
    private IMethodName definedBy;

    public AstCallCompletionAnalyzer(IRecommendersCompletionContext context) {
        ctx = context;
    }

    public Optional<IType> getReceiverType() {
        if (receiverType == null) {
            findReceiver();
        }
        return receiverType;
    }

    private void findReceiver() {
        receiverName = ctx.getReceiverName();
        receiverType = ctx.getReceiverType();
        if (!receiverType.isPresent()
                && (isReceiverNameThis() || isReceiverNameSuper() || isReceiverNameImplicitThis() || //
                !isEmpty(receiverName) // any string that may serve as prefix: w$
                )) {
            // receiver may be this!
            receiverName = "super";
            setReceiverToSupertype();
        }
    }

    private boolean isReceiverNameThis() {
        return "this".equals(receiverName);
    }

    private boolean isReceiverNameSuper() {
        return "super".equals(receiverName);
    }

    private boolean isReceiverNameImplicitThis() {
        return "".equals(receiverName);
    }

    private void setReceiverToSupertype() {
        try {
            final IMethod m = ctx.getEnclosingMethod().orNull();
            if (m == null || JdtFlags.isStatic(m)) {
                return;
            }
            final IType type = m.getDeclaringType();
            final ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
            receiverType = fromNullable(hierarchy.getSuperclass(type));
        } catch (final Exception e) {
            // RecommendersPlugin.logError(e, "Failed to resolve super type of %s", ctx.getEnclosingElement());
        }
    }

    public Optional<IMethod> getOverridesContext() {
        if (overrides == null) {
            findOverridesContext();
        }
        return overrides;
    }

    private void findOverridesContext() {
        overrides = ctx.getEnclosingMethod();
        if (overrides.isPresent()) {
            IMethod root = findFirstDeclaration(overrides.get());
            overrides = of(root);
        }
    }

    public DefinitionKind getReceiverDefinitionType() {
        if (defKind == null) {
            findCalls();
            if (defKind == DefinitionKind.UNKNOWN) {
                // if the ast resolver could not find a definition of the variable, it's a method return value? Ask the
                // context and try.
                IMethodName def = ctx.getMethodDef().orNull();
                if (def == null) {
                    if (isReceiverNameImplicitThis() || isReceiverNameThis() || isReceiverNameSuper()) {
                        defKind = THIS;
                        setReceiverToSupertype();
                    } else {
                        defKind = FIELD;
                    }
                } else {
                    defKind = RETURN;
                    definedBy = def;
                }
            }
        }
        return defKind;
    }

    private void findCalls() {
        calls = Lists.newLinkedList();
        IMethod jdtMethod = ctx.getEnclosingMethod().orNull();
        if (jdtMethod == null) {
            return;
        }
        CompilationUnit ast = ctx.getAST();
        MethodDeclaration astMethod = ASTNodeUtils.find(ast, jdtMethod).orNull();
        if (astMethod == null) {
            return;
        }

        final AstDefUseFinder r = new AstDefUseFinder(receiverName, astMethod);
        calls = r.getCalls();
        defKind = r.getDefinitionKind();
        definedBy = r.getDefiningMethod().orNull();

        if (defKind == null) {
            // we may have triggered completion on a member access (e.g., PlatformUI.getWorkbench().|<>
            definedBy = ctx.getMethodDef().orNull();
            defKind = definedBy == null ? null : RETURN;
        }
    }

    public List<IMethodName> getCalls() {
        if (calls == null) {
            findCalls();
        }
        return calls;
    }

    public Optional<IMethodName> getDefinedBy() {
        return fromNullable(definedBy);
    }
}

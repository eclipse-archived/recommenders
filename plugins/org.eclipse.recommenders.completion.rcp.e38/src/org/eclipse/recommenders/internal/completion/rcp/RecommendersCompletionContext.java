/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("restriction")
public class RecommendersCompletionContext extends BaseRecommendersCompletionContext {

    public RecommendersCompletionContext(@Assisted final JavaContentAssistInvocationContext jdtContext,
            final IAstProvider astProvider) {
        super(jdtContext, astProvider);
    }

    @Override
    public ASTNode getCompletionNode() {
        return getCoreContext().getCompletionNode();
    }

    @Override
    public ASTNode getCompletionNodeParent() {
        return getCoreContext().getCompletionNodeParent();
    }

    @Override
    public List<IField> getVisibleFields() {
        final InternalCompletionContext ctx = getCoreContext();
        if (!ctx.isExtended()) {
            return Collections.emptyList();
        }
        final ObjectVector v = ctx.getVisibleFields();
        final List<IField> res = Lists.newArrayListWithCapacity(v.size);
        for (int i = v.size(); i-- > 0;) {
            final FieldBinding b = cast(v.elementAt(i));
            final Optional<IField> f = JdtUtils.createUnresolvedField(b);
            if (f.isPresent()) {
                res.add(f.get());
            }
        }
        return res;
    }

    @Override
    public List<ILocalVariable> getVisibleLocals() {
        final InternalCompletionContext ctx = getCoreContext();
        if (!ctx.isExtended()) {
            return Collections.emptyList();
        }
        final ObjectVector v = ctx.getVisibleLocalVariables();
        final List<ILocalVariable> res = Lists.newArrayListWithCapacity(v.size);
        for (int i = v.size(); i-- > 0;) {
            final LocalVariableBinding b = cast(v.elementAt(i));
            final JavaElement parent = (JavaElement) getEnclosingElement().get();
            final ILocalVariable f = JdtUtils.createUnresolvedLocaVariable(b, parent);
            res.add(f);
        }
        return res;
    }

    @Override
    public List<IMethod> getVisibleMethods() {
        final InternalCompletionContext ctx = getCoreContext();
        if (!ctx.isExtended()) {
            return Collections.emptyList();
        }
        final ObjectVector v = ctx.getVisibleMethods();
        final List<IMethod> res = Lists.newArrayListWithCapacity(v.size);
        for (int i = v.size(); i-- > 0;) {
            final MethodBinding b = cast(v.elementAt(i));
            final Optional<IMethod> f = JdtUtils.createUnresolvedMethod(b);
            if (f.isPresent()) {
                res.add(f.get());
            }
        }
        return res;
    }
}

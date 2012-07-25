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
package org.eclipse.recommenders.internal.completion.rcp.subwords2;

import static java.lang.String.valueOf;
import static org.eclipse.jdt.core.Signature.getReturnType;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContextFactory;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class AssignmentVarnameMatchCompletionProposalComputer extends JavaCompletionProposalComputer {

    private static final int BASIS_RELEVANCE = 1500;
    private final IRecommendersCompletionContextFactory ctxFactory;

    private IRecommendersCompletionContext ctx;

    @Inject
    public AssignmentVarnameMatchCompletionProposalComputer(final IRecommendersCompletionContextFactory ctxFactory) {
        this.ctxFactory = ctxFactory;
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context,
            final IProgressMonitor monitor) {
        ctx = ctxFactory.create((JavaContentAssistInvocationContext) context);
        return super.computeCompletionProposals(context, monitor);
    }

    @Override
    protected CompletionProposalCollector createCollector(final JavaContentAssistInvocationContext context) {

        ASTNode node = ctx.getCompletionNodeParent().orNull();
        final String varName;
        if (node instanceof Assignment) {
            varName = findVarName((Assignment) node);
        } else if (node instanceof LocalDeclaration) {
            varName = findVarName((LocalDeclaration) node);
        } else {
            varName = null;
        }
        if (varName == null) {
            return NullCompletionProposalCollector.NULL;
        }
        if (varName.length() < 3) {
            return NullCompletionProposalCollector.NULL;
        }

        CompletionProposalCollector c = new CompletionProposalCollector(context.getCompilationUnit()) {

            @Override
            public void accept(final CompletionProposal proposal) {
                char[] name = proposal.getName();
                if (name == null) {
                    return;
                }

                String s = valueOf(name).toLowerCase();
                if (s.contains(varName)) {
                    if (isVoidMethod(proposal)) {
                        return;
                    }

                    proposal.setRelevance(BASIS_RELEVANCE + proposal.getRelevance());
                    super.accept(proposal);
                }
            }

            private boolean isVoidMethod(final CompletionProposal proposal) {
                if (proposal.getKind() != CompletionProposal.METHOD_REF) {
                    return false;
                }

                char[] returnType = getReturnType(proposal.getSignature());
                if (Arrays.equals(returnType, new char[] { 'V' })) {
                    return true;
                }

                return false;
            }
        };
        return c;
    }

    private String findVarName(final LocalDeclaration node) {
        return toLowerCaseString(node.name);
    }

    String toLowerCaseString(final char[] name) {
        if (name == null) {
            return null;
        }
        return String.valueOf(name).toLowerCase();
    }

    private String findVarName(final Assignment node) {
        if (!(node.lhs instanceof SingleNameReference)) {
            return null;
        }
        SingleNameReference lhs = (SingleNameReference) node.lhs;
        return toLowerCaseString(lhs.token);
    }

    private static class NullCompletionProposalCollector extends CompletionProposalCollector {

        private static NullCompletionProposalCollector NULL = new NullCompletionProposalCollector();

        public NullCompletionProposalCollector() {
            super((IJavaProject) null);
        }

        @Override
        public void accept(final CompletionProposal proposal) {
            // no, just don't
        }
    }
}

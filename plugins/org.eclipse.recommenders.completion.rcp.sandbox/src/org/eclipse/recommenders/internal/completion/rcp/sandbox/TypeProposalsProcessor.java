/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.sandbox;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.eclipse.recommenders.internal.completion.rcp.ProcessableCompletionProposalComputer.NULL_PROPOSAL;
import static org.eclipse.recommenders.utils.names.VmPackageName.DEFAULT_PACKAGE;
import static org.eclipse.recommenders.utils.rcp.ast.BindingUtils.toPackageName;
import static org.eclipse.recommenders.utils.rcp.ast.BindingUtils.toPackageNames;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnFieldType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMethodReturnType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;
import org.eclipse.recommenders.internal.completion.rcp.SimpleProposalProcessor;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmPackageName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public final class TypeProposalsProcessor extends SessionProcessor {

    private static final SimpleProposalProcessor PKG = new SimpleProposalProcessor(1 << 6, "pkg");
    private static SimpleProposalProcessor EXACT = new SimpleProposalProcessor(1 << 25, "exact type");

    static final Set<Class<?>> SUPPORTED_COMPLETION_NODES = new HashSet<Class<?>>() {
        {
            add(CompletionOnMethodReturnType.class);
            add(CompletionOnSingleNameReference.class);
            add(CompletionOnFieldType.class);
            // List<String> l = new |^space
            add(CompletionOnSingleTypeReference.class);
        }
    };

    private Set<IPackageName> pkgs;
    private IType expectedType;
    private HashSet<String> expectedSubwords;
    private Set<ITypeName> expected;

    @Inject
    public TypeProposalsProcessor(JavaElementResolver jdtCache) {
    }

    @Override
    public void startSession(IRecommendersCompletionContext context) {
        pkgs = Sets.newHashSet();
        expectedSubwords = Sets.newHashSet();
        expectedType = context.getExpectedType().orNull();
        expected = context.getExpectedTypeNames();
        if (expectedType != null) {
            String[] split1 = expectedType.getElementName().split("(?=\\p{Upper})");
            for (String s : split1) {
                if (s.length() > 3) {
                    expectedSubwords.add(s);
                }
            }
        }

        ASTNode completion = context.getCompletionNode().orNull();
        if (completion == null || !SUPPORTED_COMPLETION_NODES.contains(completion.getClass())) {
            return;
        }

        CompilationUnit ast = context.getAST();
        ast.accept(new ASTVisitor() {

            @Override
            public boolean visit(PackageDeclaration node) {
                pkgs.add(toPackageName(node.resolveBinding()).or(DEFAULT_PACKAGE));
                return false;
            }

            @Override
            public boolean visit(SimpleType node) {
                pkgs.add(toPackageName(node.resolveBinding()).or(DEFAULT_PACKAGE));
                return super.visit(node);
            }

            @Override
            public boolean visit(SimpleName node) {
                ITypeBinding b = node.resolveTypeBinding();
                pkgs.add(toPackageName(b).or(DEFAULT_PACKAGE));
                return super.visit(node);
            }

            @Override
            public boolean visit(SuperMethodInvocation node) {
                return visit(node.resolveMethodBinding());
            }

            @Override
            public boolean visit(MethodInvocation node) {
                return visit(node.resolveMethodBinding());
            }

            private boolean visit(IMethodBinding b) {
                if (b == null) return true;
                pkgs.add(toPackageName(b.getReturnType()).or(DEFAULT_PACKAGE));
                pkgs.addAll(toPackageNames(b.getParameterTypes()));
                return true;
            }
        });
        // remove omnipresent package
        pkgs.remove(VmPackageName.get("java/lang"));
    }

    @Override
    public void process(IProcessableProposal proposal) throws JavaModelException {

        final CompletionProposal coreProposal = proposal.getCoreProposal().or(NULL_PROPOSAL);
        switch (coreProposal.getKind()) {
        case CompletionProposal.CONSTRUCTOR_INVOCATION:
        case CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
            handleConstructorProposal(proposal, coreProposal);
            break;
        case CompletionProposal.METHOD_REF:
        case CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER:
            handleMethodProposal(proposal, coreProposal);
            break;
        case CompletionProposal.FIELD_REF:
        case CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER:
        case CompletionProposal.LOCAL_VARIABLE_REF:
            handleVariableProposal(proposal, coreProposal);
            break;
        case CompletionProposal.TYPE_REF:
        case CompletionProposal.TYPE_IMPORT:
            handleTypeProposal(proposal, coreProposal);
            break;
        }
    }

    private void handleVariableProposal(IProcessableProposal proposal, CompletionProposal variableProposal) {
        if (expected.isEmpty()) return;
        String sig = new String(variableProposal.getSignature());
        sig = sig.replace('.', '/');
        sig = StringUtils.removeEnd(sig, ";");
        ITypeName type = VmTypeName.get(sig);
        if (expected.contains(type)) {
            proposal.getProposalProcessorManager().addProcessor(EXACT);
        }
    }

    private void handleMethodProposal(IProcessableProposal proposal, CompletionProposal coreProposal) {
        if (expected.isEmpty()) return;
        try {
            String methodSig = new String(coreProposal.getSignature());
            String returnType = Signature.getReturnType(methodSig);
            returnType = returnType.replace('.', '/');
            returnType = StringUtils.removeEnd(returnType, ";");
            ITypeName type = VmTypeName.get(returnType);
            if (expected.contains(type)) {
                ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
                mgr.addProcessor(EXACT);
            }
        } catch (Exception e) {
            // generic TT causes excption
            e.printStackTrace();
        }
    }

    private void handleTypeProposal(IProcessableProposal proposal, final CompletionProposal coreProposal) {
        String sig = new String(coreProposal.getSignature());
        sig = sig.replace('.', '/');
        sig = StringUtils.removeEnd(sig, ";");
        ITypeName type = VmTypeName.get(sig);
        IPackageName pkg = type.getPackage();
        if (pkgs.contains(pkg)) {
            ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
            mgr.addProcessor(PKG);
        }
    }

    private void handleConstructorProposal(IProcessableProposal proposal, final CompletionProposal coreProposal) {
        String name = removeEnd(valueOf(coreProposal.getDeclarationSignature()).replace('.', '/'), ";");
        VmTypeName recType = VmTypeName.get(name);
        ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
        if (pkgs.contains(recType.getPackage())) {
            mgr.addProcessor(PKG);
        }
        if (expectedType != null) {
            Set<String> s2 = Sets.newHashSet(recType.getClassName().split("(?=\\p{Upper})"));
            final SetView<String> intersection = Sets.intersection(s2, expectedSubwords);

            if (!intersection.isEmpty()) {
                SimpleProposalProcessor p = new SimpleProposalProcessor(intersection.size(), "partial");
                mgr.addProcessor(p);
            }
        }
    }
}

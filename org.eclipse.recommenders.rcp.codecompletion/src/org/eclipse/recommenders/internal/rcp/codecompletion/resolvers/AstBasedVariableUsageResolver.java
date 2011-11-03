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
package org.eclipse.recommenders.internal.rcp.codecompletion.resolvers;

import static org.eclipse.recommenders.commons.udc.ObjectUsage.NO_METHOD;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.UNKNOWN_METHOD;
import static org.eclipse.recommenders.commons.utils.Checks.cast;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;
import org.eclipse.recommenders.rcp.utils.ast.BindingUtils;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class AstBasedVariableUsageResolver implements IVariableUsageResolver {

	private final IAstProvider astprovider;
	private int invocationOffset;

	private CompilationUnit ast;

	private MethodDeclaration astEnclosingMethodDeclaration;

	private ICompilationUnit jdtCompilationUnit;

	private IMethod jdtEnclosingMethodDeclaration;

	private Variable localVariable;
	private Kind localVariableKind;
	private final Set<IMethodName> receiverMethodInvocations = Sets.newHashSet();
	private IIntelligentCompletionContext ctx;
	private DefinitionSite.Kind receiverDefinitionKind;

	private IMethodName receiverDefinition;

	@Inject
	public AstBasedVariableUsageResolver(final IAstProvider provider) {
		astprovider = provider;

	}

	@Override
	public boolean canResolve(final IIntelligentCompletionContext ctx) {
		ensureIsNotNull(ctx);
		this.ctx = ctx;
		this.localVariable = ctx.getVariable();
		this.jdtCompilationUnit = ctx.getCompilationUnit();
		this.invocationOffset = ctx.getInvocationOffset();
		if (!findAst()) {
			return false;
		}
		if (!findEnclosingMethodDeclaration()) {
			return false;
		}
		return findUsages();
	}

	private boolean findAst() {

		ast = astprovider.get(jdtCompilationUnit);
		return ast != null;
	}

	private boolean findEnclosingMethodDeclaration() {
		ensureIsNotNull(ast);
		final int fixedInvocationOffset = getFixedOffsetForOldAsts();
		ASTNode node = NodeFinder.perform(ast, fixedInvocationOffset, 0);
		while (node != null) {
			if (node instanceof MethodDeclaration) {
				astEnclosingMethodDeclaration = cast(node);
				jdtEnclosingMethodDeclaration = BindingUtils.getMethod(astEnclosingMethodDeclaration.resolveBinding());
				break;
			}
			node = node.getParent();
		}
		return jdtEnclosingMethodDeclaration != null;
	}

	/**
	 * Due to timing issues of the CachingAstProvider it might happen that we work on a deprecated version of the AST.
	 * This happens on fast typing. This method gets an invocation offset that should work with the previous version of
	 * the AST. This is done by subtracting variable name length of the local + prefix length from the original
	 * invocation offset.
	 */
	private int getFixedOffsetForOldAsts() {
		if (localVariable.getNameLiteral() != null) {
			final int varNameLength = localVariable.getNameLiteral().length();
			final int prefixLength = ctx.getPrefixToken().length();
			return invocationOffset - (varNameLength + prefixLength);
		} else {
			return invocationOffset;
		}
	}

	private boolean findUsages() {
		ensureIsNotNull(astEnclosingMethodDeclaration);
		astEnclosingMethodDeclaration.accept(new ASTVisitor() {

			@Override
			public boolean visit(final SimpleName node) {
				final IVariableBinding var = BindingUtils.getVariableBinding(node);
				if (var == null) {
					return true;
				}
				if (var.getName().equals(localVariable.getNameLiteral())) {
					determineVariableKind(var);
					final ASTNode parent = node.getParent();
					if (parent instanceof MethodInvocation) {
						final MethodInvocation methodInvocation = (MethodInvocation) parent;
						registerMethodCallOnReceiver(methodInvocation);
					} else if (parent instanceof Assignment) {
						final Assignment assignment = (Assignment) parent;
						evaluateDefinitionByAssignment(assignment.getRightHandSide());
					} else if (parent instanceof VariableDeclarationFragment) {
						final VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) parent;
						final VariableDeclarationStatement declarationStatement = (VariableDeclarationStatement) declarationFragment
								.getParent();
						final List<?> fragments = declarationStatement.fragments();
						final VariableDeclarationFragment lastFragment = (VariableDeclarationFragment) fragments
								.get(fragments.size() - 1);
						evaluateDefinitionByAssignment(lastFragment.getInitializer());
					}
				}
				return true;
			}

			private void determineVariableKind(final IVariableBinding var) {

				if (var.isParameter()) {
					localVariableKind = ObjectInstanceKey.Kind.PARAMETER;
					receiverDefinitionKind = DefinitionSite.Kind.PARAMETER;
					receiverDefinition = NO_METHOD;
				} else if (var.isField()) {
					localVariableKind = Kind.FIELD;
					receiverDefinitionKind = DefinitionSite.Kind.FIELD;
					receiverDefinition = NO_METHOD;
				} else {
					localVariableKind = Kind.LOCAL;
					receiverDefinitionKind = DefinitionSite.Kind.UNKNOWN;
					receiverDefinition = UNKNOWN_METHOD;
				}
			}

			private void evaluateDefinitionByAssignment(final ASTNode node) {
				if (node instanceof ClassInstanceCreation) {
					ClassInstanceCreation creation = (ClassInstanceCreation) node;
					localVariableKind = Kind.LOCAL;
					receiverDefinitionKind = DefinitionSite.Kind.NEW;
					receiverDefinition = BindingUtils.toMethodName(creation.resolveConstructorBinding());
				} else if (node instanceof MethodInvocation) {
					final MethodInvocation methodInv = (MethodInvocation) node;
					receiverDefinitionKind = DefinitionSite.Kind.METHOD_RETURN;
					receiverDefinition = BindingUtils.toMethodName(methodInv.resolveMethodBinding());
				} else if (node instanceof SuperMethodInvocation) {
					final SuperMethodInvocation methodInv = (SuperMethodInvocation) node;
					receiverDefinitionKind = DefinitionSite.Kind.METHOD_RETURN;
					receiverDefinition = BindingUtils.toMethodName(methodInv.resolveMethodBinding());
				} else if (node instanceof ParenthesizedExpression) {
					final ParenthesizedExpression pExp = (ParenthesizedExpression) node;
					evaluateDefinitionByAssignment(pExp.getExpression());
				} else if (node instanceof ConditionalExpression) {
					final ConditionalExpression cond = (ConditionalExpression) node;
					evaluateDefinitionByAssignment(cond.getThenExpression());
					evaluateDefinitionByAssignment(cond.getElseExpression());
				}
			
				if(receiverDefinition == null) {
					receiverDefinition = UNKNOWN_METHOD;
				}
			}

			private void registerMethodCallOnReceiver(final MethodInvocation invoke) {
				final IMethodBinding b = invoke.resolveMethodBinding();
				final IMethodName method = BindingUtils.toMethodName(b);
				if (method != null) {
					receiverMethodInvocations.add(method);
				}
			}
		});
		return true;
	}

	@Override
	public Set<IMethodName> getReceiverMethodInvocations() {
		return receiverMethodInvocations;
	}

	@Override
	public Variable getResolvedVariable() {
		final Variable res = Variable.create(localVariable.getNameLiteral(), localVariable.getType(),
				localVariable.getReferenceContext());
		res.kind = localVariableKind;
		return res;
	}

	@Override
	public DefinitionSite.Kind getResolvedVariableKind() {
		if(receiverDefinitionKind == null) {
			return DefinitionSite.Kind.UNKNOWN;
		} else {
			return receiverDefinitionKind;
		}
	}

	@Override
	public IMethodName getResolvedVariableDefinition() {
		if(receiverDefinition == null) {
			return UNKNOWN_METHOD;
		} else {
			return receiverDefinition;
		}
	}
}

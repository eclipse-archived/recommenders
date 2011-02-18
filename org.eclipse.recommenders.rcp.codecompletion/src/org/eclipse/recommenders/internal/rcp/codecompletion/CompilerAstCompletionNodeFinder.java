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
package org.eclipse.recommenders.internal.rcp.codecompletion;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnFieldType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnLocalName;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMessageSend;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.IfStatement;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.Javadoc;
import org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArgumentExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocArraySingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocImplicitTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.JavadocQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
import org.eclipse.jdt.internal.compiler.ast.SuperReference;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.recommenders.commons.utils.annotations.Clumsy;

import com.google.common.collect.Sets;

@Clumsy
@SuppressWarnings("restriction")
public class CompilerAstCompletionNodeFinder extends ASTVisitor {

    /**
     * The JDT completion node created by the completion completion parser.
     * 
     * @see one of org.eclipse.jdt.internal.codeassist.complete
     * @see CompletionOnMessageSend
     * @see CompletionOnQualifiedNameReference
     * @see CompletionOnSingleNameReference
     */
    public Statement completionNode;

    /**
     * The type of the receiver this completion event was triggered on, e.g,
     * Button b = ...; b.|&lt;ctrl-space&gt; would set {@link #receiverType} to
     * <code>Button</code>.
     */
    public TypeBinding receiverType;

    /**
     * The name of the receiver - if it has one. When triggering code completion
     * on <code>b.|&lt;ctrl-space&gt;</code> then {@link #receiverName} is 'b'.
     * However, if code completion has been triggered on an implicit method
     * return value like {@code getB().|&lt;ctrl-space&gt;} then
     * {@link #receiverName} is null.
     * <p>
     * If code completion is triggered on a type like <code>PlatformUI</code>
     * this variable holds the name of the type. <b>NOTE:</b> in the case of
     * single names like <code>PlatformUI|&lt;^Space&gt </code> or
     * <code>varName|&lt;^Space&gt</code> the reveiver type is typically NOT
     * set! Be careful!
     */
    public String receiverName = "";

    /**
     * If {@link #expectsReturnType} is true, this completion request requires
     * the completion to define a new local, i.e., in the case of method calls
     * to have a return value.
     */
    public boolean expectsReturnType;

    /**
     * if {@link #expectsReturnType} is true, then this value <b>might</b> hold
     * a type binding of the expected return type. However, this might not be as
     * easy and work in all cases and thus may be <code>null</code> even if
     * {@link #expectsReturnType} is true;
     */
    public TypeBinding expectedReturnType;

    /**
     * This field is set whenever a completion is triggered on a type name like
     * 'Button' etc.
     * <p>
     * Example:
     * 
     * <pre>
     * void someMethod(){
     *     Button|&|&lt;ctrl-space&gt;
     * }
     * </pre>
     */
    public TypeBinding requestedTypeCompletion;

    public boolean expectsStaticMember;

    /**
     * If the code completion event occurs as a method argument guessing
     * completion (i.e., b.method(|&lt;ctrl-space&gt;), then
     * {@link IntelligentCompletionContext#enclosingMethodCallSelector} contains
     * the (unresolved and potentially ambiguous) name of the method call
     * enclosing this completion event.
     * 
     * <p>
     * Example:
     * 
     * <pre>
     * methodCall(|&lt;ctrl-space&gt;) // gives "methodCall"
     * </pre>
     */
    public String enclosingMethodCallSelector;

    /**
     * If the code completion event occurs as a method argument guessing as
     * indicated by {@link #enclosingMethodCallSelector} being not
     * <code>null</code>, this field holds the type binding that declares the
     * enclosing method.
     */
    public TypeBinding declaringTypeOfEnclosingMethodCall;

    /**
     * If code completion was triggered on an implicit method return value, this
     * field stores the method binding that defined this implicit (and unnamed)
     * local variable.
     * <p>
     * Example;
     * 
     * <pre>
     * getX().|&lt;ctrl-space&gt;  // evaluates to a binding for method "getX"
     * </pre>
     */
    public MethodBinding receiverDefinedByMethodReturn;

    public MethodScope scope;

    public final Set<FieldDeclaration> fieldDeclarations = Sets.newHashSet();
    public final Set<LocalDeclaration> localDeclarations = Sets.newHashSet();

    public void clearState() {
        receiverDefinedByMethodReturn = null;
        completionNode = null;
        declaringTypeOfEnclosingMethodCall = null;
        enclosingMethodCallSelector = null;
        expectedReturnType = null;
        expectsStaticMember = false;
        receiverName = null;
        receiverType = null;
        requestedTypeCompletion = null;
    }

    @Override
    public boolean visit(final SingleNameReference singleNameReference, final BlockScope scope) {
        if (singleNameReference instanceof CompletionOnSingleNameReference) {
            final CompletionOnSingleNameReference node = storeCompletionNode(singleNameReference);
            evaluateCompletionOnSingleNameReference(node);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private <T extends Statement> T storeCompletionNode(final Statement statement) {
        completionNode = statement;
        return (T) statement;
    }

    private void evaluateCompletionOnSingleNameReference(final CompletionOnSingleNameReference completion) {
        // XXX this is actually not resolving any binding:
        receiverType = completion.resolvedType;
        receiverName = String.valueOf(completion.token);
    }

    @Override
    public boolean visit(final QualifiedNameReference qualifiedNameReference, final BlockScope scope) {
        if (qualifiedNameReference instanceof CompletionOnQualifiedNameReference) {
            final CompletionOnQualifiedNameReference node = storeCompletionNode(qualifiedNameReference);
            evaluateCompletionOnQualifiedNameReference(node);
            return false;
        }
        return true;
    }

    private void evaluateCompletionOnQualifiedNameReference(final CompletionOnQualifiedNameReference c) {
        switch (c.binding.kind()) {
        case Binding.VARIABLE:
        case Binding.FIELD:
        case Binding.LOCAL:
            final VariableBinding varBinding = (VariableBinding) c.binding;
            evaluateVariableBindingAsReceiver(varBinding);
            return;

        case Binding.TYPE:
            // e.g. PlatformUI.|<ctrl-space>
            final TypeBinding typeBinding = (TypeBinding) c.binding;
            receiverType = typeBinding;
            expectsStaticMember = true;
            return;
        default:
            /**
             * triggering code completion on an err pos like:
             * 
             * <pre>
             *      b.|&lt;^Space&gt;
             *      final Button b = new Button(parent, 0);
             * 
             * </pre>
             * 
             * TODO is this appropriate? Do we want to handle these events? or
             * just discard error situations?
             */
            // if (c.binding instanceof ProblemBinding) {
            // final ProblemBinding problem = cast(c.binding);
            // receiverName = String.valueOf(problem.name);
            // receiverType = problem.searchType;
            // }
            clearState();
        }
    }

    private void evaluateVariableBindingAsReceiver(final VariableBinding binding) {
        ensureIsNotNull(binding);
        receiverName = new String(binding.name);
        receiverType = binding.type;
    }

    @Override
    public boolean visit(final MessageSend messageSend, final BlockScope scope) {
        if (messageSend instanceof CompletionOnMessageSend) {
            final CompletionOnMessageSend node = storeCompletionNode(messageSend);
            evaluateCompletionOnMessageSend(node);
            return false;
        }
        return true;
    }

    private void evaluateCompletionOnMessageSend(final CompletionOnMessageSend c) {
        declaringTypeOfEnclosingMethodCall = c.actualReceiverType;
        enclosingMethodCallSelector = new String(c.selector);
        expectsReturnType = true;
    }

    @Override
    public boolean visit(final FieldReference fieldReference, final BlockScope scope) {
        if (fieldReference instanceof CompletionOnMemberAccess) {
            final CompletionOnMemberAccess node = storeCompletionNode(fieldReference);
            evaluateCompletionOnMemberAccess(node);
            return false;
        }
        return true;
    }

    private void evaluateCompletionOnMemberAccess(final CompletionOnMemberAccess c) {
        // what is the actual receiver type we are asked to create a completion
        // for (i.e., the type returned by the members method return type?
        receiverType = c.actualReceiverType;
        // since we are navigating through the API call graph this receiver
        // either is 'this' or has
        // no name.
        if (c.receiver instanceof ThisReference) {
            // NOTE simply calling 'c.isThis()' doesn't work;
            evaluateThisReferenceAsReceiver((ThisReference) c.receiver);
        } else if (c.receiver instanceof MessageSend) {
            evaluteMessageSendAsDefForAnonymousReceiver((MessageSend) c.receiver);
        } else if (c.fieldBinding() != null) {
            // does this happen? When?
            evaluateVariableBindingAsReceiver(c.fieldBinding());
        } else if (c.localVariableBinding() != null) {
            // does this happen? when?
            evaluateVariableBindingAsReceiver(c.localVariableBinding());
        }
    }

    private void evaluateThisReferenceAsReceiver(final ThisReference ref) {
        receiverName = "this";
        receiverType = ref.resolvedType;
    }

    /**
     * <pre>
     *     public Activator() {
     *         b.getLocation().|&lt;ctrl-space&gt;
     *     }
     * </pre>
     * 
     * @param m
     */
    private void evaluteMessageSendAsDefForAnonymousReceiver(final MessageSend m) {
        receiverDefinedByMethodReturn = m.binding;
        receiverType = m.binding.returnType;
        receiverName = "";
    }

    @Override
    public boolean visit(final LocalDeclaration localDeclaration, final BlockScope scope) {
        if (localDeclaration instanceof CompletionOnLocalName) {
            final CompletionOnLocalName node = storeCompletionNode(localDeclaration);
            evaluateCompletionOnLocalName(node);
        } else if (isCompletionOnVariableInitialization(localDeclaration.initialization)) {
            expectedReturnType = localDeclaration.binding.type;
        } else {
            // we only add this declaration if it's "complete".
            // Var c = c doesn't make sense, right?
            localDeclarations.add(localDeclaration);
        }
        return true;
    }

    private void evaluateCompletionOnLocalName(final CompletionOnLocalName c) {
        if (c.binding != null) {
            expectedReturnType = c.binding.type;
            // TODO this is actually not correct! Need to fix the pattern
            // template stuff which expects receiver type
            // being set!
            receiverType = expectedReturnType;
        }
        receiverName = String.valueOf(c.name);
        expectsReturnType = true;
    }

    @Override
    public boolean visit(final FieldDeclaration fieldDeclaration, final MethodScope scope) {
        if (fieldDeclaration instanceof CompletionOnFieldType) {
            storeCompletionNode(fieldDeclaration);
            return false;
        }
        if (isCompletionOnVariableInitialization(fieldDeclaration.initialization)) {
            expectedReturnType = fieldDeclaration.binding.type;
        } else {
            // we only add this declaration if it's "complete".
            // Var c = c doesn't make sense, right?
            fieldDeclarations.add(fieldDeclaration);
        }
        return true;
    }

    private boolean isCompletionOnVariableInitialization(final Expression initialization) {
        return initialization instanceof CompletionOnSingleNameReference
                || initialization instanceof CompletionOnQualifiedNameReference
                || initialization instanceof CompletionOnMemberAccess;
    }

    public boolean isCompletionNodeFound() {
        return completionNode != null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean visit(final AllocationExpression allocationExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final AND_AND_Expression and_and_Expression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final AnnotationMethodDeclaration annotationTypeDeclaration, final ClassScope classScope) {
        return true;
    }

    @Override
    public boolean visit(final Argument argument, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Argument argument, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayAllocationExpression arrayAllocationExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayInitializer arrayInitializer, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayQualifiedTypeReference arrayQualifiedTypeReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayQualifiedTypeReference arrayQualifiedTypeReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayReference arrayReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayTypeReference arrayTypeReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ArrayTypeReference arrayTypeReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final AssertStatement assertStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Assignment assignment, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final BinaryExpression binaryExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Block block, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final BreakStatement breakStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final CaseStatement caseStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final CastExpression castExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final CharLiteral charLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ClassLiteralAccess classLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Clinit clinit, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final CompilationUnitDeclaration compilationUnitDeclaration, final CompilationUnitScope scope) {
        return true;
    }

    @Override
    public boolean visit(final CompoundAssignment compoundAssignment, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ConditionalExpression conditionalExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ConstructorDeclaration constructorDeclaration, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ContinueStatement continueStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final DoStatement doStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final DoubleLiteral doubleLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final EmptyStatement emptyStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final EqualExpression equalExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ExplicitConstructorCall explicitConstructor, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ExtendedStringLiteral extendedStringLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final FalseLiteral falseLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final FieldReference fieldReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final FloatLiteral floatLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ForeachStatement forStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ForStatement forStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final IfStatement ifStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ImportReference importRef, final CompilationUnitScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Initializer initializer, final MethodScope scope) {
        return true;
    }

    @Override
    public boolean visit(final InstanceOfExpression instanceOfExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final IntLiteral intLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Javadoc javadoc, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Javadoc javadoc, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocAllocationExpression expression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocAllocationExpression expression, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocArgumentExpression expression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocArgumentExpression expression, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocArrayQualifiedTypeReference typeRef, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocArrayQualifiedTypeReference typeRef, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocArraySingleTypeReference typeRef, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocArraySingleTypeReference typeRef, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocFieldReference fieldRef, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocFieldReference fieldRef, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocImplicitTypeReference implicitTypeReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocImplicitTypeReference implicitTypeReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocMessageSend messageSend, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocMessageSend messageSend, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocQualifiedTypeReference typeRef, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocQualifiedTypeReference typeRef, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocReturnStatement statement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocReturnStatement statement, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocSingleNameReference argument, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocSingleNameReference argument, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocSingleTypeReference typeRef, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final JavadocSingleTypeReference typeRef, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final LabeledStatement labeledStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final LongLiteral longLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final MarkerAnnotation annotation, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final MemberValuePair pair, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final MethodDeclaration methodDeclaration, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final StringLiteralConcatenation literal, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final NormalAnnotation annotation, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final NullLiteral nullLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final OR_OR_Expression or_or_Expression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference,
            final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference,
            final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ParameterizedSingleTypeReference parameterizedSingleTypeReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ParameterizedSingleTypeReference parameterizedSingleTypeReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final PostfixExpression postfixExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final PrefixExpression prefixExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedAllocationExpression qualifiedAllocationExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedNameReference qualifiedNameReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedSuperReference qualifiedSuperReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedSuperReference qualifiedSuperReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedThisReference qualifiedThisReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedThisReference qualifiedThisReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedTypeReference qualifiedTypeReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final QualifiedTypeReference qualifiedTypeReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ReturnStatement returnStatement, final BlockScope scope) {
        if (isCompletionOnVariableInitialization(returnStatement.expression)) {
            if (scope.referenceContext() instanceof AbstractMethodDeclaration) {
                final AbstractMethodDeclaration referenceContext = (AbstractMethodDeclaration) scope.referenceContext();
                expectedReturnType = referenceContext.binding.returnType;
            }
        }
        return true;
    }

    @Override
    public boolean visit(final SingleMemberAnnotation annotation, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final SingleNameReference singleNameReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final SingleTypeReference singleTypeReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final SingleTypeReference singleTypeReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final StringLiteral stringLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final SuperReference superReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final SwitchStatement switchStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final SynchronizedStatement synchronizedStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ThisReference thisReference, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ThisReference thisReference, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final ThrowStatement throwStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TrueLiteral trueLiteral, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TryStatement tryStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TypeDeclaration localTypeDeclaration, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TypeDeclaration memberTypeDeclaration, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TypeDeclaration typeDeclaration, final CompilationUnitScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TypeParameter typeParameter, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final TypeParameter typeParameter, final ClassScope scope) {
        return true;
    }

    @Override
    public boolean visit(final UnaryExpression unaryExpression, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final WhileStatement whileStatement, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Wildcard wildcard, final BlockScope scope) {
        return true;
    }

    @Override
    public boolean visit(final Wildcard wildcard, final ClassScope scope) {
        return true;
    }

}

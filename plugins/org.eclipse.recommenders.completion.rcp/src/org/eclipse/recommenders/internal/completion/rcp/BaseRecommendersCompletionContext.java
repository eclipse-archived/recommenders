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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnLocalName;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public abstract class BaseRecommendersCompletionContext implements IRecommendersCompletionContext {

    private final JavaContentAssistInvocationContext javaContext;
    private final InternalCompletionContext coreContext;

    public BaseRecommendersCompletionContext(final JavaContentAssistInvocationContext jdtContext) {
        this.javaContext = jdtContext;
        this.coreContext = cast(jdtContext.getCoreContext());
    }

    public InternalCompletionContext getCoreContext() {
        return coreContext;
    }

    // public Optional<InternalExtendedCompletionContext> getExtendedContext() {
    // try {
    // final Field ctxField = coreContext.getClass().getDeclaredField("extendedContext");
    // ctxField.setAccessible(true);
    // final InternalExtendedCompletionContext extendedContext = cast(ctxField.get(coreContext));
    // final Field cuField = extendedContext.getClass().getDeclaredField("compilationUnitDeclaration");
    // cuField.setAccessible(true);
    // final CompilationUnitDeclaration cu = cast(cuField.get(extendedContext));
    // return fromNullable(extendedContext);
    // } catch (final Exception e) {
    // throw throwUnhandledException(e);
    // }
    // }

    @Override
    public JavaContentAssistInvocationContext getJavaContext() {
        return javaContext;
    }

    @Override
    public IJavaProject getProject() {
        return javaContext.getProject();
    };

    @Override
    public int getInvocationOffset() {
        return javaContext.getInvocationOffset();
    }

    @Override
    public Region getReplacementRange() {
        final int offset = getInvocationOffset();
        final int length = getPrefix().length();
        return new Region(offset, length);
    }

    @Override
    public Optional<IMethod> getEnclosingMethod() {
        final IJavaElement enclosing = getEnclosingElement().orNull();
        if (enclosing instanceof IMethod) {
            return of((IMethod) enclosing);
        } else {
            return absent();
        }
    }

    @Override
    public Optional<IType> getEnclosingType() {
        final IJavaElement enclosing = getEnclosingElement().orNull();
        if (enclosing instanceof IType) {
            return of((IType) enclosing);
        } else {
            return absent();
        }
    }

    @Override
    public Optional<IJavaElement> getEnclosingElement() {
        if (coreContext.isExtended()) {
            return of(coreContext.getEnclosingElement());
        }
        return absent();
    }

    @Override
    public boolean hasEnclosingElement() {
        return getEnclosingElement().isPresent();
    }

    @Override
    public Optional<IType> getClosestEnclosingType() {
        if (!hasEnclosingElement()) {
            absent();
        }
        final IJavaElement enclosing = getEnclosingElement().get();
        if (enclosing instanceof IType) {
            return of((IType) enclosing);
        } else {
            final IType type = (IType) enclosing.getAncestor(IJavaElement.TYPE);
            return fromNullable(type);
        }
    }

    @Override
    public boolean isCompletionInMethodBody() {
        return getEnclosingMethod().isPresent();
    }

    @Override
    public boolean isCompletionInTypeBody() {
        return getEnclosingType().isPresent();
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
        return javaContext.getCompilationUnit();
    }

    @Override
    public CompilationUnit getAST() {
        try {
            final ICompilationUnit cu = getCompilationUnit();
            // XXX WTH? We have to do this twice - at least for e3.8 milestones???
            internal_getAst(cu);
            final CompilationUnit ast = internal_getAst(cu);
            return ast;
        } catch (final JavaModelException e) {
            // this should not happen since the underlying resource typically exists.
            // Thus, re-throw excepotion but unchecked
            throw throwUnhandledException(e);
        }
    }

    private CompilationUnit internal_getAst(final ICompilationUnit cu) throws JavaModelException {
        final CompilationUnit ast = cu.reconcile(AST.JLS4, true, true, new WorkingCopyOwner() {
            @Override
            public IProblemRequestor getProblemRequestor(final ICompilationUnit workingCopy) {
                return new IProblemRequestor() {

                    @Override
                    public boolean isActive() {
                        // TODO XXX this is important:
                        // Otherwise no bindings are resolved.
                        return true;
                    }

                    @Override
                    public void endReporting() {
                    }

                    @Override
                    public void beginReporting() {
                    }

                    @Override
                    public void acceptProblem(final IProblem problem) {
                    }
                };
            }
        }, null);
        return ast;
    }

    @Override
    public Optional<String> getExpectedTypeSignature() {
        final char[][] keys = coreContext.getExpectedTypesKeys();
        if (keys == null) {
            return absent();
        }
        if (keys.length < 1) {
            return absent();
        }
        final String res = new String(keys[0]);
        return of(res);
    }

    @Override
    public Optional<IType> getExpectedType() {
        final IType res = javaContext.getExpectedType();
        return fromNullable(res);
    }

    @Override
    public String getPrefix() {
        final char[] token = coreContext.getToken();
        if (token == null) {
            return "";
        }
        return new String(token);
    }

    @Override
    public String getReceiverName() {

        final ASTNode n = getCompletionNode();
        char[] name = null;
        if (n instanceof CompletionOnQualifiedNameReference) {
            final CompletionOnQualifiedNameReference c = cast(n);
            switch (c.binding.kind()) {
            case Binding.VARIABLE:
            case Binding.FIELD:
            case Binding.LOCAL:
                final VariableBinding b = (VariableBinding) c.binding;
                name = b.name;
                break;
            }
        } else if (n instanceof CompletionOnLocalName) {
            final CompletionOnLocalName c = cast(n);
            name = c.realName;
        } else if (n instanceof CompletionOnSingleNameReference) {
            final CompletionOnSingleNameReference c = cast(n);
            name = c.token;
        } else if (n instanceof CompletionOnMemberAccess) {
            final CompletionOnMemberAccess c = cast(n);
            if (c.receiver instanceof ThisReference) {
                name = "this".toCharArray();
            } else if (c.receiver instanceof MessageSend) {
                // some anonymous type/method return value that has no name... e.g.:
                // PlatformUI.getWorkbench()|^Space --> receiver is anonymous --> name = null
                name = null;
            } else if (c.fieldBinding() != null) {
                // does this happen? When?
                name = c.fieldBinding().name;
            } else if (c.localVariableBinding() != null) {
                // does this happen? when?
                name = c.localVariableBinding().name;
            }
        }
        return toString(name);
    }

    private String toString(final char[] name) {
        if (name == null) {
            return "";
        }
        // remove all whitespaces:
        return new String(name).replace(" ", "");
    }

    @Override
    public Optional<String> getReceiverTypeSignature() {
        final Optional<TypeBinding> opt = findReceiverTypeBinding();
        return toString(opt.orNull());
    }

    private Optional<TypeBinding> findReceiverTypeBinding() {
        final ASTNode n = getCompletionNode();
        TypeBinding receiver = null;
        if (n instanceof CompletionOnLocalName) {
            // final CompletionOnLocalName c = cast(n);
            // name = c.realName;
        } else if (n instanceof CompletionOnSingleNameReference) {
            final CompletionOnSingleNameReference c = cast(n);
            receiver = c.resolvedType;
        } else if (n instanceof CompletionOnQualifiedNameReference) {
            final CompletionOnQualifiedNameReference c = cast(n);
            switch (c.binding.kind()) {
            case Binding.VARIABLE:
            case Binding.FIELD:
            case Binding.LOCAL:
                final VariableBinding varBinding = (VariableBinding) c.binding;
                receiver = varBinding.type;
                break;
            case Binding.TYPE:
                // e.g. PlatformUI.|<ctrl-space>
                receiver = (TypeBinding) c.binding;
                break;
            default:
            }
        } else if (n instanceof CompletionOnMemberAccess) {
            final CompletionOnMemberAccess c = cast(n);
            receiver = c.actualReceiverType;
        }
        return fromNullable(receiver);
    }

    private Optional<String> toString(final TypeBinding receiver) {
        if (receiver == null) {
            return absent();
        }
        final String res = new String(receiver.signature());
        return of(res);
    }

    @Override
    public Optional<IType> getReceiverType() {
        final Optional<TypeBinding> opt = findReceiverTypeBinding();
        if (opt.isPresent()) {
            return of(JdtUtils.createUnresolvedType(opt.get()));
        }
        return absent();
    }
}

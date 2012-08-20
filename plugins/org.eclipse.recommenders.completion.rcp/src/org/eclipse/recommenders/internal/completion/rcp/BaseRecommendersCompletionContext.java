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
package org.eclipse.recommenders.internal.completion.rcp;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnLocalName;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedAllocationExpression;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.MissingTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.CompilerBindings;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;

public abstract class BaseRecommendersCompletionContext implements IRecommendersCompletionContext {

    public static ASTNode NULL = new ASTNode() {

        @Override
        public StringBuffer print(int indent, StringBuffer output) {
            return output;
        }
    };
    private static Field fAssistScope;
    private static Field fAssistNode;
    private static Field fAssistNodeParent;
    private static Field fCompilationUnitDeclaration;
    private static Field fExtendedContext;

    private final class TimeoutProgressMonitor extends NullProgressMonitor {
        long limit = System.currentTimeMillis() + 5000;

        @Override
        public boolean isCanceled() {
            return System.currentTimeMillis() - limit > 0;
        }
    }

    private final JavaContentAssistInvocationContext javaContext;
    private InternalCompletionContext coreContext;
    private final IAstProvider astProvider;
    private ProposalCollectingCompletionRequestor collector;
    private InternalExtendedCompletionContext extCoreContext;
    private ASTNode assistNode;
    private ASTNode assistNodeParent;
    private Scope assistScope;
    private CompilationUnitDeclaration compilationUnitDeclaration;

    public BaseRecommendersCompletionContext(final JavaContentAssistInvocationContext jdtContext,
            final IAstProvider astProvider) {
        this.javaContext = jdtContext;
        this.astProvider = astProvider;
        this.coreContext = cast(jdtContext.getCoreContext());
        requestExtendedContext();
        initializeReflectiveFields();
    }

    private void initializeReflectiveFields() {
        try {
            extCoreContext = (InternalExtendedCompletionContext) fExtendedContext.get(coreContext);
            if (extCoreContext == null) return;

            assistNode = (ASTNode) fAssistNode.get(extCoreContext);
            assistNodeParent = (ASTNode) fAssistNodeParent.get(extCoreContext);
            assistScope = (MethodScope) fAssistScope.get(extCoreContext);
            compilationUnitDeclaration = (CompilationUnitDeclaration) fCompilationUnitDeclaration.get(extCoreContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestExtendedContext() {
        final ICompilationUnit cu = getCompilationUnit();

        collector = new ProposalCollectingCompletionRequestor(javaContext);
        try {
            cu.codeComplete(getInvocationOffset(), collector, new TimeoutProgressMonitor());
        } catch (final JavaModelException e) {
            RecommendersPlugin.log(e);
        }
        coreContext = collector.getCoreContext();
    }

    public Optional<InternalCompletionContext> getCoreContext() {
        return fromNullable(coreContext);
    }

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
        if (coreContext == null) return absent();
        try {
            if (coreContext.isExtended()) {
                return fromNullable(coreContext.getEnclosingElement());
            }
        } catch (IllegalArgumentException e) {
            // thrown by JDT if it fails to parse the signature.
            // we silently ignore that and return nothing instead.
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

    @Override public Optional<CompilationUnitDeclaration> getCompliationUnitDeclaration() {
        return fromNullable(compilationUnitDeclaration);
    }

    @Override
    public Optional<Scope> getAssistScope() {
        return fromNullable(assistScope);
    }

    @Override
    public CompilationUnit getAST() {
        return astProvider.get(getCompilationUnit());
    }

    @Override
    public Map<IJavaCompletionProposal, CompletionProposal> getProposals() {
        return collector.getProposals();
    }

    @Override
    public Optional<String> getExpectedTypeSignature() {
        if (coreContext == null) return absent();
        // keys contain '/' instead of dots and may end with ';'
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
    public Set<ITypeName> getExpectedTypeNames() {
        ASTNode completion = getCompletionNode().orNull();
        char[][] keys =
                isArgumentCompletion(completion) && getPrefix().isEmpty() ? simulateCompletionWithFakePrefix()
                        : coreContext.getExpectedTypesKeys();
        return createTypeNamesFromKeys(keys);
    }

    private boolean isArgumentCompletion(ASTNode completion) {
        return completion instanceof MessageSend || completion instanceof CompletionOnQualifiedAllocationExpression;
    }

    private char[][] simulateCompletionWithFakePrefix() {
        final MutableObject<char[][]> res = new MutableObject<char[][]>(null);
        ICompilationUnit cu = getCompilationUnit();
        ICompilationUnit wc = null;
        int offset = getInvocationOffset();
        String fakePrefix = "___x";
        try {
            wc = cu.getWorkingCopy(new NullProgressMonitor());
            IBuffer buffer = wc.getBuffer();
            String contents = buffer.getContents();
            String newContents =
                    substring(contents, 0, offset) + fakePrefix + substring(contents, offset, contents.length());
            buffer.setContents(newContents);
            wc.codeComplete(offset + 1, new CompletionRequestor(true) {

                @Override
                public boolean isExtendedContextRequired() {
                    return true;
                }

                @Override
                public void acceptContext(CompletionContext context) {
                    res.setValue(context.getExpectedTypesKeys());
                    super.acceptContext(context);
                }

                @Override
                public void accept(CompletionProposal proposal) {
                }
            });
        } catch (JavaModelException x) {
            RecommendersPlugin.log(x);
        } finally {
            discardWorkingCopy(wc);
        }
        return res.getValue();
    }

    private void discardWorkingCopy(ICompilationUnit wc) {
        try {
            if (wc != null) wc.discardWorkingCopy();
        } catch (JavaModelException x) {
            RecommendersPlugin.log(x);
        }
    }

    private Set<ITypeName> createTypeNamesFromKeys(final char[][] keys) {
        if (keys == null) {
            return Collections.emptySet();
        }
        if (keys.length < 1) {
            return Collections.emptySet();
        }
        Set<ITypeName> res = Sets.newHashSet();
        // keys contain '/' instead of dots and may end with ';'
        for (char[] key : keys) {
            String typeName = StringUtils.removeEnd(new String(key), ";");
            res.add(VmTypeName.get(typeName));
        }
        return res;
    }

    @Override
    public Optional<IType> getExpectedType() {
        final IType res = javaContext.getExpectedType();
        return fromNullable(res);
    }

    @Override
    public String getPrefix() {
        if (coreContext == null) return "";

        final char[] token = coreContext.getToken();
        if (token == null) {
            return "";
        }
        return new String(token);
    }

    @Override
    public String getReceiverName() {

        final ASTNode n = getCompletionNode().orNull();
        if (n == null) return "";

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
                // some anonymous type/method return value that has no name...
                // e.g.:
                // PlatformUI.getWorkbench()|^Space --> receiver is anonymous
                // --> name = null
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
        final ASTNode n = getCompletionNode().orNull();
        if (n == null) return absent();
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
        if (!opt.isPresent()) {
            return absent();
        }
        final TypeBinding b = opt.get();
        if (b instanceof MissingTypeBinding) {
            return absent();
        }
        return JdtUtils.createUnresolvedType(b);
    }

    @Override
    public Optional<IMethodName> getMethodDef() {
        final ASTNode node = getCompletionNode().orNull();
        if (node == null) return absent();

        if (node instanceof CompletionOnMemberAccess) {
            final CompletionOnMemberAccess n = cast(node);
            if (n.receiver instanceof MessageSend) {
                final MessageSend receiver = (MessageSend) n.receiver;
                final MethodBinding binding = receiver.binding;
                return CompilerBindings.toMethodName(binding);
            }
        }
        return absent();
    }

    static {
        try {
            Class<InternalCompletionContext> clazzCtx = InternalCompletionContext.class;
            fExtendedContext = clazzCtx.getDeclaredField("extendedContext");
            fExtendedContext.setAccessible(true);

            Class<InternalExtendedCompletionContext> clazzExt = InternalExtendedCompletionContext.class;
            fAssistScope = clazzExt.getDeclaredField("assistScope");
            fAssistScope.setAccessible(true);
            fAssistNode = clazzExt.getDeclaredField("assistNode");
            fAssistNode.setAccessible(true);
            fAssistNodeParent = clazzExt.getDeclaredField("assistNodeParent");
            fAssistNodeParent.setAccessible(true);
            fCompilationUnitDeclaration = clazzExt.getDeclaredField("compilationUnitDeclaration");
            fCompilationUnitDeclaration.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

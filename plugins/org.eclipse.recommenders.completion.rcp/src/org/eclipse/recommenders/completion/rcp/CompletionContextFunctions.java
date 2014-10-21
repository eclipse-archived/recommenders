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
package org.eclipse.recommenders.completion.rcp;

import static com.google.common.base.Objects.firstNonNull;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.*;
import static org.eclipse.recommenders.internal.completion.rcp.LogMessages.LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findFirstDeclaration;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Logs.log;
import static org.eclipse.recommenders.utils.Reflections.getDeclaredField;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.InternalExtendedCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnLocalName;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedAllocationExpression;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.completion.rcp.processable.ProposalCollectingCompletionRequestor;
import org.eclipse.recommenders.rcp.utils.ASTNodeUtils;
import org.eclipse.recommenders.rcp.utils.AstBindings;
import org.eclipse.recommenders.rcp.utils.JdtUtils;
import org.eclipse.recommenders.rcp.utils.TimeDelimitedProgressMonitor;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings({ "restriction", "rawtypes" })
public final class CompletionContextFunctions {

    private CompletionContextFunctions() {
        throw new IllegalStateException("Not meant to be instantiated"); //$NON-NLS-1$
    }

    public static Map<CompletionContextKey, ICompletionContextFunction> defaultFunctions() {
        Map<CompletionContextKey, ICompletionContextFunction> res = new HashMap<CompletionContextKey, ICompletionContextFunction>();
        res.put(COMPLETION_PREFIX, new CompletionPrefixContextFunction());
        res.put(IS_COMPLETION_ON_TYPE, new CompletionOnTypeContextFunction());
        res.put(ENCLOSING_ELEMENT, new EnclosingElementContextFunction());
        res.put(ENCLOSING_TYPE, new EnclosingTypeContextFunction());
        res.put(ENCLOSING_METHOD, new EnclosingMethodContextFunction());
        res.put(ENCLOSING_AST_METHOD, new EnclosingAstMethodContextFunction());
        res.put(ENCLOSING_METHOD_FIRST_DECLARATION, new EnclosingMethodFirstDeclarationContextFunction());
        res.put(EXPECTED_TYPE, new ExpectedTypeContextFunction());
        res.put(EXPECTED_TYPENAMES, new ExpectedTypeNamesContextFunction());
        res.put(INTERNAL_COMPLETIONCONTEXT, new InternalCompletionContextFunction());
        res.put(JAVA_PROPOSALS, new InternalCompletionContextFunction());
        res.put(LOOKUP_ENVIRONMENT, new LookupEnvironmentContextFunction());
        res.put(RECEIVER_TYPEBINDING, new ReceiverTypeBindingContextFunction());
        res.put(RECEIVER_NAME, new ReceiverNameContextFunction());
        res.put(VISIBLE_METHODS, new VisibleMethodsContextFunction());
        res.put(VISIBLE_FIELDS, new VisibleFieldsContextFunction());
        res.put(VISIBLE_LOCALS, new VisibleLocalsContextFunction());
        res.put(SESSION_ID, new SessionIdFunction());
        res.put(IMPORTED_PACKAGES, new ImportedPackagesFunction());
        return res;
    }

    private static final Logger LOG = LoggerFactory.getLogger(CompletionContextFunctions.class);

    private static final char[] EMPTY = new char[0];

    public static class EnclosingElementContextFunction implements ICompletionContextFunction<IJavaElement> {

        @Override
        public IJavaElement compute(IRecommendersCompletionContext context, CompletionContextKey<IJavaElement> key) {
            IJavaElement res = null;
            try {
                InternalCompletionContext core = context.get(INTERNAL_COMPLETIONCONTEXT, null);
                if (core != null && core.isExtended()) {
                    res = core.getEnclosingElement();
                }
            } catch (Exception e) {
                // IAE thrown by JDT if it fails to parse the signature.
                // we silently ignore that and return nothing instead.
            }
            context.set(key, res);
            return res;
        }
    }

    public static class CompletionOnTypeContextFunction implements ICompletionContextFunction<Boolean> {

        @Override
        public Boolean compute(IRecommendersCompletionContext context, CompletionContextKey<Boolean> key) {
            ASTNode node = context.getCompletionNode().orNull();
            boolean res = false;
            if (node instanceof CompletionOnQualifiedNameReference) {
                Binding binding = ((CompletionOnQualifiedNameReference) node).binding;
                res = binding != null && Binding.TYPE == binding.kind();
            }
            context.set(key, res);
            return res;
        }
    }

    public static class EnclosingMethodContextFunction implements ICompletionContextFunction<IMethod> {

        @Override
        public IMethod compute(IRecommendersCompletionContext context, CompletionContextKey<IMethod> key) {
            IJavaElement enclosing = context.get(ENCLOSING_ELEMENT, null);
            IMethod res = (IMethod) (enclosing instanceof IMethod ? enclosing : null);
            context.set(key, res);
            return res;
        }
    }

    public static class EnclosingMethodFirstDeclarationContextFunction implements ICompletionContextFunction<IMethod> {

        @Override
        public IMethod compute(IRecommendersCompletionContext context, CompletionContextKey<IMethod> key) {
            IMethod root = null;
            IMethod enclosing = context.get(ENCLOSING_METHOD, null);
            if (enclosing != null) {
                root = findFirstDeclaration(enclosing);
            }
            context.set(key, root);
            return root;
        }
    }

    public static class EnclosingTypeContextFunction implements ICompletionContextFunction<IType> {

        @Override
        public IType compute(IRecommendersCompletionContext context, CompletionContextKey<IType> key) {
            IJavaElement enclosing = context.get(ENCLOSING_ELEMENT, null);
            IType res = null;
            if (enclosing instanceof IType) {
                res = (IType) enclosing;
            } else if (enclosing instanceof IField) {
                res = ((IField) enclosing).getDeclaringType();
            } else {
                // res = null
            }
            context.set(key, res);
            return res;
        }
    }

    public static class ExpectedTypeContextFunction implements ICompletionContextFunction<IType> {

        @Override
        public IType compute(IRecommendersCompletionContext context, CompletionContextKey<IType> key) {
            JavaContentAssistInvocationContext ctx = context.get(JAVA_CONTENTASSIST_CONTEXT, null);
            IType res = ctx == null ? null : ctx.getExpectedType();
            context.set(key, res);
            return res;
        }
    }

    public static class ExpectedTypeNamesContextFunction implements ICompletionContextFunction<Set<ITypeName>> {

        @Override
        public Set<ITypeName> compute(IRecommendersCompletionContext context, CompletionContextKey<Set<ITypeName>> key) {
            ASTNode completion = context.getCompletionNode().orNull();
            InternalCompletionContext core = context.get(INTERNAL_COMPLETIONCONTEXT, null);

            char[][] keys;
            if (isArgumentCompletion(completion) && context.getPrefix().isEmpty()) {
                ICompilationUnit cu = context.getCompilationUnit();
                int offset = context.getInvocationOffset();
                keys = simulateCompletionWithFakePrefix(cu, offset);
            } else {
                keys = core.getExpectedTypesSignatures();
            }
            Set<ITypeName> res = RecommendersCompletionContext.createTypeNamesFromSignatures(keys);
            context.set(key, res);
            return res;
        }

        private boolean isArgumentCompletion(ASTNode completion) {
            return completion instanceof MessageSend || completion instanceof CompletionOnQualifiedAllocationExpression;
        }

        private char[][] simulateCompletionWithFakePrefix(ICompilationUnit cu, int offset) {
            final MutableObject<char[][]> res = new MutableObject<char[][]>(null);
            ICompilationUnit wc = null;
            String fakePrefix = "___x"; //$NON-NLS-1$
            try {
                wc = cu.getWorkingCopy(new NullProgressMonitor());
                IBuffer buffer = wc.getBuffer();
                String contents = buffer.getContents();
                String newContents = substring(contents, 0, offset) + fakePrefix
                        + substring(contents, offset, contents.length());
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
                log(LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION, x);
            } finally {
                discardWorkingCopy(wc);
            }
            return res.getValue();
        }

        private void discardWorkingCopy(ICompilationUnit wc) {
            try {
                if (wc != null) {
                    wc.discardWorkingCopy();
                }
            } catch (JavaModelException x) {
                log(LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION, x);
            }
        }

    }

    public static class ReceiverTypeBindingContextFunction implements ICompletionContextFunction<TypeBinding> {

        @Override
        public TypeBinding compute(IRecommendersCompletionContext context, CompletionContextKey<TypeBinding> key) {
            final ASTNode n = context.getCompletionNode().orNull();
            if (n == null) {
                return null;
            }
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
                case Binding.GENERIC_TYPE:
                    // e.g. Class.|<ctrl-space>
                    receiver = (TypeBinding) c.binding;
                    break;
                default:
                }
            } else if (n instanceof CompletionOnMemberAccess) {
                final CompletionOnMemberAccess c = cast(n);
                receiver = c.actualReceiverType;
            }
            context.set(key, receiver);
            return receiver;
        }
    }

    public static class ReceiverNameContextFunction implements ICompletionContextFunction<String> {

        @Override
        public String compute(IRecommendersCompletionContext context, CompletionContextKey<String> key) {
            final ASTNode n = context.getCompletionNode().orNull();
            if (n == null) {
                return ""; //$NON-NLS-1$
            }

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
                // final CompletionOnSingleNameReference c = cast(n);
                // TODO is that correct?
                // name = c.token;
                name = new char[0];
            } else if (n instanceof CompletionOnMemberAccess) {
                final CompletionOnMemberAccess c = cast(n);
                if (c.receiver instanceof ThisReference) {
                    name = "this".toCharArray(); //$NON-NLS-1$
                } else if (c.receiver instanceof MessageSend) {
                    // some anonymous type/method return value that has no
                    // name...
                    // e.g.:
                    // PlatformUI.getWorkbench()|^Space --> receiver is
                    // anonymous
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
            String res = new String(firstNonNull(name, EMPTY));
            res = res.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
            context.set(key, res);
            return res;
        }
    }

    public static class InternalCompletionContextFunction implements ICompletionContextFunction<Object> {

        @Override
        public Object compute(IRecommendersCompletionContext context, CompletionContextKey<Object> key) {
            JavaContentAssistInvocationContext coreContext = context.getJavaContext();

            int offset = context.getInvocationOffset();
            if (offset == -1) {
                return null;
            }
            ICompilationUnit cu = context.getCompilationUnit();
            ProposalCollectingCompletionRequestor collector = new ProposalCollectingCompletionRequestor(coreContext);
            try {
                cu.codeComplete(offset, collector, new TimeDelimitedProgressMonitor(5000));
            } catch (final Exception e) {
                log(LOG_ERROR_EXCEPTION_DURING_CODE_COMPLETION, e);
            }
            InternalCompletionContext internal = collector.getCoreContext();
            context.set(INTERNAL_COMPLETIONCONTEXT, internal);
            Map<IJavaCompletionProposal, CompletionProposal> proposals = collector.getProposals();
            context.set(JAVA_PROPOSALS, proposals);

            if (INTERNAL_COMPLETIONCONTEXT.equals(key)) {
                return internal;
            } else {
                return proposals;
            }
        }
    }

    public static class CompletionPrefixContextFunction implements ICompletionContextFunction<String> {

        @Override
        public String compute(IRecommendersCompletionContext context, CompletionContextKey<String> key) {
            InternalCompletionContext ctx = context.get(INTERNAL_COMPLETIONCONTEXT, null);
            char[] prefix = EMPTY;
            if (ctx != null) {
                prefix = firstNonNull(ctx.getToken(), EMPTY);
            }
            String res = new String(prefix);
            context.set(key, res);
            return res;
        }
    }

    public static class VisibleMethodsContextFunction implements ICompletionContextFunction<List<IMethod>> {

        @Override
        public List<IMethod> compute(IRecommendersCompletionContext context, CompletionContextKey<List<IMethod>> key) {
            InternalCompletionContext ctx = context.get(INTERNAL_COMPLETIONCONTEXT, null);
            if (ctx == null || !ctx.isExtended()) {
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
            context.set(key, res);
            return res;
        }
    }

    public static class VisibleFieldsContextFunction implements ICompletionContextFunction<List<IField>> {

        @Override
        public List<IField> compute(IRecommendersCompletionContext context, CompletionContextKey<List<IField>> key) {
            InternalCompletionContext ctx = context.get(INTERNAL_COMPLETIONCONTEXT, null);
            if (ctx == null || !ctx.isExtended()) {
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
            context.set(key, res);
            return res;
        }
    }

    public static class VisibleLocalsContextFunction implements ICompletionContextFunction<List<ILocalVariable>> {

        @Override
        public List<ILocalVariable> compute(IRecommendersCompletionContext context,
                CompletionContextKey<List<ILocalVariable>> key) {
            InternalCompletionContext ctx = context.get(INTERNAL_COMPLETIONCONTEXT, null);
            if (ctx == null || !ctx.isExtended()) {
                return Collections.emptyList();
            }
            final ObjectVector v = ctx.getVisibleLocalVariables();
            final List<ILocalVariable> res = Lists.newArrayListWithCapacity(v.size);
            for (int i = v.size(); i-- > 0;) {
                final LocalVariableBinding b = cast(v.elementAt(i));
                final JavaElement parent = (JavaElement) context.getEnclosingElement().get();
                final ILocalVariable f = JdtUtils.createUnresolvedLocaVariable(b, parent);
                res.add(f);
            }
            context.set(key, res);
            return res;
        }
    }

    public static class EnclosingAstMethodContextFunction implements ICompletionContextFunction<MethodDeclaration> {

        @Override
        public MethodDeclaration compute(IRecommendersCompletionContext context,
                CompletionContextKey<MethodDeclaration> key) {
            MethodDeclaration astMethod = null;
            IMethod jdtMethod = context.getEnclosingMethod().orNull();
            if (jdtMethod != null) {
                CompilationUnit ast = context.getAST();
                astMethod = ASTNodeUtils.find(ast, jdtMethod).orNull();
            }
            context.set(key, astMethod);
            return astMethod;
        }
    }

    public static class LookupEnvironmentContextFunction implements ICompletionContextFunction<LookupEnvironment> {

        private static final Field EXTENDED_CONTEXT = getDeclaredField(InternalCompletionContext.class,
                "extendedContext").orNull(); //$NON-NLS-1$
        private static final Field LOOKUP_ENVIRONMENT = getDeclaredField(InternalExtendedCompletionContext.class,
                "lookupEnvironment").orNull(); //$NON-NLS-1$

        @Override
        public LookupEnvironment compute(IRecommendersCompletionContext context,
                CompletionContextKey<LookupEnvironment> key) {
            if (EXTENDED_CONTEXT == null || LOOKUP_ENVIRONMENT == null) {
                return null;
            }

            try {
                InternalCompletionContext ctx = context.get(CompletionContextKey.INTERNAL_COMPLETIONCONTEXT, null);
                InternalExtendedCompletionContext extCtx = cast(EXTENDED_CONTEXT.get(ctx));
                if (extCtx == null) {
                    return null;
                }
                LookupEnvironment env = cast(LOOKUP_ENVIRONMENT.get(extCtx));
                context.set(key, env);
                return env;
            } catch (Exception e) {
                LOG.error("Cannot access LookupEnvironment by reflection.", e); //$NON-NLS-1$
                return null;
            }
        }
    }

    public static class SessionIdFunction implements ICompletionContextFunction<UUID> {

        @Override
        public UUID compute(IRecommendersCompletionContext context, CompletionContextKey<UUID> key) {
            UUID res = UUID.randomUUID();
            context.set(key, res);
            return res;
        }
    }

    public static class ImportedPackagesFunction implements ICompletionContextFunction<Set<IPackageName>> {

        @Override
        @SuppressWarnings("unchecked")
        public Set<IPackageName> compute(IRecommendersCompletionContext context,
                CompletionContextKey<Set<IPackageName>> key) {
            CompilationUnit ast = context.getAST();
            List<ImportDeclaration> imports = ast.imports();
            Set<IPackageName> res = Sets.newHashSet();
            for (ImportDeclaration decl : imports) {
                IBinding b = decl.resolveBinding();
                if (b == null) {
                    continue;
                }
                switch (b.getKind()) {
                case IBinding.TYPE: {
                    ITypeName type = AstBindings.toTypeName((ITypeBinding) b).orNull();
                    if (type != null) {
                        res.add(type.getPackage());
                    }
                    break;
                }
                case IBinding.PACKAGE: {
                    IPackageName pkg = AstBindings.toPackageName((IPackageBinding) b).orNull();
                    if (pkg != null) {
                        res.add(pkg);
                    }
                    break;
                }
                case IBinding.METHOD: {
                    ITypeName type = AstBindings.toTypeName(((IMethodBinding) b).getDeclaringClass()).orNull();
                    if (type != null) {
                        res.add(type.getPackage());
                    }
                    break;
                }
                case IBinding.VARIABLE: {
                    ITypeName type = AstBindings.toTypeName(((IVariableBinding) b).getDeclaringClass()).orNull();
                    if (type != null) {
                        res.add(type.getPackage());
                    }
                    break;
                }
                }
            }
            context.set(key, res);
            return res;
        }
    }
}

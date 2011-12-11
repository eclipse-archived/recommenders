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
package org.eclipse.recommenders.rcp.utils;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static org.eclipse.jdt.internal.corext.util.JdtFlags.isPublic;
import static org.eclipse.jdt.internal.corext.util.JdtFlags.isStatic;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.rcp.utils.ast.MethodDeclarationFinder;
import org.eclipse.recommenders.rcp.utils.internal.MyWorkingCopyOwner;
import org.eclipse.recommenders.rcp.utils.internal.RecommendersUtilsPlugin;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.base.Optional;

@SuppressWarnings({ "restriction", "unchecked", "deprecation" })
public class JdtUtils {
    private static final Util.BindingsToNodesMap EMPTY_NODE_MAP = new Util.BindingsToNodesMap() {
        @Override
        public org.eclipse.jdt.internal.compiler.ast.ASTNode get(final Binding binding) {
            return null;
        }
    };
    private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

    private static IJavaElement[] codeResolve(final ITypeRoot root, final ITextSelection selection) {
        reconcileIfCompilationUnit(root);
        try {
            return root.codeSelect(selection.getOffset(), selection.getLength());
        } catch (final JavaModelException e) {
            log(e);
        }
        return EMPTY_RESULT;
    }

    /**
     * Finds and returns the Java elements for the given editor selection.
     * 
     * @param editor
     *            the Java editor
     * @param selection
     *            the text selection
     * @return the Java elements for the given editor selection
     */
    public static IJavaElement[] codeResolve(final JavaEditor editor, final ITextSelection selection) {
        ensureIsNotNull(editor);
        ensureIsNotNull(selection);
        final Optional<ITypeRoot> input = getInput(editor);
        if (input.isPresent()) {
            return codeResolve(input.get(), selection);
        }
        return EMPTY_RESULT;
    }

    public static boolean containsErrors(final IType type) {
        final ITypeRoot typeRoot = type.getTypeRoot();
        final CompilationUnit ast = SharedASTProvider.getAST(typeRoot, SharedASTProvider.WAIT_YES, null);
        final IProblem[] problems = ast.getProblems();
        for (final IProblem problem : problems) {
            if (problem.isError()) {
                return true;
            }
        }
        return false;
    }

    public static CompilationUnit createCompilationUnitFromString(final ITypeName typeName, final String source,
            final IJavaProject javaProject) {
        final ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setResolveBindings(true);
        // XXX does this hurt?
        final String srcClassName = Names.vm2srcTypeName(typeName.getIdentifier());
        parser.setUnitName(srcClassName + ".java");
        parser.setProject(javaProject);
        parser.setWorkingCopyOwner(new MyWorkingCopyOwner());
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        final ASTNode ast = parser.createAST(null);
        return (CompilationUnit) ast;
    }

    private static String createFieldKey(final IField field) {
        try {
            return field.getElementName() + field.getTypeSignature();
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    public static JavaContext createJavaContext(final JavaContentAssistInvocationContext contentAssistContext) {
        final ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
        final TemplateContextType templateContextType = templateContextRegistry.getContextType(JavaContextType.ID_ALL);
        final JavaContext javaTemplateContext = new JavaContext(templateContextType,
                contentAssistContext.getDocument(), contentAssistContext.getInvocationOffset(), contentAssistContext
                        .getCoreContext().getToken().length, contentAssistContext.getCompilationUnit());
        javaTemplateContext.setForceEvaluation(true);
        return javaTemplateContext;
    }

    private static String createMethodKey(final IMethod method) {
        try {
            final String signature = method.getSignature();
            final String signatureWithoutReturnType = substringBeforeLast(signature, ")");
            final String methodName = method.getElementName();
            return methodName + signatureWithoutReturnType;
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    public static IRegion createRegion(final ASTNode node) {
        ensureIsNotNull(node);
        return new Region(node.getStartPosition(), node.getLength());
    }

    public static IField createUnresolvedField(final FieldBinding compilerBinding) {
        ensureIsNotNull(compilerBinding);
        return (IField) Util.getUnresolvedJavaElement(compilerBinding, null, EMPTY_NODE_MAP);
    }

    public static ILocalVariable createUnresolvedLocaVariable(final VariableBinding compilerBinding,
            final JavaElement parent) {
        ensureIsNotNull(compilerBinding);
        ensureIsNotNull(parent);

        final String name = new String(compilerBinding.name);
        final String type = new String(compilerBinding.type.signature());
        return new LocalVariable(parent, name, 0, 0, 0, 0, type, null, compilerBinding.modifiers,
                compilerBinding.isParameter());
    }

    public static IMethod createUnresolvedMethod(final MethodBinding compilerBinding) {
        ensureIsNotNull(compilerBinding);
        return (IMethod) Util.getUnresolvedJavaElement(compilerBinding, null, EMPTY_NODE_MAP);
    }

    public static IType createUnresolvedType(final TypeBinding compilerBinding) {
        return (IType) Util.getUnresolvedJavaElement(compilerBinding, null, EMPTY_NODE_MAP);
    }

    /**
     * Returns a list of all public instance methods and fields declared in the given type or any of its super-types
     */
    public static Collection<IMember> findAllPublicInstanceFieldsAndNonVoidNonPrimitiveMethods(final IType type) {
        final LinkedHashMap<String, IMember> tmp = new LinkedHashMap<String, IMember>();

        try {
            final IType[] returnTypeAndSupertypes = findAllSupertypesIncludeingArgument(type);
            for (final IType cur : returnTypeAndSupertypes) {
                for (final IMethod m : cur.getMethods()) {
                    if (isVoid(m) || !isPublic(m) || m.isConstructor() || isStatic(m) || hasPrimitiveReturnType(m)) {
                        continue;
                    }
                    final String key = createMethodKey(m);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, m);
                    }
                }
                for (final IField field : cur.getFields()) {
                    if (!isPublic(field) || isStatic(field)) {
                        continue;
                    }
                    final String key = createFieldKey(field);
                    if (!tmp.containsKey(key)) {
                        tmp.put(key, field);
                    }
                }
            }
        } catch (final JavaModelException e) {
            log(e);
        }
        return tmp.values();
    }

    /**
     * Returns a list of all public static fields and methods declared in the given class (but not its super-classes)
     * TODO: superclasses not, should we add this?
     */
    public static List<IMember> findAllPublicStaticFieldsAndNonVoidNonPrimitiveMethods(final IType type) {
        final List<IMember> res = new LinkedList<IMember>();
        try {
            for (final IMethod m : type.getMethods()) {
                if (isStatic(m) && isPublic(m) && !isVoid(m) && !hasPrimitiveReturnType(m)) {
                    res.add(m);
                }
            }
            for (final IField f : type.getFields()) {
                if (isStatic(f) && isPublic(f)) {
                    res.add(f);
                }
            }
        } catch (final JavaModelException e) {
            log(e);
        }
        return res;
    }

    private static IType[] findAllSupertypesIncludeingArgument(final IType returnType) {
        try {
            ITypeHierarchy typeHierarchy;
            typeHierarchy = SuperTypeHierarchyCache.getTypeHierarchy(returnType);
            final IType[] allSupertypes = typeHierarchy.getAllSupertypes(returnType);
            return ArrayUtils.add(allSupertypes, 0, returnType);
        } catch (final JavaModelException e) {
            log(e);
            return new IType[0];
        }
    }

    public static ASTNode findClosestMethodOrTypeDeclarationAroundOffset(final CompilationUnit cuNode,
            final ITextSelection selection) {
        ensureIsNotNull(cuNode, "cuNode");
        ensureIsNotNull(selection, "selection");
        ASTNode node = NodeFinder.perform(cuNode, selection.getOffset(), selection.getLength());
        while (node != null) {
            switch (node.getNodeType()) {
            case ASTNode.METHOD_DECLARATION:
            case ASTNode.TYPE_DECLARATION:
                return node;
            }
            node = node.getParent();
        }
        return cuNode;
    }

    public static IMethod findFirstDeclaration(final IMethod method) {
        IMethod res = method;
        while (true) {
            final Optional<IMethod> oFind = findOverriddenMethod(res);
            if (!oFind.isPresent()) {
                break;
            } else {
                res = oFind.get();
            }
        }
        return res;
    }

    public static Optional<MethodDeclaration> findMethod(final CompilationUnit cuNode, final IMethodName searchedMethod) {
        return MethodDeclarationFinder.find(cuNode, searchedMethod);
    }

    public static Optional<IMethod> findOverriddenMethod(final IMethod jdtMethod) {
        try {
            final IType jdtDeclaringType = jdtMethod.getDeclaringType();
            final MethodOverrideTester methodOverrideTester = SuperTypeHierarchyCache
                    .getMethodOverrideTester(jdtDeclaringType);
            final IMethod overriddenMethod = methodOverrideTester.findOverriddenMethod(jdtMethod, false);
            return fromNullable(overriddenMethod);
        } catch (final JavaModelException e) {
            RecommendersUtilsPlugin.log(e);
            return absent();
        }
    }

    public static Optional<ITypeName> findSuperclassName(final IType type) {
        try {
            final String superclassName = type.getSuperclassTypeSignature();
            if (superclassName == null) {
                return absent();
            }
            final String resolvedSuperclassName = resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(
                    superclassName, type);
            final String vmSuperclassName = toVMTypeDescriptor(resolvedSuperclassName);
            final ITypeName vmTypeName = VmTypeName.get(vmSuperclassName);
            return of(vmTypeName);
        } catch (final Exception e) {
            throw throwUnhandledException(e);
        }
    }

    public static Optional<IType> findSuperclass(final IType type) {
        ensureIsNotNull(type);
        try {
            final String superclassTypeSignature = type.getSuperclassTypeSignature();
            return findTypeFromSignature(superclassTypeSignature, type);
        } catch (final JavaModelException e) {
            log(e);
            return absent();
        }
    }

    public static Optional<IType> findTypeFromSignature(final String typeSignature, final IJavaElement parent) {
        try {
            final String resolvedTypeSignature = resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(
                    typeSignature, parent);
            final IType res = parent.getJavaProject().findType(resolvedTypeSignature);
            return Optional.fromNullable(res);
        } catch (final JavaModelException e) {
            log(e);
            return Optional.absent();
        }
    }

    public static Optional<IType> findTypeOfField(final IField field) {
        try {
            return findTypeFromSignature(field.getTypeSignature(), field);
        } catch (final JavaModelException e) {
            log(e);
            return Optional.absent();
        }
    }

    public static Optional<ITypeRoot> findTypeRoot(final IEditorPart editor) {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        return fromNullable(root);
    }

    public static Optional<IWorkbenchPage> getActiveWorkbenchPage() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return absent();
        }
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return absent();
        }
        final IWorkbenchPage page = window.getActivePage();
        return of(page);
    }

    private static Optional<IJavaElement> getElementAtOffset(final ITypeRoot input, final ITextSelection selection) {
        IJavaElement res = null;
        try {
            final ITypeRoot root = input;
            reconcileIfCompilationUnit(root);
            res = root.getElementAt(selection.getOffset());
        } catch (final JavaModelException e) {
            log(e);
        }
        if (res == null) {
            res = input;
        }
        return of(res);
    }

    /**
     * Finds and returns the Java element that contains the text selection in the given editor.
     * 
     * @param editor
     *            the Java editor
     * @param selection
     *            the text selection
     * @return the Java elements for the given editor selection
     */
    public static Optional<IJavaElement> getElementAtOffset(final JavaEditor editor, final ITextSelection selection) {
        final Optional<ITypeRoot> input = getInput(editor);
        if (input.isPresent()) {
            return getElementAtOffset(input.get(), selection);
        }
        return absent();
    }

    private static Optional<ITypeRoot> getInput(final JavaEditor editor) {
        ITypeRoot res;

        final IEditorInput input = editor.getEditorInput();
        if (input instanceof IClassFileEditorInput) {
            final IClassFileEditorInput classfileInput = (IClassFileEditorInput) input;
            res = classfileInput.getClassFile();
        } else {
            final IWorkingCopyManager manager = JavaPlugin.getDefault().getWorkingCopyManager();
            res = manager.getWorkingCopy(input);
        }
        return fromNullable(res);
    }

    public static IPackageFragmentRoot getPackageFragmentRoot(final IPackageFragment packageFragment) {
        return (IPackageFragmentRoot) packageFragment.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    }

    public static ITextSelection getTextSelection(final ITextEditor editor) {
        if (editor == null) {
            return new TextSelection(0, 0);
        } else {
            return (TextSelection) editor.getSelectionProvider().getSelection();
        }
    }

    public static boolean hasPrimitiveReturnType(final IMethod method) {
        try {
            return !method.getReturnType().endsWith(";");
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    public static boolean isAssignable(final IType lhsType, final IType rhsType) {
        ensureIsNotNull(lhsType);
        ensureIsNotNull(rhsType);
        final IType[] supertypes = findAllSupertypesIncludeingArgument(rhsType);
        for (final IType supertype : supertypes) {
            if (supertype.equals(lhsType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isJavaClass(final IType type) {
        try {
            return type.isClass();
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    public static boolean isVoid(final IMethod method) {
        try {
            return Signature.SIG_VOID.equals(method.getReturnType());
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    private static void log(final CoreException e) {
        RecommendersUtilsPlugin.log(e);
    }

    public static void log(final Exception e) {
        RecommendersUtilsPlugin.logError(e, "Exception occurred.");
    }

    public static Optional<JavaEditor> openJavaEditor(final IEditorInput input) {
        final Optional<IWorkbenchPage> oPage = getActiveWorkbenchPage();
        if (!oPage.isPresent()) {
            return absent();
        }
        final IWorkbenchPage page = oPage.get();
        final IEditorPart editor = page.findEditor(input);
        if (editor instanceof JavaEditor) {
            page.bringToTop(editor);
            return of((JavaEditor) editor);
        } else {
            try {
                return fromNullable((JavaEditor) page.openEditor(input, "org.eclipse.jdt.ui.CompilationUnitEditor"));
            } catch (final PartInitException e) {
                log(e);
                return absent();
            }
        }
    }

    private static void reconcileIfCompilationUnit(final IJavaElement element) {
        if (element instanceof ICompilationUnit) {
            final ICompilationUnit cunit = (ICompilationUnit) element;
            if (cunit.isWorkingCopy()) {
                try {
                    JavaModelUtil.reconcile(cunit);
                } catch (final JavaModelException e) {
                    log(e);
                }
            }
        }
    }

    public static Optional<ASTNode> resolveDeclarationNode(final JavaEditor editor) {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        if (root == null) {
            return Optional.absent();
        }
        final CompilationUnit cuNode = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        final ITextSelection selection = getTextSelection(editor);
        final ASTNode activeDeclarationNode = findClosestMethodOrTypeDeclarationAroundOffset(cuNode, selection);
        return fromNullable(activeDeclarationNode);
    }

    public static <T extends IJavaElement> T resolveJavaElementProxy(final IJavaElement element) {
        return (T) element.getPrimaryElement();
    }

    public static String resolveUnqualifiedTypeNamesAndStripOffGenericsAndArrayDimension(String typeSignature,
            final IJavaElement parent) {
        try {
            typeSignature = typeSignature.replace('/', '.');
            final IType type = (IType) parent.getAncestor(IJavaElement.TYPE);
            typeSignature = JavaModelUtil.getResolvedTypeName(typeSignature, type);
            // NOT needed. Done by getResolvedTypeName typeSignature = StringUtils.substringBefore(typeSignature, "[");
            typeSignature = substringBeforeLast(typeSignature, "<");
            return typeSignature;
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    public static void revealInEditor(final IEditorPart editor, final MethodDeclaration method) {
        EditorUtility.revealInEditor(editor, createRegion(method.getName()));
    }

    public static void revealInEditor(final IEditorPart editor, final TypeDeclaration type) {
        EditorUtility.revealInEditor(editor, createRegion(type.getName()));
    }

    private static String substringBeforeLast(String typeSignature, final String separator) {
        final int lastIndexOf = typeSignature.lastIndexOf(separator);
        if (lastIndexOf > -1) {
            typeSignature = typeSignature.substring(0, lastIndexOf);
        }
        return typeSignature;
    }

    private static String toVMTypeDescriptor(final String fqjdtName) {
        return fqjdtName == null ? "Ljava/lang/Object" : "L" + fqjdtName.replace('.', '/');
    }

}

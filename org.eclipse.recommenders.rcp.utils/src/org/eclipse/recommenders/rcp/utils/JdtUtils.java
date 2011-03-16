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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.rcp.utils.ast.MethodDeclarationFinder;
import org.eclipse.recommenders.rcp.utils.internal.MyWorkingCopyOwner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings({ "restriction", "unchecked", "deprecation" })
public class JdtUtils {
    private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

    public static StructuredSelection asStructuredSelection(final ISelection selection) {
        return (StructuredSelection) (isStructured(selection) ? selection : StructuredSelection.EMPTY);
    }

    public static <T extends IJavaElement> T resolveJavaElementProxy(final IJavaElement element) {
        return (T) element.getPrimaryElement();
    }

    private static IJavaElement[] codeResolve(final IJavaElement input, final ITextSelection selection)
            throws JavaModelException {
        if (input instanceof ICodeAssist) {
            if (input instanceof ICompilationUnit) {
                final ICompilationUnit cunit = (ICompilationUnit) input;
                if (cunit.isWorkingCopy()) {
                    JavaModelUtil.reconcile(cunit);
                }
            }
            final IJavaElement[] elements = ((ICodeAssist) input).codeSelect(selection.getOffset(),
                    selection.getLength());
            if (elements != null && elements.length > 0) {
                return elements;
            }
        }
        return EMPTY_RESULT;
    }

    public static boolean isJavaClass(final IType type) {
        try {
            return type.isClass();
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    /**
     * Finds and returns the Java elements for the given editor selection.
     * 
     * @param editor
     *            the Java editor
     * @param selection
     *            the text selection
     * @return the Java elements for the given editor selection
     * @throws JavaModelException
     */
    public static IJavaElement[] codeResolve(final JavaEditor editor, final ITextSelection selection)
            throws JavaModelException {
        return codeResolve(getInput(editor), selection);
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

    public static ITypeName getSuperclass(final IType jdtType) {
        try {
            final String superclassName = jdtType.getSuperclassTypeSignature();
            if (superclassName == null) {
                return null;
            }
            final String resolvedSuperclassName = JavaModelUtil.getResolvedTypeName(superclassName, jdtType);
            final String vmSuperclassName = toVMTypeDescriptor(resolvedSuperclassName);
            return VmTypeName.get(vmSuperclassName);
        } catch (final Exception e) {
            throw throwUnhandledException(e);
        }
    }

    public static IMethod getoverriddenMethod(final IMethod jdtMethod) throws JavaModelException {
        final IType jdtDeclaringType = jdtMethod.getDeclaringType();
        final MethodOverrideTester methodOverrideTester = SuperTypeHierarchyCache
                .getMethodOverrideTester(jdtDeclaringType);
        final IMethod overriddenMethod = methodOverrideTester.findOverriddenMethod(jdtMethod, false);
        return overriddenMethod;
    }

    private static String toVMTypeDescriptor(final String fqjdtName) {
        return fqjdtName == null ? "Ljava/lang/Object" : "L" + fqjdtName.replace('.', '/');
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

    public static IRegion createRegion(final ASTNode node) {
        return new Region(node.getStartPosition(), node.getLength());
    }

    public static MethodDeclaration findMethod(final CompilationUnit cuNode, final IMethodName searchedMethod) {
        return MethodDeclarationFinder.find(cuNode, searchedMethod);
    }

    public static IWorkbenchPage getActiveWorkbenchPage() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) {
            return null;
        }
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }
        final IWorkbenchPage page = window.getActivePage();
        return page;
    }

    public static ASTNode getClosestMethodOrTypeDeclarationAroundOffset(final CompilationUnit cuNode,
            final ITextSelection selection) throws JavaModelException {
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

    private static IJavaElement getElementAtOffset(final IJavaElement input, final ITextSelection selection)
            throws JavaModelException {
        if (input instanceof ICompilationUnit) {
            final ICompilationUnit cunit = (ICompilationUnit) input;
            if (cunit.isWorkingCopy()) {
                JavaModelUtil.reconcile(cunit);
            }
            final IJavaElement ref = cunit.getElementAt(selection.getOffset());
            if (ref == null) {
                return input;
            } else {
                return ref;
            }
        } else if (input instanceof IClassFile) {
            final IJavaElement ref = ((IClassFile) input).getElementAt(selection.getOffset());
            if (ref == null) {
                return input;
            } else {
                return ref;
            }
        }
        return null;
    }

    /**
     * Finds and returns the Java element that contains the text selection in
     * the given editor.
     * 
     * @param editor
     *            the Java editor
     * @param selection
     *            the text selection
     * @return the Java elements for the given editor selection
     * @throws JavaModelException
     */
    public static IJavaElement getElementAtOffset(final JavaEditor editor, final ITextSelection selection)
            throws JavaModelException {
        return getElementAtOffset(getInput(editor), selection);
    }

    // -------------------- Helper methods --------------------
    private static IJavaElement getInput(final JavaEditor editor) {
        if (editor == null) {
            return null;
        }
        final IEditorInput input = editor.getEditorInput();
        if (input instanceof IClassFileEditorInput) {
            return ((IClassFileEditorInput) input).getClassFile();
        }
        final IWorkingCopyManager manager = JavaPlugin.getDefault().getWorkingCopyManager();
        return manager.getWorkingCopy(input);
    }

    public static ITextSelection getTextSelection(final ITextEditor editor) {
        if (editor == null) {
            return new TextSelection(0, 0);
        } else {
            return (TextSelection) editor.getSelectionProvider().getSelection();
        }
    }

    public static boolean isStructured(final ISelection selection) {
        return selection instanceof IStructuredSelection;
    }

    private static void log(final CoreException e) {
        e.printStackTrace();
    }

    public static void log(final Exception e) {
        e.printStackTrace();
    }

    public static JavaEditor openJavaEditor(final IEditorInput input) {
        final IWorkbenchPage page = getActiveWorkbenchPage();
        if (page == null) {
            return null;
        }
        try {
            final IEditorPart editor = page.findEditor(input);
            if (editor instanceof JavaEditor) {
                page.bringToTop(editor);
                return (JavaEditor) editor;
            }
            return (JavaEditor) page.openEditor(input, "org.eclipse.jdt.ui.CompilationUnitEditor");
        } catch (final PartInitException e) {
            log(e);
            return null;
        }
    }

    public static ASTNode resolveDeclarationNode(final JavaEditor editor) throws JavaModelException {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        if (root == null) {
            return null;
        }
        final CompilationUnit cuNode = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        final ITextSelection selection = getTextSelection(editor);
        final ASTNode activeDeclarationNode = getClosestMethodOrTypeDeclarationAroundOffset(cuNode, selection);
        return activeDeclarationNode;
    }

    public static void revealInEditor(final IEditorPart editor, final MethodDeclaration method) {
        EditorUtility.revealInEditor(editor, createRegion(method.getName()));
    }

    public static void revealInEditor(final IEditorPart editor, final TypeDeclaration type) {
        EditorUtility.revealInEditor(editor, createRegion(type.getName()));
    }

    public static <T> T safeFirstElement(final ISelection s, final Class<T> type) {
        final Object element = asStructuredSelection(s).getFirstElement();
        return (T) (type.isInstance(element) ? element : null);
    }

    public static <T> List<T> toList(final ISelection selection) {
        return asStructuredSelection(selection).toList();
    }

    public static <T> T unsafeFirstElement(final ISelection s) {
        return (T) asStructuredSelection(s).getFirstElement();
    }

}

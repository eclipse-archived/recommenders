/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.List;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.Names;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.rcp.utils.RCPUtils;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

@SuppressWarnings({ "restriction", "unchecked" })
public class CrASTUtil {
    private static final IJavaElement[] EMPTY_RESULT = new IJavaElement[0];

    public static IRegion createRegion(final ASTNode node) {
        return new Region(node.getStartPosition(), node.getLength());
    }

    private static final class MethodDeclarationFinder extends ASTVisitor {
        private final IMethodName searchedMethod;
        private MethodDeclaration match;

        private MethodDeclarationFinder(final boolean visitDocTags, final IMethodName searchedMethod) {
            super(visitDocTags);
            ensureIsNotNull(searchedMethod);
            this.searchedMethod = searchedMethod;
        }

        @Override
        public boolean visit(final MethodDeclaration decl) {
            if (matchesSimpleName(decl)) {
                final List<SingleVariableDeclaration> jdtParams = decl.parameters();
                final ITypeName[] crParams = searchedMethod.getParameterTypes();
                if (haveSameNumberOfParameters(jdtParams, crParams) && haveSameParameterTypes(jdtParams, crParams)) {
                    match = decl;
                }
            }
            return false;
        }

        private boolean haveSameParameterTypes(final List<SingleVariableDeclaration> jdtParams,
                final ITypeName[] crParams) {
            for (int i = crParams.length; --i > 0;) {
                final Type jdtParam = jdtParams.get(i).getType();
                final ITypeName crParam = crParams[i];
                //
                if (jdtParam.isArrayType() || jdtParam.isPrimitiveType()) {
                    continue;
                }
                if (jdtParam.isSimpleType() && !haveSameSimpleName(jdtParam, crParam)) {
                    return false;
                }
            }
            return true;
        }

        private boolean haveSameSimpleName(final Type jdtParam, final ITypeName crParam) {
            final String jdtSimpleName = jdtParam.toString();
            final String crSimpleName = crParam.getClassName();
            return jdtSimpleName.equals(crSimpleName);
        }

        private boolean haveSameNumberOfParameters(final List<SingleVariableDeclaration> jdtParams,
                final ITypeName[] crParams) {
            return crParams.length == jdtParams.size();
        }

        private boolean matchesSimpleName(final MethodDeclaration decl) {
            final String IMethodName = decl.getName().toString();
            if (searchedMethod.isInit()) {
                final ITypeName declaringType = searchedMethod.getDeclaringType();
                final String className = declaringType.getClassName();
                final boolean sameIMethodName = className.equals(IMethodName);
                return sameIMethodName;
            }
            final boolean sameIMethodName = IMethodName.equals(searchedMethod.getName());
            return sameIMethodName;
        }
    }

    public static CompilationUnit createCompilationUnitFromString(final ITypeName ITypeName, final String source,
            final IJavaProject javaProject) {
        final ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(source.toCharArray());
        parser.setResolveBindings(true);
        // XXX does this hurt?
        final String srcClassName = Names.vm2srcTypeName(ITypeName.getIdentifier());
        parser.setUnitName(srcClassName + ".java");
        parser.setProject(javaProject);
        parser.setWorkingCopyOwner(new MyWorkingCopyOwner());
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        final ASTNode ast = parser.createAST(null);
        return (CompilationUnit) ast;
    }

    public static ASTNode resolveClosesMethodOrTypeDeclarationNode(final JavaEditor editor) throws JavaModelException {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        if (root == null) {
            return null;
        }
        final CompilationUnit cuNode = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        final ITextSelection selection = RCPUtils.getTextSelection(editor);
        final ASTNode activeDeclarationNode = getClosestMethodOrTypeDeclarationAroundOffset(cuNode, selection);
        return activeDeclarationNode;
    }

    public static ASTNode resolveClosestTypeDeclarationNode(final JavaEditor editor) throws JavaModelException {
        final ITypeRoot root = EditorUtility.getEditorInputJavaElement(editor, true);
        if (root == null) {
            return null;
        }
        final CompilationUnit cuNode = SharedASTProvider.getAST(root, SharedASTProvider.WAIT_YES, null);
        final ITextSelection selection = RCPUtils.getTextSelection(editor);
        final ASTNode activeDeclarationNode = getClosestTypeDeclaration(cuNode, selection);
        return activeDeclarationNode;
    }

    private static ASTNode getClosestTypeDeclaration(final CompilationUnit cuNode, final ITextSelection selection) {
        ensureIsNotNull(cuNode, "cuNode");
        ensureIsNotNull(selection, "selection");
        ASTNode node = NodeFinder.perform(cuNode, selection.getOffset(), selection.getLength());
        while (node != null) {
            switch (node.getNodeType()) {
            case ASTNode.TYPE_DECLARATION:
                return node;
            }
            node = node.getParent();
        }
        return cuNode;
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
            if ((elements != null) && (elements.length > 0)) {
                return elements;
            }
        }
        return EMPTY_RESULT;
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

    public static MethodDeclaration findMethod(final CompilationUnit cuNode, final IMethodName searchedMethod) {
        final MethodDeclarationFinder finder = new MethodDeclarationFinder(false, searchedMethod);
        cuNode.accept(finder);
        return finder.match;
    }

    public static void revealInEditor(final IEditorPart editor, final TypeDeclaration type) {
        EditorUtility.revealInEditor(editor, CrASTUtil.createRegion(type.getName()));
    }

    public static void revealInEditor(final IEditorPart editor, final MethodDeclaration method) {
        EditorUtility.revealInEditor(editor, CrASTUtil.createRegion(method.getName()));
    }
}

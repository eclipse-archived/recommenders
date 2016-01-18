/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.jdt.templates;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.internal.jdt.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.recommenders.utils.Nonnull;
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/**
 * @see <a href=
 *      "http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Fconcepts%2Fconcept-template-variables.htm">
 *      Template variables</a>
 */
public class SnippetCodeBuilder {

    private final CompilationUnit ast;
    private final ASTNode startNode;
    private final IDocument doc;
    private final IRegion textSelection;

    private final Set<String> imports = new TreeSet<>();
    private final Set<String> importStatics = new TreeSet<>();
    private final HashMap<IVariableBinding, String> vars = new HashMap<>();
    private final HashMap<String, Integer> lastVarIndex = new HashMap<>();
    private final StringBuilder sb = new StringBuilder();

    public SnippetCodeBuilder(@Nonnull CompilationUnit ast, @Nonnull IDocument doc, @Nonnull IRegion textSelection) {
        this(ast, ast, doc, textSelection);
    }

    /**
     * @param startNode
     *            an {@code ASTNode} which <strong>must</strong> completely cover the {@code textSelection}. The closer
     *            the node covers the selection the better the performance of {@link SnippetCodeBuilder#build()}. If in
     *            doubt, use {@link #SnippetCodeBuilder(CompilationUnit, IDocument, IRegion)} to pass the entire
     *            {@code CompilationUnit}.
     */
    public SnippetCodeBuilder(@Nonnull ASTNode startNode, @Nonnull IDocument doc, @Nonnull IRegion textSelection) {
        this((CompilationUnit) startNode.getRoot(), startNode, doc, textSelection);
    }

    private SnippetCodeBuilder(@Nonnull CompilationUnit ast, @Nonnull ASTNode startNode, @Nonnull IDocument doc,
            @Nonnull IRegion textSelection) {
        this.ast = requireNonNull(ast);
        this.startNode = requireNonNull(startNode);
        this.doc = requireNonNull(doc);
        this.textSelection = requireNonNull(textSelection);
    }

    public String build() {
        final int start = textSelection.getOffset();
        final int length = textSelection.getLength();
        String text;
        try {
            text = doc.get(start, length);
        } catch (BadLocationException e) {
            IJavaElement javaElement = ast.getJavaElement();
            log(WARN_FAILED_TO_GET_TEXT_SELECTION, e, javaElement == null ? null : javaElement.getHandleIdentifier(),
                    start, length);
            return "";
        }
        if (text == null) {
            IJavaElement javaElement = ast.getJavaElement();
            log(WARN_FAILED_TO_GET_TEXT_SELECTION, javaElement == null ? null : javaElement.getHandleIdentifier(),
                    start, length);
            return ""; //$NON-NLS-1$
        }
        final char[] chars = text.toCharArray();

        final ASTNode enclosingNode = NodeFinder.perform(startNode, start, length);

        outer: for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // every non-identifier character can be copied right away. This is necessary since the NodeFinder sometimes
            // associates a whitespace with a previous AST node (not exactly understood yet).
            if (!Character.isJavaIdentifierPart(c)) {
                sb.append(c);
                continue;
            }

            NodeFinder nodeFinder = new NodeFinder(enclosingNode, start + i, 0);
            ASTNode node = nodeFinder.getCoveringNode();
            if (isCoveredBySelection(node)) {
                switch (node.getNodeType()) {
                case ASTNode.SIMPLE_NAME:
                    SimpleName name = (SimpleName) node;
                    IBinding b = name.resolveBinding();
                    if (b == null) {
                        break;
                    }
                    switch (b.getKind()) {
                    case IBinding.TYPE:
                        sb.append(name);
                        if (!isQualified(name) && !isDeclaredInSelection(b)) {
                            rememberImport((ITypeBinding) b);
                        }
                        i += name.getLength() - 1;
                        continue outer;
                    case IBinding.METHOD:
                        sb.append(name);
                        if (isUnqualifiedMethodInvocation(name) && isStatic(b) && !isDeclaredInSelection(b)) {
                            rememberStaticImport((IMethodBinding) b);
                        }
                        i += name.getLength() - 1;
                        continue outer;
                    case IBinding.VARIABLE:
                        IVariableBinding vb = (IVariableBinding) b;
                        String uniqueVariableName = generateUniqueVariableName(vb, remove(name.toString(), '$'));
                        if (isDeclaration(name)) {
                            if (!appendNewNameVariable(uniqueVariableName, vb)) {
                                sb.append(name);
                            }
                        } else if (isDeclaredInSelection(vb)) {
                            appendVariableReference(uniqueVariableName);
                        } else if (isQualified(name)) {
                            sb.append(name);
                        } else if (vb.isField()) {
                            if (isStatic(vb)) {
                                sb.append(name);
                                rememberStaticImport(vb);
                            } else {
                                if (!appendFieldVariable(uniqueVariableName, vb)) {
                                    sb.append(name);
                                }
                            }
                        } else {
                            appendVarVariable(uniqueVariableName, vb);
                        }
                        i += name.getLength() - 1;
                        continue outer;
                    }
                }
            }
            sb.append(c);

            if (c == '$') {
                sb.append(c);
            }
        }

        sb.append('\n');
        appendImportVariable();
        appendImportStaticVariable();
        appendCursorVariable();
        replaceLeadingWhitespaces();

        return sb.toString();
    }

    public boolean isCoveredBySelection(ASTNode node) {
        int nodeStart = node.getStartPosition();
        int nodeEnd = nodeStart + node.getLength();
        return textSelection.getOffset() <= nodeStart
                && nodeEnd <= textSelection.getOffset() + textSelection.getLength();
    }

    private boolean isDeclaredInSelection(@Nonnull IBinding binding) {
        ASTNode declaringNode = ast.findDeclaringNode(binding);
        if (declaringNode == null) {
            return false; // Declared in different compilation unit
        }
        return isCoveredBySelection(declaringNode);
    }

    private boolean isQualified(@Nonnull SimpleName name) {
        if (QualifiedName.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isUnqualifiedMethodInvocation(@Nonnull SimpleName name) {
        if (!MethodInvocation.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return false;
        }
        MethodInvocation methodInvocation = (MethodInvocation) name.getParent();
        if (methodInvocation.getExpression() != null) {
            return false;
        }
        return true;
    }

    private boolean isStatic(@Nonnull IBinding binding) {
        return Modifier.isStatic(binding.getModifiers());
    }

    private boolean isDeclaration(@Nonnull SimpleName name) {
        if (VariableDeclarationFragment.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return true;
        } else if (SingleVariableDeclaration.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return true;
        } else {
            return false;
        }
    }

    private void rememberImport(@Nonnull ITypeBinding binding) {
        // need importable types only. Get the component type if it's an array type
        if (binding.isArray()) {
            rememberImport(binding.getComponentType());
            return;
        }
        IPackageBinding packageBinding = binding.getPackage();
        if (packageBinding == null) {
            return; // Either a primitive or some generics-related binding (e.g., a type variable)
        }
        if (packageBinding.isUnnamed()) {
            return;
        }
        if (packageBinding.getName().equals("java.lang")) { //$NON-NLS-1$
            return;
        }
        ITypeBinding erasure = binding.getErasure();
        if (erasure.isRecovered()) {
            return;
        }
        imports.add(erasure.getQualifiedName());
    }

    private void rememberStaticImport(@Nonnull IMethodBinding method) {
        Preconditions.checkArgument(isStatic(method));
        rememberStaticImport(method.getDeclaringClass(), method.getName());
    }

    private void rememberStaticImport(@Nonnull IVariableBinding field) {
        Preconditions.checkArgument(field.isField());
        Preconditions.checkArgument(isStatic(field));
        rememberStaticImport(field.getDeclaringClass(), field.getName());
    }

    private void rememberStaticImport(@Nullable ITypeBinding declaringClass, @Nonnull String member) {
        if (declaringClass == null) {
            return;
        }
        IPackageBinding packageBinding = declaringClass.getPackage();
        if (packageBinding == null) {
            return; // Either a primitive or some generics-related binding (e.g., a type variable)
        }
        if (packageBinding.isUnnamed()) {
            return;
        }
        importStatics.add(declaringClass.getErasure().getQualifiedName() + '.' + member);
    }

    private String generateUniqueVariableName(@Nullable IVariableBinding binding, @Nonnull String name) {
        if (binding != null && vars.containsKey(binding)) {
            return vars.get(binding);
        } else {
            String newName = name;

            Integer i;
            if (lastVarIndex.containsKey(newName)) {
                i = lastVarIndex.get(newName);
            } else {
                i = 1;
            }

            while (vars.containsValue(newName)) {
                i++;
                newName = name.concat(i.toString());
            }
            lastVarIndex.put(name, i);
            vars.put(binding, newName);
            return newName;
        }
    }

    private boolean appendNewNameVariable(@Nonnull String name, @Nonnull IVariableBinding binding) {
        return appendVarVariableInternal("newName", name, binding); //$NON-NLS-1$
    }

    private boolean appendVariableReference(@Nonnull String name) {
        sb.append('$').append('{').append(name).append('}');
        return true;
    }

    private boolean appendFieldVariable(@Nonnull String name, @Nonnull IVariableBinding binding) {
        Preconditions.checkArgument(binding.isField());
        return appendVarVariableInternal("field", name, binding); //$NON-NLS-1$
    }

    private boolean appendVarVariable(@Nonnull String name, @Nonnull IVariableBinding binding) {
        return appendVarVariableInternal("var", name, binding); //$NON-NLS-1$
    }

    private boolean appendVarVariableInternal(@Nonnull String kind, @Nonnull String name,
            @Nonnull IVariableBinding binding) {
        ITypeBinding type = binding.getType();
        if (type == null) {
            return false;
        }
        ITypeBinding erasure = type.getErasure();
        if (erasure == null) {
            return false;
        }
        if (erasure.isRecovered()) {
            return false;
        }
        sb.append('$').append('{').append(name).append(':').append(kind).append('(');
        if (type.isArray()) {
            sb.append('\'').append(erasure.getQualifiedName()).append('\'');
        } else {
            sb.append(erasure.getQualifiedName());
        }
        sb.append(')').append('}');
        return true;
    }

    private boolean appendImportVariable() {
        return appendImportVariableInternal("import", imports); //$NON-NLS-1$
    }

    private boolean appendImportStaticVariable() {
        return appendImportVariableInternal("importStatic", importStatics); //$NON-NLS-1$
    }

    private boolean appendImportVariableInternal(@Nonnull String name, @Nonnull Collection<String> imports) {
        if (imports.isEmpty()) {
            return false;
        }

        String uniqueName = generateUniqueVariableName(null, name);
        String joinedImports = Joiner.on(", ").join(imports); //$NON-NLS-1$
        sb.append('$').append('{').append(uniqueName).append(':').append(name).append('(').append(joinedImports)
                .append(')').append('}');
        return true;
    }

    private void appendCursorVariable() {
        sb.append("${cursor}"); //$NON-NLS-1$
    }

    private void replaceLeadingWhitespaces() {
        try {
            // fetch the selection's starting line from the editor document to
            // determine the number of leading
            // whitespace characters to remove from the snippet:
            IRegion firstLineInfo = doc.getLineInformationOfOffset(textSelection.getOffset());
            String line = doc.get(firstLineInfo.getOffset(), firstLineInfo.getLength());

            int index = 0;
            for (; index < line.length(); index++) {
                if (!Character.isWhitespace(line.charAt(index))) {
                    break;
                }
            }
            String wsPrefix = line.substring(0, index);

            // rewrite the buffer and try to remove the leading whitespace. This
            // is a simple heuristic only...
            String[] code = sb.toString().split("\\r?\\n"); //$NON-NLS-1$
            sb.setLength(0);
            for (String l : code) {
                String clean = StringUtils.removeStart(l, wsPrefix);
                sb.append(clean).append(LINE_SEPARATOR);
            }
        } catch (BadLocationException e) {
            log(ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED, e);
        }
    }
}

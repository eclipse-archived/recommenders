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
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.internal.jdt.l10n.LogMessages.*;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
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
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * @see <a href=
 *      "http://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Fconcepts%2Fconcept-template-variables.htm">
 *      Template variables</a>
 */
public class SnippetCodeBuilder {

    private final CompilationUnit ast;
    private final ASTNode startNode;
    private final IDocument document;
    private final IRegion textSelection;
    private final Map<ASTNode, String> nodesToReplace;

    private final Set<String> imports = new TreeSet<>();
    private final Set<String> importStatics = new TreeSet<>();

    private final HashMap<IVariableBinding, String> templateVariableNameReferences = new HashMap<>();
    private final HashSet<String> assignedTemplateVariablesNames = new HashSet<>();
    private final HashMap<String, Integer> lastTemplateVariableIndex = new HashMap<>();

    private final StringBuilder code = new StringBuilder();

    /**
     * A convenience constructor calling
     * {@link SnippetCodeBuilder#SnippetCodeBuilder(CompilationUnit, IDocument, IRegion, Map) with an empty map, i.e.,
     * no nodes to replace.
     */
    public SnippetCodeBuilder(CompilationUnit ast, IDocument document, IRegion textSelection) {
        this(ast, document, textSelection, Collections.<ASTNode, String>emptyMap());
    }

    /**
     * @param nodesToReplace
     *            a map whose keys are {@code ASTNode}s completely covered by {@code textSelection} which will be
     *            replaced by template variables. The map's values are the preferred template variable names for the
     *            corresponding nodes. A {@code ASTNode} will be replaced by
     *            <code>${variableName:var(typeOfExpression)}</code> if it is an {@link Expression} and by
     *            <code>${variableName}</code> otherwise.
     *
     * @since 2.2.6
     */
    public SnippetCodeBuilder(CompilationUnit ast, IDocument document, IRegion textSelection,
            Map<ASTNode, String> nodesToReplace) {
        this(ast, ast, document, textSelection, nodesToReplace);
    }

    /**
     * A convenience constructor calling {@link SnippetCodeBuilder#SnippetCodeBuilder(ASTNode, IDocument, IRegion, Map)
     * with an empty map, i.e., no nodes to replace.
     */
    public SnippetCodeBuilder(ASTNode startNode, IDocument document, IRegion textSelection) {
        this(startNode, document, textSelection, Collections.<ASTNode, String>emptyMap());
    }

    /**
     * @param startNode
     *            an {@code ASTNode} which <strong>must</strong> completely cover the {@code textSelection}. The closer
     *            the node covers the selection the better the performance of {@link SnippetCodeBuilder#build()} will
     *            be. If in doubt, use {@link #SnippetCodeBuilder(CompilationUnit, IDocument, IRegion)} to pass an
     *            entire {@code CompilationUnit}
     * @param nodesToReplace
     *            a map whose keys are {@code ASTNode}s below {@code startNode} which will be replaced by template
     *            variables. The map's values are the preferred template variable names for the corresponding nodes. A
     *            {@code ASTNode} will be replaced by <code>${variableName:var(typeOfExpression)}</code> if it is an
     *            {@link Expression} and by <code>${variableName}</code> otherwise.
     *
     * @since 2.2.6
     */
    public SnippetCodeBuilder(ASTNode startNode, IDocument document, IRegion textSelection,
            Map<ASTNode, String> nodesToReplace) {
        this((CompilationUnit) startNode.getRoot(), startNode, document, textSelection, nodesToReplace);
    }

    private SnippetCodeBuilder(CompilationUnit ast, ASTNode startNode, IDocument document, IRegion textSelection,
            Map<ASTNode, String> nodesToReplace) {
        this.ast = requireNonNull(ast);
        this.startNode = requireNonNull(startNode);
        this.document = requireNonNull(document);
        this.textSelection = requireNonNull(textSelection);
        this.nodesToReplace = requireNonNull(nodesToReplace);
    }

    public String build() {
        final int start = textSelection.getOffset();
        final int length = textSelection.getLength();
        String text;
        try {
            text = document.get(start, length);
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
            int offset = start + i;

            for (Entry<ASTNode, String> entry : nodesToReplace.entrySet()) {
                ASTNode nodeToReplace = entry.getKey();
                String preferredName = entry.getValue();
                if (offset == nodeToReplace.getStartPosition()
                        && nodeToReplace.getStartPosition() + nodeToReplace.getLength() <= offset + chars.length) {
                    if (!(nodeToReplace instanceof Expression)) {
                        appendTemplateVariableReference(preferredName);
                        i += nodeToReplace.getLength() - 1;
                        continue outer;
                    }
                    Expression expressionToReplace = (Expression) nodeToReplace;
                    ITypeBinding typeBinding = expressionToReplace.resolveTypeBinding();
                    if (typeBinding == null) {
                        appendTemplateVariableReference(preferredName);
                        i += nodeToReplace.getLength() - 1;
                        continue outer;
                    }
                    String templateVariableName = createTemplateVariableName(preferredName);
                    if (!appendTypedTemplateVariableInternal(templateVariableName, "var", typeBinding)) {
                        appendTemplateVariableReference(templateVariableName);
                        i += nodeToReplace.getLength() - 1;
                        continue outer;
                    }
                    i += nodeToReplace.getLength() - 1;
                    continue outer;
                }
            }

            char c = chars[i];
            // every non-identifier character can be copied right away. This is necessary since the NodeFinder sometimes
            // associates a whitespace with a previous AST node (not exactly understood yet).
            if (!Character.isJavaIdentifierPart(c)) {
                code.append(c);
                continue;
            }

            NodeFinder nodeFinder = new NodeFinder(enclosingNode, offset, 0);
            ASTNode node = nodeFinder.getCoveringNode();
            if (

            isCoveredBySelection(node)) {
                switch (node.getNodeType()) {
                case ASTNode.SIMPLE_NAME:
                    SimpleName name = (SimpleName) node;
                    IBinding binding = name.resolveBinding();
                    if (binding == null) {
                        break;
                    }
                    switch (binding.getKind()) {
                    case IBinding.TYPE:
                        ITypeBinding typeBinding = (ITypeBinding) binding;
                        if (isUnqualified(name) && !isDeclaredInSelection(typeBinding)) {
                            rememberImport(typeBinding);
                        }
                        code.append(name);
                        i += name.getLength() - 1;
                        continue outer;
                    case IBinding.METHOD:
                        IMethodBinding methodBinding = (IMethodBinding) binding;
                        if (isUnqualifiedMethodInvocation(name) && isStatic(methodBinding)
                                && !isDeclaredInSelection(methodBinding)) {
                            rememberStaticImport(methodBinding);
                        }
                        code.append(name);
                        i += name.getLength() - 1;
                        continue outer;
                    case IBinding.VARIABLE:
                        IVariableBinding variableBinding = (IVariableBinding) binding;
                        if (isDeclaration(name)) {
                            if (!appendNewNameTemplateVariable(name.getIdentifier(), variableBinding)) {
                                code.append(name);
                            }
                        } else if (isDeclaredInSelection(variableBinding)) {
                            appendTemplateVariableReference(variableBinding);
                        } else if (!isUnqualified(name)) {
                            code.append(name);
                        } else if (variableBinding.isField()) {
                            if (isStatic(variableBinding)) {
                                code.append(name);
                                rememberStaticImport(variableBinding);
                            } else {
                                if (!appendFieldTemplateVariable(name.getIdentifier(), variableBinding)) {
                                    code.append(name);
                                }
                            }
                        } else {
                            appendVarTemplateVariable(name.getIdentifier(), variableBinding);
                        }
                        i += name.getLength() - 1;
                        continue outer;
                    }
                }
            }
            code.append(c);
            if (c == '$') {
                code.append(c);
            }
        }

        code.append('\n');

        replaceLeadingWhitespaces();

        appendImportTemplateVariable();

        appendImportStaticTemplateVariable();

        appendCursorTemplateVariable();

        return code.toString();

    }

    public boolean isCoveredBySelection(ASTNode node) {
        int nodeStart = node.getStartPosition();
        int nodeEnd = nodeStart + node.getLength();
        return textSelection.getOffset() <= nodeStart
                && nodeEnd <= textSelection.getOffset() + textSelection.getLength();
    }

    private boolean isDeclaredInSelection(IBinding binding) {
        ASTNode declaringNode = ast.findDeclaringNode(binding);
        if (declaringNode == null) {
            return false; // Declared in different compilation unit
        }
        return isCoveredBySelection(declaringNode);
    }

    private boolean isUnqualified(SimpleName name) {
        return !QualifiedName.NAME_PROPERTY.equals(name.getLocationInParent());
    }

    private boolean isUnqualifiedMethodInvocation(SimpleName name) {
        if (!MethodInvocation.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return false;
        }
        MethodInvocation methodInvocation = (MethodInvocation) name.getParent();
        if (methodInvocation.getExpression() != null) {
            return false;
        }
        return true;
    }

    private boolean isStatic(IBinding binding) {
        return Modifier.isStatic(binding.getModifiers());
    }

    private boolean isDeclaration(SimpleName name) {
        if (VariableDeclarationFragment.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return true;
        } else if (SingleVariableDeclaration.NAME_PROPERTY.equals(name.getLocationInParent())) {
            return true;
        } else {
            return false;
        }
    }

    private void rememberImport(ITypeBinding binding) {
        // Remember importable types only. Get the component type if it's an array type
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

    private void rememberStaticImport(IMethodBinding method) {
        Preconditions.checkArgument(isStatic(method));
        rememberStaticImport(method.getDeclaringClass(), method.getName());
    }

    private void rememberStaticImport(IVariableBinding field) {
        Preconditions.checkArgument(field.isField());
        Preconditions.checkArgument(isStatic(field));
        rememberStaticImport(field.getDeclaringClass(), field.getName());
    }

    private void rememberStaticImport(@Nullable ITypeBinding declaringTypeBinding, String member) {
        if (declaringTypeBinding == null) {
            return;
        }
        IPackageBinding packageBinding = declaringTypeBinding.getPackage();
        if (packageBinding == null) {
            return; // Either a primitive or some generics-related binding (e.g., a type variable)
        }
        if (packageBinding.isUnnamed()) {
            return;
        }
        importStatics.add(declaringTypeBinding.getErasure().getQualifiedName() + '.' + member);
    }

    private boolean appendTemplateVariableReference(String preferredName) {
        String templateVariableName = createTemplateVariableName(preferredName);
        code.append('$').append('{').append(templateVariableName).append('}');
        return true;
    }

    private boolean appendTemplateVariableReference(IVariableBinding variableBinding) {
        String templateVariableName = findTemplateVariableName(variableBinding).orNull();
        if (templateVariableName != null) {
            code.append('$').append('{').append(templateVariableName).append('}');
            return true;
        } else {
            return false;
        }
    }

    private boolean appendNewNameTemplateVariable(String preferredName, IVariableBinding variableBinding) {
        if (appendTemplateVariableReference(variableBinding)) {
            return true;
        }
        String templateVariableName = createTemplateVariableName(preferredName, variableBinding);
        ITypeBinding type = variableBinding.getType();
        return appendTypedTemplateVariableInternal(templateVariableName, "newName", type); //$NON-NLS-1$
    }

    private boolean appendFieldTemplateVariable(String preferredName, IVariableBinding variableBinding) {
        Preconditions.checkArgument(variableBinding.isField());
        if (appendTemplateVariableReference(variableBinding)) {
            return true;
        }
        String templateVariableName = createTemplateVariableName(preferredName, variableBinding);
        ITypeBinding typeBinding = variableBinding.getType();
        return appendTypedTemplateVariableInternal(templateVariableName, "field", typeBinding); //$NON-NLS-1$
    }

    private boolean appendVarTemplateVariable(String preferredName, IVariableBinding variableBinding) {
        Preconditions.checkArgument(!variableBinding.isField());
        if (appendTemplateVariableReference(variableBinding)) {
            return true;
        }
        String templateVariableName = createTemplateVariableName(preferredName, variableBinding);
        ITypeBinding typeBinding = variableBinding.getType();
        return appendTypedTemplateVariableInternal(templateVariableName, "var", typeBinding); //$NON-NLS-1$
    }

    private boolean appendTypedTemplateVariableInternal(String templateVariableName, String kind,
            @Nullable ITypeBinding typeBinding) {
        if (typeBinding == null) {
            return false;
        }
        ITypeBinding erasure = typeBinding.getErasure();
        if (erasure == null) {
            return false;
        }
        if (erasure.isRecovered()) {
            return false;
        }
        code.append('$').append('{').append(templateVariableName).append(':').append(kind).append('(');
        if (typeBinding.isArray()) {
            code.append('\'').append(erasure.getQualifiedName()).append('\'');
        } else {
            code.append(erasure.getQualifiedName());
        }
        code.append(')').append('}');
        return true;
    }

    private Optional<String> findTemplateVariableName(IVariableBinding variable) {
        return Optional.fromNullable(templateVariableNameReferences.get(variable));
    }

    private String createTemplateVariableName(String preferredName, IVariableBinding variableBinding) {
        Preconditions.checkState(!templateVariableNameReferences.containsKey(variableBinding));
        String assignedName = createTemplateVariableName(preferredName);
        templateVariableNameReferences.put(variableBinding, assignedName);
        return assignedName;
    }

    private String createTemplateVariableName(String preferredName) {
        String sanitizedPreferredName = preferredName.replace('$', '_');
        String candidateName = sanitizedPreferredName;

        Integer i;
        if (lastTemplateVariableIndex.containsKey(candidateName)) {
            i = lastTemplateVariableIndex.get(candidateName);
        } else {
            i = 1;
        }

        while (assignedTemplateVariablesNames.contains(candidateName)) {
            candidateName = sanitizedPreferredName.concat(Integer.toString(i++));
        }

        String assignedName = candidateName;
        assignedTemplateVariablesNames.add(assignedName);
        lastTemplateVariableIndex.put(assignedName, i);
        return assignedName;
    }

    private boolean appendImportTemplateVariable() {
        return appendStringTemplateVariableInternal("import", imports); //$NON-NLS-1$
    }

    private boolean appendImportStaticTemplateVariable() {
        return appendStringTemplateVariableInternal("importStatic", importStatics); //$NON-NLS-1$
    }

    private boolean appendStringTemplateVariableInternal(String kind, Collection<String> imports) {
        if (imports.isEmpty()) {
            return false;
        }

        String joinedImports = Joiner.on(", ").join(imports); //$NON-NLS-1$
        code.append('$').append('{').append(':').append(kind).append('(').append(joinedImports).append(')').append('}');
        return true;
    }

    private void appendCursorTemplateVariable() {
        code.append("${cursor}").append(LINE_SEPARATOR); //$NON-NLS-1$
    }

    private void replaceLeadingWhitespaces() {
        try {
            // fetch the selection's starting line from the editor document to
            // determine the number of leading
            // whitespace characters to remove from the snippet:
            IRegion firstLineInfo = document.getLineInformationOfOffset(textSelection.getOffset());
            String line = document.get(firstLineInfo.getOffset(), firstLineInfo.getLength());

            int index = 0;
            for (; index < line.length(); index++) {
                if (!Character.isWhitespace(line.charAt(index))) {
                    break;
                }
            }
            String wsPrefix = line.substring(0, index);

            // rewrite the buffer and try to remove the leading whitespace. This
            // is a simple heuristic only...
            String[] lines = code.toString().split("\\r?\\n"); //$NON-NLS-1$
            code.setLength(0);
            for (String l : lines) {
                String clean = StringUtils.removeStart(l, wsPrefix);
                code.append(clean).append(LINE_SEPARATOR);
            }
        } catch (BadLocationException e) {
            log(ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED, e);
        }
    }
}

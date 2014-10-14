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
package org.eclipse.recommenders.snipmatch.rcp.util;

import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.internal.snipmatch.rcp.LogMessages.ERROR_SNIPPET_REPLACE_LEADING_WHITESPACE_FAILED;
import static org.eclipse.recommenders.utils.Logs.log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SnippetCodeBuilder {

    private final CompilationUnit ast;
    private final IDocument doc;
    private final ITextSelection textSelection;

    private Set<String> imports;
    private Set<String> importStatics;
    private HashMap<IVariableBinding, String> vars;
    private HashMap<String, Integer> lastVarIndex;
    private StringBuilder sb;

    public SnippetCodeBuilder(CompilationUnit ast, IDocument doc, ITextSelection textSelection) {
        this.ast = ast;
        this.doc = doc;
        this.textSelection = textSelection;
    }

    public String build() {
        final int start = textSelection.getOffset();
        final int length = textSelection.getLength();
        final char[] text = textSelection.getText().toCharArray();

        final ASTNode enclosingNode = NodeFinder.perform(ast, start, length);
        final Selection selection = Selection.createFromStartLength(start, length);

        imports = Sets.newTreeSet();
        importStatics = Sets.newTreeSet();
        vars = Maps.newHashMap();
        lastVarIndex = Maps.newHashMap();
        sb = new StringBuilder();

        outer: for (int i = 0; i < text.length; i++) {
            char ch = text[i];
            // every non-identifier character can be copied right away. This is
            // necessary since the NodeFinder sometimes
            // associates a whitespace with a previous AST node (not exactly
            // understood yet).
            if (!Character.isJavaIdentifierPart(ch)) {
                sb.append(ch);
                continue outer;
            }

            NodeFinder nodeFinder = new NodeFinder(enclosingNode, start + i, 0);
            ASTNode node = nodeFinder.getCoveringNode();
            if (selection.covers(node)) {
                switch (node.getNodeType()) {
                case ASTNode.SIMPLE_NAME:
                    SimpleName name = (SimpleName) node;
                    IBinding b = name.resolveBinding();
                    if (b == null) {
                        break;
                    }
                    switch (b.getKind()) {
                    case IBinding.TYPE:
                        ITypeBinding tb = (ITypeBinding) b;
                        appendTypeBinding(name, tb);
                        i += name.getLength() - 1;
                        continue outer;
                    case IBinding.VARIABLE:
                        IVariableBinding vb = (IVariableBinding) b;
                        String uniqueVariableName = generateUniqueVariableName(vb, name.toString());
                        if (isDeclaration(name)) {
                            appendNewName(uniqueVariableName, vb);
                        } else if (declaredInSelection(selection, vb)) {
                            appendTemplateVariableReference(uniqueVariableName);
                        } else if (isQualified(name)) {
                            sb.append(name.getIdentifier());
                        } else if (vb.isField()) {
                            if (Modifier.isStatic(b.getModifiers())) {
                                sb.append(name.getIdentifier());
                                addStaticImport(vb);
                            } else {
                                appendVarReference(uniqueVariableName, vb, "field"); //$NON-NLS-1$
                            }
                        } else {
                            appendVarReference(uniqueVariableName, vb, "var"); //$NON-NLS-1$
                        }
                        i += name.getLength() - 1;
                        continue outer;
                    }
                }
            }
            sb.append(ch);
        }

        sb.append('\n');
        appendImportVariable("import", imports);
        appendImportVariable("importStatic", importStatics);
        appendCursor();
        replaceLeadingWhitespaces();

        return sb.toString();
    }

    private boolean isDeclaration(SimpleName name) {
        StructuralPropertyDescriptor locationInParent = name.getLocationInParent();

        if (locationInParent == VariableDeclarationFragment.NAME_PROPERTY) {
            return true;
        } else if (locationInParent == SingleVariableDeclaration.NAME_PROPERTY) {
            return true;
        } else {
            return false;
        }
    }

    private boolean declaredInSelection(Selection selection, IVariableBinding vb) {
        ASTNode declaringNode = ast.findDeclaringNode(vb);
        if (declaringNode == null) {
            return false; // Declared in different compilation unit
        }
        return selection.covers(declaringNode);
    }

    private boolean isQualified(SimpleName node) {
        StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
        if (locationInParent == QualifiedName.NAME_PROPERTY) {
            return true;
        } else {
            return false;
        }
    }

    private String generateUniqueVariableName(IVariableBinding vb, String name) {
        if (vb != null && vars.containsKey(vb)) {
            return vars.get(vb);
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
            vars.put(vb, newName);
            return newName;
        }
    }

    private void appendTypeBinding(SimpleName name, ITypeBinding tb) {
        sb.append(name);
        addImport(tb);
    }

    private void appendNewName(String name, IVariableBinding vb) {
        ITypeBinding type = vb.getType();
        sb.append('$').append('{').append(name).append(':').append("newName").append('('); //$NON-NLS-1$
        if (type.isArray()) {
            sb.append('\'').append(type.getErasure().getQualifiedName()).append('\'');
        } else {
            sb.append(type.getErasure().getQualifiedName());
        }
        sb.append(')').append('}');

        addImport(type);
    }

    private StringBuilder appendTemplateVariableReference(String name) {
        return sb.append('$').append('{').append(name).append('}');
    }

    private void appendVarReference(String name, IVariableBinding vb, String kind) {
        ITypeBinding type = vb.getType();
        sb.append('$').append('{').append(name).append(':').append(kind).append('(');
        if (type.isArray()) {
            sb.append('\'').append(type.getErasure().getQualifiedName()).append('\'');
        } else {
            sb.append(type.getErasure().getQualifiedName());
        }
        sb.append(')').append('}');

        addImport(type);
    }

    private void appendImportVariable(String name, Collection<String> imports) {
        if (!imports.isEmpty()) {
            String uniqueName = generateUniqueVariableName(null, name);
            String joinedImports = Joiner.on(", ").join(imports); //$NON-NLS-1$
            sb.append('$').append('{').append(uniqueName).append(':').append(name).append('(').append(joinedImports)
            .append(')').append('}');
        }
    }

    private void appendCursor() {
        sb.append("${cursor}"); //$NON-NLS-1$
    }

    private void addImport(ITypeBinding type) {
        // need importable types only. Get the component type if it's an array type
        if (type.isArray()) {
            addImport(type.getComponentType());
            return;
        }
        if (type.isPrimitive()) {
            return;
        }
        if (type.getPackage().getName().equals("java.lang")) { //$NON-NLS-1$
            return;
        }
        String name = type.getErasure().getQualifiedName();
        imports.add(name);
    }

    private void addStaticImport(IVariableBinding binding) {
        ITypeBinding declaringClass = binding.getDeclaringClass();
        if (declaringClass == null) {
            return;
        }

        String name = declaringClass.getErasure().getQualifiedName();
        importStatics.add(name + "." + binding.getName());
    }

    private void replaceLeadingWhitespaces() {
        try {
            // fetch the selection's starting line from the editor document to
            // determine the number of leading
            // whitespace characters to remove from the snippet:
            int startLineIndex = textSelection.getStartLine();
            int startLineBeginOffset = doc.getLineOffset(startLineIndex);
            int startLineEndOffset = doc.getLineOffset(startLineIndex + 1) - 1;
            int lineLength = startLineEndOffset - startLineBeginOffset;
            String line = doc.get(startLineBeginOffset, lineLength);

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

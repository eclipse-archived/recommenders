/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.util;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.collect.Sets;

public class DependencyExtractor {

    private final Set<ProjectCoordinate> dependencies = Sets.newHashSet();
    private final ASTNode enclosingNode;
    private final IProjectCoordinateProvider pcProvider;
    private final Selection selection;

    public DependencyExtractor(ASTNode node, IProjectCoordinateProvider pcProvider) {
        this.enclosingNode = node;
        this.pcProvider = pcProvider;
        this.selection = Selection.createFromStartLength(node.getStartPosition(), node.getLength());
    }

    public DependencyExtractor(CompilationUnit ast, ITextSelection textSelection, IProjectCoordinateProvider pcProvider) {
        this.enclosingNode = NodeFinder.perform(ast, textSelection.getOffset(), textSelection.getLength());
        this.pcProvider = pcProvider;
        this.selection = Selection.createFromStartLength(textSelection.getOffset(), textSelection.getLength());
    }

    public Set<ProjectCoordinate> extractDependencies() {
        enclosingNode.accept(new ASTVisitor() {

            @Override
            public boolean visit(SimpleName simpleName) {
                if (selection.covers(simpleName)) {
                    IBinding binding = simpleName.resolveBinding();
                    if (binding == null) {
                        return super.visit(simpleName);
                    }
                    switch (binding.getKind()) {
                    case IBinding.TYPE:
                        processVariableBinding((ITypeBinding) binding);
                        break;
                    case IBinding.VARIABLE:
                        processVariableBinding((IVariableBinding) binding);
                        break;
                    }
                }
                return super.visit(simpleName);
            }

            private void processVariableBinding(@Nullable ITypeBinding binding) {
                if (binding == null) {
                    return;
                }
                ProjectCoordinate pc = pcProvider.resolve(binding).orNull();
                if (pc == null) {
                    return;
                }
                dependencies.add(new ProjectCoordinate(pc.getGroupId(), pc.getArtifactId(), "0.0.0")); //$NON-NLS-1$
            }

            private void processVariableBinding(@Nullable IVariableBinding binding) {
                if (binding == null) {
                    return;
                }
                processVariableBinding(binding.getType());
            }
        });
        return dependencies;
    }
}

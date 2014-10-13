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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.collect.Sets;

public class DependencyExtractor {

    private final Set<ProjectCoordinate> dependencies;
    private final CompilationUnit ast;
    private final ITextSelection textSelection;
    private final IProjectCoordinateProvider pcProdivder;

    public DependencyExtractor(CompilationUnit ast, ITextSelection textSelection, IProjectCoordinateProvider pcProvider) {
        this.pcProdivder = pcProvider;
        this.dependencies = Sets.newHashSet();
        this.ast = ast;
        this.textSelection = textSelection;
    }

    public Set<ProjectCoordinate> extractDependencies() {
        final int start = textSelection.getOffset();
        final int length = textSelection.getLength();
        final ASTNode enclosingNode = NodeFinder.perform(ast, start, length);
        enclosingNode.accept(new ASTVisitor() {

            @Override
            public boolean visit(SimpleName simpleName) {
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
                return super.visit(simpleName);
            }

            private void processVariableBinding(@Nullable ITypeBinding binding) {
                if (binding == null) {
                    return;
                }
                ProjectCoordinate pc = pcProdivder.resolve(binding).orNull();
                if (pc == null) {
                    return;
                }
                dependencies.add(new ProjectCoordinate(pc.getGroupId(), pc.getArtifactId(), "0.0.0")); //$NON-NLS-1$
            }

            private void processVariableBinding(@Nullable IVariableBinding binding) {
                if (binding == null) {
                    return;
                }
                processVariableBinding(binding.getDeclaringClass());
            }
        });
        return dependencies;
    }
}

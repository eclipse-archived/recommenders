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
package org.eclipse.recommenders.internal.rcp;

import static org.eclipse.recommenders.utils.Checks.cast;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.recommenders.rcp.IAstProvider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Singleton;

@Singleton
public class CachingAstProvider implements IAstProvider, IElementChangedListener {

    private final Cache<ICompilationUnit, CompilationUnit> cache = CacheBuilder.newBuilder().maximumSize(20).build();

    @Override
    public CompilationUnit get(final ICompilationUnit compilationUnit) {
        CompilationUnit ast = cache.getIfPresent(compilationUnit);
        if (ast == null) {
            ast = SharedASTProvider.getAST(compilationUnit, SharedASTProvider.WAIT_YES, null);
            if (ast != null) {
                cache.put(compilationUnit, ast);
            }
        }
        return ast;
    }

    @Override
    public void elementChanged(final ElementChangedEvent event) {
        final IJavaElementDelta delta = event.getDelta();
        final CompilationUnit ast = delta.getCompilationUnitAST();
        if (ast == null) {
            return;
        }
        final ICompilationUnit cu = cast(ast.getJavaElement());
        if (cu == null) {
            return;
        }
        cache.put(cu, ast);
    }
}

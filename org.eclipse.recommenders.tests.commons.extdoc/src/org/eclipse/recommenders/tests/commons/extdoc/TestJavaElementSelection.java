/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.commons.extdoc;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import org.mockito.Mockito;

@SuppressWarnings("restriction")
public class TestJavaElementSelection implements IJavaElementSelection {

    private static final CompilationUnit compilationUnit = Mockito.mock(CompilationUnit.class);
    static {
        Mockito.when(compilationUnit.getJavaProject()).thenReturn(TestTypeUtils.getDefaultProject());
    }

    private final IJavaElement javaElement;
    private final JavaElementLocation location;
    private final IEditorPart editorPart = new EditorPart() {

        @Override
        public void doSave(final IProgressMonitor monitor) {
        }

        @Override
        public void doSaveAs() {
        }

        @Override
        public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        }

        @Override
        public boolean isDirty() {
            return false;
        }

        @Override
        public boolean isSaveAsAllowed() {
            return false;
        }

        @Override
        public void createPartControl(final Composite parent) {
        }

        @Override
        public void setFocus() {
        }
    };

    public TestJavaElementSelection(final JavaElementLocation location, final IJavaElement javaElement) {
        this.javaElement = javaElement;
        this.location = location;
    }

    @Override
    public IJavaElement getJavaElement() {
        return javaElement;
    }

    @Override
    public JavaElementLocation getElementLocation() {
        return location;
    }

    @Override
    public int getInvocationOffset() {
        return 0;
    }

    @Override
    public ITypeRoot getCompilationUnit() {
        return compilationUnit;
    }

    @Override
    public ASTNode getAstNode() {
        return null;
    }

    @Override
    public IEditorPart getEditor() {
        return editorPart;
    }

    @Override
    public IJavaElementSelection copy(final IJavaElement element) {
        return null;
    }

}

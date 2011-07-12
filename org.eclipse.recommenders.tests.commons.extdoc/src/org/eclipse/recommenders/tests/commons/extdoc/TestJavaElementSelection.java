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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.ui.IEditorPart;
import org.mockito.Mockito;

class TestJavaElementSelection implements IJavaElementSelection {

    private IType javaElement;
    private final IEditorPart editorPart = Mockito.mock(IEditorPart.class);

    @Override
    public IJavaElement getJavaElement() {
        if (javaElement == null) {
            javaElement = Mockito.mock(IType.class);
            Mockito.when(javaElement.getPrimaryElement()).thenReturn(javaElement);
            Mockito.when(javaElement.getElementName()).thenReturn("Composite");
            Mockito.when(javaElement.getHandleIdentifier()).thenReturn("TestIdentifier");
            Mockito.when(javaElement.getFullyQualifiedName()).thenReturn("org/eclipse/swt/widgets/Composite");
        }
        return javaElement;
    }

    @Override
    public JavaElementLocation getElementLocation() {
        return JavaElementLocation.METHOD_BODY;
    }

    @Override
    public int getInvocationOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ITypeRoot getCompilationUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ASTNode getAstNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEditorPart getEditor() {
        return editorPart;
    }

}

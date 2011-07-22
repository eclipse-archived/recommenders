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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;

import org.junit.Test;
import org.mockito.Mockito;

public final class CallsProviderTest {

    @Test
    public void testCallsProvider() throws JavaModelException {
        final CallsProvider provider = new CallsProvider(null, null, null, null);
        final IJavaElementSelection selection = UnitTestSuite.getSelection();

        final ILocalVariable variable = Mockito.mock(ILocalVariable.class);
        Mockito.when(variable.getTypeSignature()).thenReturn("Button;");
        final IJavaProject project = Mockito.mock(IJavaProject.class);
        final IType type = Mockito.mock(IType.class);
        // Mockito.when(project.findType(Mockito.anyString())).thenReturn(type);
        Mockito.when(variable.getJavaProject()).thenReturn(project);
        Mockito.when(selection.getJavaElement()).thenReturn(variable);
        provider.selectionChanged(selection);
    }
}

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
package org.eclipse.recommenders.tests.internal.extdoc.rcp.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.CallsProvider;
import org.eclipse.recommenders.tests.extdoc.ExtDocUtils;
import org.eclipse.recommenders.tests.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.extdoc.TestJavaElementSelection;
import org.eclipse.recommenders.tests.extdoc.TestTypeUtils;
import org.eclipse.recommenders.tests.internal.extdoc.rcp.providers.utils.CallsAdapterTest;
import org.eclipse.swt.widgets.Composite;
import org.junit.Assert;
import org.junit.Test;

public final class CallsProviderTest {

    private final CallsProvider provider = new CallsProvider(CallsAdapterTest.createProjectServices(), null,
            ServerUtils.getGenericServer());
    private final Composite composite = provider.createComposite(ExtDocUtils.getShell(), null);

    @Test
    public void testMethodBody() throws JavaModelException {
        for (final IJavaElement element : TestTypeUtils.getDefaultElements()) {
            testSelection(JavaElementLocation.METHOD_BODY, element);
        }
    }

    @Test
    public void testFieldDeclaration() {
        for (final IJavaElement element : TestTypeUtils.getDefaultElements()) {
            testSelection(JavaElementLocation.FIELD_DECLARATION, element);
        }
    }

    @Test
    public void testMethodDeclaration() {
        for (final IJavaElement element : TestTypeUtils.getDefaultElements()) {
            testSelection(JavaElementLocation.METHOD_DECLARATION, element);
        }
    }

    @Test
    public void testTypeDeclaration() {
        for (final IJavaElement element : TestTypeUtils.getDefaultElements()) {
            testSelection(JavaElementLocation.TYPE_DECLARATION, element);
        }
    }

    private void testSelection(final JavaElementLocation location, final IJavaElement element) {
        final TestJavaElementSelection selection = ExtDocUtils.getSelection(JavaElementLocation.METHOD_BODY,
                TestTypeUtils.getDefaultJavaMethod());
        Assert.assertTrue(provider.selectionChanged(selection, composite));
    }
}

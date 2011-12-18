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
package org.eclipse.recommenders.tests.extdoc;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.IProvider;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.ElementResolver;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.mockito.Mockito;

@SuppressWarnings("restriction")
public final class ExtDocUtils {

    private static TestProvider provider = new TestProvider();
    private static TestJavaElementSelection selection = getSelection(JavaElementLocation.METHOD_BODY,
            TestTypeUtils.getDefaultJavaType());
    private static Shell shell = new Shell();
    private static IWorkbenchWindow workbenchWindow = Mockito.mock(IWorkbenchWindow.class);

    static {
        ElementResolver.setJavaElementResolver(new JavaElementResolver());
    }

    private ExtDocUtils() {
    }

    public static IProvider getTestProvider() {
        return provider;
    }

    public static TestJavaElementSelection getSelection() {
        return selection;
    }

    public static TestJavaElementSelection getSelection(final JavaElementLocation location, final IJavaElement element) {
        return new TestJavaElementSelection(location, element);
    }

    public static Shell getShell() {
        return shell;
    }

    public static IWorkbenchWindow getWorkbenchWindow() {
        return workbenchWindow;
    }

}

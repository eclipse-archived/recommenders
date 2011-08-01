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

import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public final class ExtDocUtils {

    private static TestProvider provider = new TestProvider();
    private static TestJavaElementSelection selection = new TestJavaElementSelection(JavaElementLocation.METHOD_BODY,
            TestTypeUtils.getDefaultJavaType());
    private static Shell shell = new Shell();

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

    public static Shell getShell() {
        return shell;
    }

}

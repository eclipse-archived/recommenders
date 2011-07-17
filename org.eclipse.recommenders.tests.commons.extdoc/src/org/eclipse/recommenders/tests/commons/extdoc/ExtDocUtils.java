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

import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.swt.widgets.Composite;

public final class ExtDocUtils {

    private static TestProvider provider;
    private static IJavaElementSelection selection;
    private static Composite composite;

    private ExtDocUtils() {
    }

    public static IProvider getTestProvider() {
        if (provider == null) {
            provider = new TestProvider();
        }
        return provider;
    }

    public static IJavaElementSelection getSelection() {
        if (selection == null) {
            selection = new TestJavaElementSelection();
        }
        return selection;
    }

    public static Composite getComposite() {
        if (composite == null) {
            // TODO: Some way to "fake" a shell?
            composite = null;
        }
        return composite;
    }

    public static JavaElementResolver getResolver() {
        return new JavaElementResolver();
    }

}

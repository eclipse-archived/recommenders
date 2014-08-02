/**
 * Copyright (c) 2011 Sebastian Proksch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.tests.apidocs;

import static org.eclipse.recommenders.rcp.JavaElementSelectionEvent.JavaElementSelectionLocation.TYPE_DECLARATION;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.JavaElementSelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class ProviderImplementation extends ApidocProvider {

    @SuppressWarnings("deprecation")
    @JavaSelectionSubscriber(TYPE_DECLARATION)
    public Status methodInSuperclass(IType type, JavaElementSelectionEvent selection, Composite parent) {
        return null;
    }
}

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
package org.eclipse.recommenders.tests.rcp.internal.providers.helper;

import static org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionLocation.TYPE_DECLARATION;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.rcp.events.JavaSelection;
import org.eclipse.recommenders.rcp.events.JavaSelection.JavaSelectionListener;

public class SpyImplementation extends JavaSelectionListenerSpy {

    @JavaSelectionListener(TYPE_DECLARATION)
    public void methodInSuperclass(final IType type, final JavaSelection selection) {
        recordEvent(selection);
    }
}
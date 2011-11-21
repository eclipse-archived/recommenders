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
package org.eclipse.recommenders.tests.extdoc.rcp.selection2.helper;

import static org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation.TYPE_DECLARATION;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelection.JavaSelectionListener;

public class SpyImplementation extends JavaSelectionListenerSpy {

    @JavaSelectionListener(TYPE_DECLARATION)
    public void methodInSuperclass(IType type, JavaSelection selection) {
        recordEvent(selection);
    }
}
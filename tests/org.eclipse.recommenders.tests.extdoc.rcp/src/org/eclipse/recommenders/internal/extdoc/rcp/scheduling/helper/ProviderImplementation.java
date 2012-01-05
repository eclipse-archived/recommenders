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
package org.eclipse.recommenders.internal.extdoc.rcp.scheduling.helper;

import static org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation.TYPE_DECLARATION;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class ProviderImplementation extends ExtdocProvider {

    @JavaSelectionSubscriber(TYPE_DECLARATION)
    public Status methodInSuperclass(IType type, JavaSelectionEvent selection, Composite parent) {
        return null;
    }
}
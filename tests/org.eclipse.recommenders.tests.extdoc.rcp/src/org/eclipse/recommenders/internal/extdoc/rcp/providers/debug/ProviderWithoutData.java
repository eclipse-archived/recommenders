/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers.debug;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class ProviderWithoutData extends ExtdocProvider {

    @JavaSelectionSubscriber
    public Status displayProposalsForType(final IJavaElement element, final JavaSelectionEvent selection,
            final Composite parent) throws InterruptedException {
        // No data available for selection...
        Thread.sleep(1000);
        return Status.NOT_AVAILABLE;
    }

}
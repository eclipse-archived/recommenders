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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public final class SlowProvider extends ExtdocProvider {

    @JavaSelectionSubscriber
    public void displayProposalsForType(final IJavaElement element, final JavaSelectionEvent selection,
            final Composite parent) throws InterruptedException {
        Thread.sleep(1000);
        runSyncInUiThread(new Runnable() {
            @Override
            public void run() {
                final Label l = new Label(parent, SWT.NONE);
                l.setText("Slooooow provider was here! :>");
            }
        });
    }
}
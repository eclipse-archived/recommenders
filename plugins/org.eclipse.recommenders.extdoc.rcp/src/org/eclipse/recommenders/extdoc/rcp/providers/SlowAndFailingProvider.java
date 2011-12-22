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
package org.eclipse.recommenders.extdoc.rcp.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.ProviderDescription;
import org.eclipse.recommenders.extdoc.rcp.scheduling.SubscriptionManager.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.recommenders.utils.Throws;
import org.eclipse.swt.widgets.Composite;

import com.google.inject.Inject;

public final class SlowAndFailingProvider extends Provider {

    private ProviderDescription description;

    @Inject
    public SlowAndFailingProvider(ExtdocIconLoader iconLoader) {
        description = new ProviderDescription("SlowAndFailingProvider", iconLoader.getImage("provider.subclassing.gif"));
    }

    @Override
    public ProviderDescription getDescription() {
        return description;
    }

    @JavaSelectionListener
    public void displayProposalsForType(IJavaElement element, JavaSelectionEvent selection, final Composite parent)
            throws InterruptedException {
        Thread.sleep(1500);
        Throws.throwNotImplemented();
    }
}
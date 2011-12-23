/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 *    Sebastian Proksch - integrated into new eventbus system
 */
package org.eclipse.recommenders.extdoc.rcp.providers.javadoc;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.ProviderDescription;
import org.eclipse.recommenders.extdoc.rcp.scheduling.SubscriptionManager.JavaSelectionListener;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class JavadocProvider extends Provider {

    private final ProviderDescription description;

    private final IWorkbenchWindow activeWorkbenchWindow;
    private JavadocViewPart javadoc;

    private final EventBus workspaceBus;

    @Inject
    public JavadocProvider(final ExtdocIconLoader iconLoader, final EventBus workspaceBus) {
        this.workspaceBus = workspaceBus;
        activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        description = new ProviderDescription("JavadocProvider", iconLoader.getImage("provider.javadoc.gif"));
    }

    @Override
    public ProviderDescription getDescription() {
        return description;
    }

    /*
     * NOTE: this provider is an example provider. There is actually no need to create dispatch methods for each of
     * these java elements separately. We just do this for demo purpose.
     */

    @JavaSelectionListener
    public void onPackageSelection(final IPackageFragment pkg, final JavaSelectionEvent selection,
            final Composite parent) {
        render(pkg, parent);
    }

    @JavaSelectionListener
    public void onCompilationUnitSelection(final ITypeRoot root, final JavaSelectionEvent selection,
            final Composite parent) {
        render(root, parent);
    }

    @JavaSelectionListener
    public void onTypeSelection(final IType type, final JavaSelectionEvent selection, final Composite parent) {
        render(type, parent);
    }

    @JavaSelectionListener
    public void onMethodSelection(final IMethod method, final JavaSelectionEvent selection, final Composite parent) {
        render(method, parent);
    }

    @JavaSelectionListener
    public void onFieldSelection(final IField field, final JavaSelectionEvent selection, final Composite parent) {
        render(field, parent);
    }

    private void render(final IJavaElement element, final Composite parent) {
        runSyncInUiThread(new Runnable() {
            @Override
            public void run() {
                javadoc = new JavadocViewPart(parent, activeWorkbenchWindow, element, workspaceBus);
                if (javadoc.getControl() instanceof Browser) {
                    new BrowserSizeWorkaround((Browser) javadoc.getControl());
                }
                javadoc.setInput(element);
            }
        });
        waitForBrowserSizeWorkaround();
    }

    private void waitForBrowserSizeWorkaround() {
        try {
            Thread.sleep(BrowserSizeWorkaround.MILLIS_UNTIL_RESCALE + 50);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }
}
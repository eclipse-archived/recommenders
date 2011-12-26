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
package org.eclipse.recommenders.internal.extdoc.rcp.providers.javadoc;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.recommenders.extdoc.rcp.providers.ExtdocProvider;
import org.eclipse.recommenders.extdoc.rcp.providers.JavaSelectionSubscriber;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class JavadocProvider extends ExtdocProvider {

    private final IWorkbenchWindow activeWorkbenchWindow;
    private JavadocViewPart javadoc;

    private final EventBus workspaceBus;

    @Inject
    public JavadocProvider(final EventBus workspaceBus) {
        this.workspaceBus = workspaceBus;
        activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }

    /*
     * NOTE: this provider is an example provider. There is actually no need to create dispatch methods for each of
     * these java elements separately. We just do this for demo purpose.
     */

    @JavaSelectionSubscriber
    public void onPackageSelection(final IPackageFragment pkg, final JavaSelectionEvent selection,
            final Composite parent) {
        render(pkg, parent);
    }

    @JavaSelectionSubscriber
    public void onCompilationUnitSelection(final ITypeRoot root, final JavaSelectionEvent selection,
            final Composite parent) {
        render(root, parent);
    }

    @JavaSelectionSubscriber
    public void onTypeSelection(final IType type, final JavaSelectionEvent selection, final Composite parent) {
        render(type, parent);
    }

    @JavaSelectionSubscriber
    public void onMethodSelection(final IMethod method, final JavaSelectionEvent selection, final Composite parent) {
        render(method, parent);
    }

    @JavaSelectionSubscriber
    public void onFieldSelection(final IField field, final JavaSelectionEvent selection, final Composite parent) {
        render(field, parent);
    }

    private void render(final IJavaElement element, final Composite parent) {
        runSyncInUiThread(new Runnable() {
            @Override
            public void run() {
                javadoc = new JavadocViewPart(parent, activeWorkbenchWindow, element, workspaceBus);

                // TODO REVIEW MB: since javadoc view part is ours, shouldn't we move that into JavadocViewpart???
                final Control control = javadoc.getControl();
                if (control instanceof Browser) {
                    // on mac or win:
                    new BrowserSizeWorkaround((Browser) control);
                } else if (control instanceof StyledText) {
                    // on linux:
                    initializeStyledText((StyledText) control);
                }
                javadoc.setInput(element);
            }

            private void initializeStyledText(final StyledText styledText) {
                // here some layout magic happens only Stefan knows about. Won't touch until needed.
                final GridData gridData = GridDataFactory.fillDefaults().grab(true, false)
                        .hint(SWT.DEFAULT, BrowserSizeWorkaround.MINIMUM_HEIGHT)
                        .minSize(SWT.DEFAULT, BrowserSizeWorkaround.MINIMUM_HEIGHT).create();
                styledText.setLayoutData(gridData);
                styledText.addModifyListener(new ModifyListener() {

                    @Override
                    public void modifyText(final ModifyEvent e) {
                        final int height = styledText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
                        if (gridData.heightHint != height) {
                            gridData.heightHint = height;
                            gridData.minimumHeight = height;
                            // styledText.setAlwaysShowScrollBars(false);
                            BrowserSizeWorkaround.layoutParents(styledText);
                        }
                    }
                });
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
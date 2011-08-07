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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.internal.rcp.extdoc.UiManager;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

abstract class AbstractExtDocInformationControl extends AbstractInformationControl implements
        IInformationControlExtension2 {

    private ProvidersComposite composite;
    private IJavaElementSelection lastSelection;
    private final Map<Composite, IAction> actions = new HashMap<Composite, IAction>();

    private final UiManager uiManager;
    private final ProviderStore providerStore;
    private final IInformationControlCreator creator;

    AbstractExtDocInformationControl(final Shell parentShell, final UiManager uiManager,
            final ProviderStore providerStore, final IInformationControlCreator creator) {
        super(parentShell, new ToolBarManager(SWT.FLAT));
        this.uiManager = uiManager;
        this.providerStore = providerStore;
        this.creator = creator;
        create();
    }

    @Override
    public final boolean hasContents() {
        return true;
    }

    @Override
    protected final void createContent(final Composite parent) {
        composite = new ProvidersComposite(parent, false);
        final ToolBarManager toolbar = getToolBarManager();
        for (final IProvider provider : providerStore.getProviders()) {
            final Composite providerComposite = composite.addProvider(provider, uiManager.getViewSite());
            final IAction action = new AbstractAction("Scroll to " + provider.getProviderFullName(), provider.getIcon()) {
                @Override
                public void run() {
                    composite.scrollToProvider(providerComposite);
                }
            };
            toolbar.add(action);
            actions.put(providerComposite, action);
        }
        toolbar.add(new AbstractAction("Show in ExtDoc View", ExtDocPlugin.getIcon("eview16/extdoc.png")) {
            @Override
            public void run() {
                uiManager.selectionChanged(getLastSelection());
            }
        });
        toolbar.update(true);
    }

    @Override
    public final void setInput(final Object input) {
        lastSelection = getSelection(input);

        for (final Composite control : composite.getProviders()) {
            final IProvider provider = (IProvider) control.getData();
            ((GridData) control.getLayoutData()).exclude = true;
            actions.get(control).setEnabled(false);
            new Job("Updating Hover Provider") {
                @Override
                public IStatus run(final IProgressMonitor monitor) {
                    if (lastSelection != null && provider.selectionChanged(lastSelection, control)) {
                        new ProviderUiJob() {
                            @Override
                            public Composite run() {
                                if (!control.isDisposed()) {
                                    ((GridData) control.getLayoutData()).exclude = false;
                                    actions.get(control).setEnabled(true);
                                }
                                return control;
                            }
                        }.schedule();
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }

    @Override
    public IInformationControlCreator getInformationPresenterControlCreator() {
        return creator;
    }

    protected abstract IJavaElementSelection getSelection(Object object);

    private IJavaElementSelection getLastSelection() {
        return lastSelection;
    }

    private abstract class AbstractAction extends Action {

        public AbstractAction(final String text, final Image icon) {
            super(text, ImageDescriptor.createFromImage(icon));
        }

    }

}

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
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.hover.AbstractJavaEditorTextHover;
import org.eclipse.jdt.internal.ui.text.java.hover.ProblemHover;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementSelectionResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ExtDocHover extends AbstractJavaEditorTextHover {

    private final ExtDocView view;
    private final ProviderStore providerStore;

    // TODO: Currently this seems to be the only way to avoid problem hovers to
    // be overriden by the ExtDoc hover.
    private final ProblemHover problemHover = new ProblemHover();
    private boolean isProblemHoverActive;

    private final IInformationControlCreator creator = new IInformationControlCreator() {
        @Override
        public IInformationControl createInformationControl(final Shell parent) {
            return new InformationControl(parent);
        }
    };

    @Inject
    ExtDocHover(final ExtDocView view, final ProviderStore providerStore) {
        this.view = view;
        this.providerStore = providerStore;
    }

    @Override
    public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
        throw new IllegalAccessError("JDT is expected to call getHoverInfo2");
    }

    @Override
    public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
        final Object problemInfo = problemHover.getHoverInfo2(textViewer, hoverRegion);
        isProblemHoverActive = problemInfo != null;
        return isProblemHoverActive ? problemInfo : JavaElementSelectionResolver.resolveFromEditor(
                (JavaEditor) getEditor(), hoverRegion.getOffset());
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return isProblemHoverActive ? problemHover.getHoverControlCreator() : creator;
    }

    private final class InformationControl extends AbstractInformationControl implements IInformationControlExtension2 {

        private ProvidersComposite composite;
        private IJavaElementSelection lastSelection;
        private final Map<Composite, IAction> actions = new HashMap<Composite, IAction>();

        public InformationControl(final Shell parentShell) {
            super(parentShell, new ToolBarManager(SWT.FLAT));
            create();
        }

        @Override
        public boolean hasContents() {
            return true;
        }

        @Override
        protected void createContent(final Composite parent) {
            composite = new ProvidersComposite(parent, false);
            final ToolBarManager toolbar = getToolBarManager();
            for (final IProvider provider : providerStore.getProviders()) {
                final Composite providerComposite = composite.addProvider(provider, view.getViewSite());
                final IAction action = new AbstractAction("Scroll to " + provider.getProviderFullName(),
                        provider.getIcon()) {
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
                    view.selectionChanged(getLastSelection());
                }
            });
            toolbar.update(true);
        }

        @Override
        public void setInput(final Object input) {
            final IJavaElementSelection selection = (IJavaElementSelection) input;
            lastSelection = selection;
            updateProviders(selection);
        }

        private void updateProviders(final IJavaElementSelection selection) {
            for (final Composite control : composite.getProviders()) {
                final IProvider provider = (IProvider) control.getData();
                ((GridData) control.getLayoutData()).exclude = true;
                actions.get(control).setEnabled(false);
                new Job("Updating Hover Provider") {
                    @Override
                    public IStatus run(final IProgressMonitor monitor) {
                        if (provider.selectionChanged(selection)) {
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

        private IJavaElementSelection getLastSelection() {
            return lastSelection;
        }
    }

    private abstract class AbstractAction extends Action {

        public AbstractAction(final String text, final Image icon) {
            super(text, ImageDescriptor.createFromImage(icon));
        }

    }

}

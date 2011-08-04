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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementSelectionResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.ProviderUiJob;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ExtDocHover implements IJavaEditorTextHover, ITextHoverExtension, ITextHoverExtension2 {

    private final IViewSite viewSite;
    private final ProviderStore providerStore;

    private IEditorPart editor;

    private final IInformationControlCreator creator = new IInformationControlCreator() {
        @Override
        public IInformationControl createInformationControl(final Shell parent) {
            return new InformationControl(parent, null, null);
        }
    };

    @Inject
    ExtDocHover(final ExtDocView view, final ProviderStore providerStore) {
        viewSite = view.getViewSite();
        this.providerStore = providerStore;
    }

    @Override
    public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
        throw new IllegalAccessError("JDT is expected to call getHoverInfo2");
    }

    @Override
    public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
        return JavaElementSelectionResolver.resolveFromEditor((JavaEditor) editor, hoverRegion.getOffset());
    }

    @Override
    public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
        return JavaWordFinder.findWord(textViewer.getDocument(), offset);
    }

    @Override
    public void setEditor(final IEditorPart editor) {
        this.editor = editor;
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return creator;
    }

    private final class InformationControl extends AbstractInformationControl implements IInformationControlExtension2 {

        private ProvidersComposite composite;
        private IJavaElementSelection lastSelection;

        public InformationControl(final Shell parentShell, final ProvidersComposite composite,
                final IJavaElementSelection lastSelection) {
            super(parentShell, new ToolBarManager());
            // this.composite = composite;
            // this.lastSelection = lastSelection;
            create();
        }

        @Override
        public boolean hasContents() {
            return true;
        }

        @Override
        protected void createContent(final Composite parent) {
            if (composite == null) {
                composite = new ProvidersComposite(parent, false);
                for (final IProvider provider : providerStore.getProviders()) {
                    composite.addProvider(provider, viewSite);
                }
            } else {
                composite.setParent(parent);
            }
        }

        @Override
        public void setInput(final Object input) {
            final IJavaElementSelection selection = (IJavaElementSelection) input;
            if (!selection.equals(lastSelection)) {
                updateProviders(selection);
                lastSelection = selection;
            }
        }

        private void updateProviders(final IJavaElementSelection selection) {
            for (final Composite control : composite.getProviders()) {
                final IProvider provider = (IProvider) control.getData();
                ((GridData) control.getLayoutData()).exclude = true;
                new Job("Updating Hover Provider") {
                    @Override
                    public IStatus run(final IProgressMonitor monitor) {
                        if (provider.selectionChanged(selection)) {
                            new ProviderUiJob() {
                                @Override
                                public Composite run() {
                                    ((GridData) control.getLayoutData()).exclude = false;
                                    control.getParent().layout(true);
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
            return new IInformationControlCreator() {
                @Override
                public IInformationControl createInformationControl(final Shell parent) {
                    return new InformationControl(parent, composite, lastSelection);
                }
            };
        }
    }

}

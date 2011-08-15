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

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.hover.AbstractJavaEditorTextHover;
import org.eclipse.jdt.internal.ui.text.java.hover.ProblemHover;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementSelectionResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.internal.rcp.extdoc.UiManager;
import org.eclipse.swt.widgets.Shell;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ExtDocHover extends AbstractJavaEditorTextHover {

    // TODO: Currently this seems to be the only way to avoid problem hovers to
    // be overriden by the ExtDoc hover.
    private final ProblemHover problemHover = new ProblemHover();
    private boolean isProblemHoverActive;

    private final IInformationControlCreator creator;

    @Inject
    ExtDocHover(final UiManager uiManager, final ProviderStore providerStore) {
        creator = new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new InformationControl(parent, uiManager, providerStore, null);
            }
        };
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

    private static final class InformationControl extends AbstractExtDocInformationControl {

        public InformationControl(final Shell parentShell, final UiManager uiManager,
                final ProviderStore providerStore, final ProvidersComposite composite) {
            super(parentShell, uiManager, providerStore, composite);
        }

        @Override
        protected IJavaElementSelection getSelection(final Object input) {
            return (IJavaElementSelection) input;
        }

        @Override
        public IInformationControlCreator getInformationPresenterControlCreator() {
            return new IInformationControlCreator() {
                @Override
                public IInformationControl createInformationControl(final Shell parent) {
                    return new InformationControl(parent, getUiManager(), getProviderStore(), null);
                }
            };
        }
    }

}

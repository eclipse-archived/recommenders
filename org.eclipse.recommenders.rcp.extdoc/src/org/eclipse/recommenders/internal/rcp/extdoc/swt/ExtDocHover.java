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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.commons.selection.SelectionResolver;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

public final class ExtDocHover implements IJavaEditorTextHover, ITextHoverExtension, ITextHoverExtension2 {

    private IEditorPart editor;

    @Override
    public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
        throw new IllegalAccessError("JDT is expected to call getHoverInfo2");
    }

    @Override
    public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
        final IJavaElement element = SelectionResolver.resolveJavaElement(editor.getEditorInput(),
                hoverRegion.getOffset());
        return element;
    }

    @Override
    public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
        throw new IllegalAccessError("No occasion of calls to this method is known.");
    }

    @Override
    public void setEditor(final IEditorPart editor) {
        this.editor = editor;
    }

    @Override
    public IInformationControlCreator getHoverControlCreator() {
        return new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new InformationControl(parent);
            }
        };
    }

    private static final class InformationControl extends AbstractInformationControl implements
            IInformationControlExtension2 {

        private Label label;

        public InformationControl(final Shell parentShell) {
            super(parentShell, true);
            create();
        }

        @Override
        public boolean hasContents() {
            return true;
        }

        @Override
        protected void createContent(final Composite parent) {
            label = new Label(parent, SWT.NONE);
        }

        @Override
        public void setInput(final Object input) {
            label.setText(input.toString());
        }

    }

}

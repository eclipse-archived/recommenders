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
package org.eclipse.recommenders.rcp.extdoc;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class AbstractProviderComposite extends AbstractProvider {

    private IJavaElementSelection lastSelection;
    private Composite composite;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public final Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        composite = new Composite(parent, SWT.None);
        final GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 0;
        layout.marginHeight = 10;
        layout.marginWidth = 8;
        layout.verticalSpacing = 8;
        composite.setLayout(layout);

        final CLabel label = new CLabel(composite, SWT.NONE);
        label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        label.setImage(getIcon());
        label.setText(getProviderFullName());

        createContentControl(composite);

        return composite;
    }

    protected abstract Control createContentControl(Composite parent);

    @Override
    public final void selectionChanged(final IJavaElementSelection selection) {
        lastSelection = selection;
        updateContent(selection);
    }

    protected abstract void updateContent(IJavaElementSelection selection);

    @Override
    public final void redraw() {
        selectionChanged(lastSelection);
    }

    @Override
    public final Shell getShell() {
        return composite.getShell();
    }

}

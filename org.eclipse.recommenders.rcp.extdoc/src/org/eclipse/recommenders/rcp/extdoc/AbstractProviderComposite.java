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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class AbstractProviderComposite extends AbstractProvider {

    private Composite composite;
    private IWorkbenchPartSite partSite;
    private CLabel titleLabel;

    @Override
    public final Control createControl(final Composite parent, final IWorkbenchPartSite partSite) {
        this.partSite = partSite;
        composite = SwtFactory.createGridComposite(parent, 1, 0, 4, 8, 10);

        titleLabel = new CLabel(composite, SWT.NONE);
        titleLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        titleLabel.setImage(getIcon());
        titleLabel.setText(getProviderFullName());
        SwtFactory.createSeparator(composite);

        createContentControl(composite);

        return composite;
    }

    protected abstract Control createContentControl(Composite parent);

    @Override
    public final Shell getShell() {
        return composite.getShell();
    }

    public final IWorkbenchPartSite getSite() {
        return partSite;
    }

    protected final void disposeChildren(final Composite composite) {
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
    }

    public final void setTitle(final String title) {
        titleLabel.setText(title);
        titleLabel.getParent().layout();
    }

    public final void setTitleIcon(final Image image) {
        titleLabel.setImage(image);
        titleLabel.getParent().layout();
    }

}

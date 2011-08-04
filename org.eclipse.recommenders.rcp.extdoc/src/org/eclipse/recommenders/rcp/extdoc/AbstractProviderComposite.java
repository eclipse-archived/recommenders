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
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.rcp.extdoc.AbstractProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;

public abstract class AbstractProviderComposite extends AbstractProvider {

    private IViewSite viewSite;
    private Composite provider;

    @Override
    public final Composite createComposite(final Composite parent, final IViewSite site) {
        viewSite = site;

        final Composite container = SwtFactory.createGridComposite(parent, 1, 0, 3, 8, 8);
        createProviderTitle(container);
        SwtFactory.createSeparator(container);
        provider = createContentComposite(container);
        Checks.ensureIsNotNull(provider);
        return container;
    }

    private void createProviderTitle(final Composite container) {
        final CLabel titleLabel = new CLabel(container, SWT.NONE);
        titleLabel.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
        titleLabel.setImage(getIcon());
        titleLabel.setText(getProviderFullName());
        titleLabel.setLeftMargin(0);
    }

    protected abstract Composite createContentComposite(Composite parent);

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return location != JavaElementLocation.PACKAGE_DECLARATION;
    }

    public final IViewSite getViewSite() {
        return viewSite;
    }

    protected final void disposeChildren(final Composite composite) {
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
    }

}

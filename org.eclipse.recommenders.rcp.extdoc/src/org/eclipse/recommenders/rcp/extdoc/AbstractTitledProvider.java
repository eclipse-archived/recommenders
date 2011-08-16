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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.internal.rcp.extdoc.AbstractProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class AbstractTitledProvider extends AbstractProvider {

    private IWorkbenchWindow window;

    @Override
    public final Composite createComposite(final Composite parent, final IWorkbenchWindow workbenchWindow) {
        window = workbenchWindow;

        final ProviderComposite container = new ProviderComposite(parent);
        createProviderTitle(container);
        SwtFactory.createSeparator(container);
        container.contentComposite = Checks.ensureIsNotNull(createContentComposite(container));
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

    @Override
    public final boolean selectionChanged(final IJavaElementSelection selection, final Composite composite) {
        final ProviderUiJob job = updateSelection(selection);
        if (job == null) {
            return false;
        }
        ProviderUiJob.run(job, ((ProviderComposite) composite).contentComposite);
        return true;
    }

    protected abstract ProviderUiJob updateSelection(IJavaElementSelection selection);

    public final IWorkbenchWindow getWorkbenchWindow() {
        return window;
    }

    public static final void disposeChildren(final Composite composite) {
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
    }

    private static final class ProviderComposite extends Composite {

        private Composite contentComposite;

        public ProviderComposite(final Composite parent) {
            super(parent, SWT.NONE);
            setLayout(GridLayoutFactory.swtDefaults().numColumns(1).margins(8, 8).spacing(0, 3).create());
            setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        }

    }

}

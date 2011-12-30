/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.wizard;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.recommenders.internal.udc.ui.packageselection.PackageSelectionController;
import org.eclipse.recommenders.internal.udc.ui.packageselection.RegExpTableController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class PackageSelectionPage extends WizardPageTemplate {
    PackageSelectionController packageSelectionController;
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getNewValue() == null) {
                setErrorMessage(null);
            } else {
                setErrorMessage("Page contains invalid expressions");
            }
        }

    };

    public PackageSelectionPage() {
        this.setMessage("Enter the packages you want to include or exclude from export.");
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite c = new Composite(parent, SWT.NONE);

        setControl(c);
        c.setLayout(new FillLayout());

        packageSelectionController = new PackageSelectionController();
        packageSelectionController.createControls(c);
        packageSelectionController.addPropertyChangeListener(RegExpTableController.validationState,
                propertyChangeListener);
    }

    @Override
    public String getPageTitle() {
        return "Package Selection";
    }

    public void setProjects(final IProject[] Projects) {
        packageSelectionController.setProjects(Projects);
    }

    public String[] getIncludeExpressions() {
        return packageSelectionController.getIncludeExpressions();
    }

    public String[] getExcludeExpressions() {
        return packageSelectionController.getExcludeExpressions();
    }
}

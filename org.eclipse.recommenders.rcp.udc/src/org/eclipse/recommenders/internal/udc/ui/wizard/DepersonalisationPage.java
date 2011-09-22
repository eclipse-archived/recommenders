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

import org.eclipse.recommenders.internal.udc.depersonalizer.ICompilationUnitDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.IDepersonalisationProvider;
import org.eclipse.recommenders.internal.udc.ui.depersonalisation.DepersonalisationController;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DepersonalisationPage extends WizardPageTemplate implements PropertyChangeListener,
        IDepersonalisationProvider {

    public boolean isDepersonalisationRequired() {
        return controller.isDepersonalisationRequired();
    }

    public DepersonalisationPage() {
    }

    private DepersonalisationController controller;

    @Override
    public void createControl(final Composite parent) {
        controller = new DepersonalisationController();
        final Control control = controller.createControl(parent);
        setControl(control);
        setMessage("Select Depersonalisation Options");
    }

    @Override
    public ICompilationUnitDepersonalizer[] getDepersonalizers() {
        if (controller == null) {
            return new ICompilationUnitDepersonalizer[0];
        }
        return controller.getDepersonalizers();
    }

    @Override
    public String getPageTitle() {
        return "Depersonalisation";
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DepersonalisationController.errorMsgProperty)) {
            setErrorMessage(controller.getErrorMessage());
        }
    }

}

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

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.udc.FingerprintProvider;
import org.eclipse.recommenders.internal.udc.depersonalizer.ICompilationUnitDepersonalizer;
import org.eclipse.recommenders.internal.udc.depersonalizer.IDepersonalisationProvider;
import org.eclipse.recommenders.internal.udc.ui.depersonalisation.ControllerFactory;
import org.eclipse.recommenders.internal.udc.ui.depersonalisation.DependencySelectionController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class LibrariesPage extends WizardPage implements IDepersonalisationProvider {
    DependencySelectionController controller;

    public LibrariesPage() {
        super("");
    }

    @Override
    public void createControl(final Composite parent) {
        controller = ControllerFactory.instance().createDependencySelectionController();
        final Composite composite = controller.createControls(parent);
        setControl(composite);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.setTitle("Library Selection");
        this.setMessage("We would like to know how you use classes within the listed libraries.\nSelect the libraries you want to share the usage data for.");
    }

    public void setProjects(final IProject[] projects) {
        controller.setProjects(projects);
        controller.selectDependenciesFromPreferences();
    }

    public String[] getDependencies() {
        return controller.getDependencies();
    }

    @Override
    public ICompilationUnitDepersonalizer[] getDepersonalizers() {
        final FingerprintProvider provider = FingerprintProvider.createInstance();
        final Set<String> fingerPrints = provider.getFingerprints(getDependencies());
        return new ICompilationUnitDepersonalizer[] {};
    }

}

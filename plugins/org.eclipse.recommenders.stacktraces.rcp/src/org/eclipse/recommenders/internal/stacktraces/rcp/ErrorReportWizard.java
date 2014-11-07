/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Daniel Haftstein - added support for multiple stacktraces
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;

public class ErrorReportWizard extends Wizard {

    public static ImageDescriptor TITLE_IMAGE_DESC = ImageDescriptor.createFromFile(ErrorReportWizard.class,
            "/icons/wizban/stackframes_wiz.gif"); //$NON-NLS-1$

    private Settings settings;
    private SettingsWizardPage page1;
    private DetailsWizardPage page2;

    public ErrorReportWizard(Settings settings, IObservableList errors) {
        this.settings = settings;
        page1 = new SettingsWizardPage(settings);
        page2 = new DetailsWizardPage(errors, settings);
        setHelpAvailable(true);
    }

    @Override
    public void addPages() {
        setWindowTitle(Messages.ERRORREPORTWIZARD_WE_NOTICED_ERROR);
        setDefaultPageImageDescriptor(TITLE_IMAGE_DESC);
        addPage(page1);
        addPage(page2);
    }

    @Override
    public boolean performFinish() {
        PreferenceInitializer.saveSettings(settings);
        return true;
    }

}

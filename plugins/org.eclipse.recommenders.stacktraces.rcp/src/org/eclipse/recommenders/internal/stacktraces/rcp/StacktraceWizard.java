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
import org.eclipse.recommenders.internal.stacktraces.rcp.StacktracesRcpPreferences.Mode;

public class StacktraceWizard extends Wizard {

    static class WizardPreferences {
        // wizard should not override preferences if canceled.
        // For this reason the wizard stores its current settings
        // and updates the global preferences if finished.

        String name;
        String email;
        Mode mode;
        boolean anonymize;
        boolean clearMessages;

        public WizardPreferences(StacktracesRcpPreferences stacktracesPreferences) {
            this.name = stacktracesPreferences.getName();
            this.email = stacktracesPreferences.getEmail();
            this.mode = stacktracesPreferences.getMode();
            this.anonymize = stacktracesPreferences.shouldAnonymizeStackTraceElements();
            this.clearMessages = stacktracesPreferences.shouldClearMessages();
        }

    }

    private StacktracesRcpPreferences stacktracesPreferences;
    private WizardPreferences wizardPreferences;
    private StacktraceSettingsPage page1;
    private JsonPreviewPage page2;

    public StacktraceWizard(StacktracesRcpPreferences stacktracesPreferences, IObservableList errors) {
        this.stacktracesPreferences = stacktracesPreferences;
        this.wizardPreferences = new WizardPreferences(stacktracesPreferences);
        page1 = new StacktraceSettingsPage(wizardPreferences);
        page2 = new JsonPreviewPage(errors, stacktracesPreferences, wizardPreferences);
        setHelpAvailable(true);
    }

    @Override
    public void addPages() {
        setWindowTitle("We noticed an error...");
        ImageDescriptor img = ImageDescriptor.createFromFile(getClass(), "/icons/wizban/stackframes_wiz.gif");
        setDefaultPageImageDescriptor(img);
        addPage(page1);
        addPage(page2);
    }

    @Override
    public boolean performFinish() {
        updatePreferences();
        return true;
    }

    private void updatePreferences() {
        stacktracesPreferences.setAnonymizeStackframes(wizardPreferences.anonymize);
        stacktracesPreferences.setClearMessages(wizardPreferences.clearMessages);
        stacktracesPreferences.setEmail(wizardPreferences.email);
        stacktracesPreferences.setMode(wizardPreferences.mode);
        stacktracesPreferences.setName(wizardPreferences.name);
    }
}

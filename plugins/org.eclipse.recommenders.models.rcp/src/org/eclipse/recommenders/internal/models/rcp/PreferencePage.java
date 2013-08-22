/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Gottschaemmer, Olav Lenz - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.recommenders.internal.models.rcp.Constants.*;
import static org.eclipse.recommenders.internal.models.rcp.Messages.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public PreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
    }

    @Override
    protected void createFieldEditors() {
        createRemoteRepositorySection();
    }

    private void createRemoteRepositorySection() {
        GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        layoutData.horizontalSpan = 3;

        Label headline = new Label(getFieldEditorParent(), SWT.LEFT);
        headline.setText(PREFPAGE_MODEL_REPOSITORY_HEADLINE);
        headline.setLayoutData(layoutData);
        headline.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));

        Label description = new Label(getFieldEditorParent(), SWT.LEFT);
        description.setText(PREFPAGE_MODEL_REPOSITORY_INTRO);
        description.setLayoutData(layoutData);

        StringButtonFieldEditor modelRepoFieldEditor = new StringButtonFieldEditor(P_REPOSITORY_URL, PREFPAGE_URI,
                getFieldEditorParent()) {
            @Override
            protected String changePressed() {
                InputDialog inputDialog = new InputDialog(getShell(), PREFPAGE_URI_MODEL_REPOSITORY,
                        PREFPAGE_URI_INSERT, oldValue, new IInputValidator() {

                            @Override
                            public String isValid(String newText) {
                                if (isValidRepoURI(newText)) {
                                    return null;
                                } else {
                                    return PREFPAGE_URI_INVALID;
                                }
                            }
                        });
                if (inputDialog.open() == Window.OK) {
                    return inputDialog.getValue();
                }
                return oldValue;
            }
        };
        modelRepoFieldEditor.getTextControl(getFieldEditorParent()).setEnabled(false);
        addField(modelRepoFieldEditor);

        addField(new BooleanFieldEditor(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD, PREFPAGE_ENABLE_AUTO_DOWNLOAD,
                getFieldEditorParent()));
    }

    private boolean isValidRepoURI(String uri) {
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }
}

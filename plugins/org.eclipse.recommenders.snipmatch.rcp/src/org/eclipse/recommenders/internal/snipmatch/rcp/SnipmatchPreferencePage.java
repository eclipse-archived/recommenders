/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.P_SNIPPETS_REPO;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class SnipmatchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private StringButtonFieldEditor snippetsRepoField;

    public SnipmatchPreferencePage() {
        super(GRID);
        setDescription("Set Snipmatch preferences.");
    }

    @Override
    public void createFieldEditors() {
        snippetsRepoField = new StringButtonFieldEditor(P_SNIPPETS_REPO, "&Remote Snippets Repository:",
                getFieldEditorParent()) {

            @Override
            protected String changePressed() {
                String url = getPreferenceStore().getDefaultString(P_SNIPPETS_REPO);

                InputDialog d = new InputDialog(getShell(), "New Snippet Repository", "Enter snippet repository URL:",
                        url, new UriInputValidator());
                if (d.open() == Window.OK) {
                    return d.getValue();
                }
                return null;
            }

        };
        addField(snippetsRepoField);
    }

    @Override
    public void init(IWorkbench workbench) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID);
        setPreferenceStore(store);
    }

    private final class UriInputValidator implements IInputValidator {
        @Override
        public String isValid(String newText) {
            // TODO this does not support git:// urls
            try {
                new URI(newText);
                return null;
            } catch (URISyntaxException e) {
                return e.getMessage();
            }
        }
    }
}

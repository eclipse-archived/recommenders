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

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.PREF_SNIPPETS_REPO;

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
        setDescription(Messages.PREFPAGE_DESCRIPTION);
    }

    @Override
    public void createFieldEditors() {
        snippetsRepoField = new StringButtonFieldEditor(PREF_SNIPPETS_REPO, Messages.PREFPAGE_LABEL_REMOTE_SNIPPETS_REPOSITORY,
                getFieldEditorParent()) {

            @Override
            protected String changePressed() {
                String url = getPreferenceStore().getDefaultString(PREF_SNIPPETS_REPO);

                InputDialog d = new InputDialog(getShell(), Messages.DIALOG_TITLE_NEW_SNIPPET_REPOSITORY, Messages.DIALOG_MESSAGE_NEW_SNIPPET_REPOSITORY,
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

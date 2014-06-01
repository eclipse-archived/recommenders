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
import static org.eclipse.recommenders.utils.Checks.cast;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class SnipmatchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private StringButtonFieldEditor snippetsRepoField;
    private ImmutableSet<ISnippetRepositoryProvider> providers;

    @Inject
    public SnipmatchPreferencePage(
            @Named(SnipmatchRcpModule.SNIPPET_REPOSITORY_PROVIDERS) ImmutableSet<ISnippetRepositoryProvider> providers) {
        super(GRID);
        this.providers = providers;
        setDescription(Messages.PREFPAGE_DESCRIPTION);
    }

    @Override
    public void createFieldEditors() {
        snippetsRepoField = new StringButtonFieldEditor(PREF_SNIPPETS_REPO,
                Messages.PREFPAGE_LABEL_REMOTE_SNIPPETS_REPOSITORY, getFieldEditorParent()) {

            @Override
            protected String changePressed() {
                EclipseGitSnippetRepositoryConfiguration value = loadDefaultValue();
                String url = ""; //$NON-NLS-1$
                if (value != null) {
                    url = value.getRepositoryUrl();
                }

                InputDialog d = new InputDialog(getShell(), Messages.DIALOG_TITLE_NEW_SNIPPET_REPOSITORY,
                        Messages.DIALOG_MESSAGE_NEW_SNIPPET_REPOSITORY, url, new UriInputValidator());
                if (d.open() == Window.OK) {
                    return d.getValue();
                }
                return null;
            }

            private EclipseGitSnippetRepositoryConfiguration loadDefaultValue() {
                String configurations = getPreferenceStore().getDefaultString(PREF_SNIPPETS_REPO);
                return loadValue(configurations);
            }

            private EclipseGitSnippetRepositoryConfiguration loadValue(String string) {
                List<ISnippetRepositoryConfiguration> configurations = RepositoryConfigurations.fromPreferenceString(
                        string, providers);
                if (configurations.size() > 0
                        && configurations.get(0) instanceof EclipseGitSnippetRepositoryConfiguration) {
                    EclipseGitSnippetRepositoryConfiguration config = cast(configurations.get(0));
                    return config;
                }
                return null;
            }

            @Override
            public void load() {
                String configurations = getPreferenceStore().getString(PREF_SNIPPETS_REPO);
                EclipseGitSnippetRepositoryConfiguration value = loadValue(configurations);
                if (value != null) {
                    getTextControl().setText(value.getRepositoryUrl());
                }
            }

            @Override
            public void loadDefault() {
                EclipseGitSnippetRepositoryConfiguration value = loadDefaultValue();
                if (value != null) {
                    getTextControl().setText(value.getRepositoryUrl());
                }
                store();
            }

            @Override
            public void store() {
                ISnippetRepositoryConfiguration config = new EclipseGitSnippetRepositoryConfiguration(
                        "", getTextControl() //$NON-NLS-1$
                                .getText(), true);
                getPreferenceStore().setValue(PREF_SNIPPETS_REPO,
                        RepositoryConfigurations.toPreferenceString(config, providers));
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

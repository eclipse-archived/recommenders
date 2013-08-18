/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    private static final String URL_SEPARATOR = "\t";
    private ModelRepositoryListEditor repoEditor;

    public PreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
        setMessage(PREFPAGE_MODEL_REPOSITORY_HEADLINE);
        setDescription(PREFPAGE_MODEL_REPOSITORY_INTRO);
    }

    @Override
    protected void createFieldEditors() {

        repoEditor = new ModelRepositoryListEditor(P_REPOSITORY_URL_LIST, PREFPAGE_URI, getFieldEditorParent());
        addField(repoEditor);
        addField(new BooleanFieldEditor(P_REPOSITORY_ENABLE_AUTO_DOWNLOAD, PREFPAGE_ENABLE_AUTO_DOWNLOAD,
                getFieldEditorParent()));
    }

    @Override
    protected void performApply() {
        super.performApply();
        IPreferenceStore store = getPreferenceStore();
        String[] split = split(store.getString(P_REPOSITORY_URL_LIST));
        if (split.length == 0) {
            // if the user removed all entries, add the default entry back
            repoEditor.loadDefault();
            store.setValue(P_REPOSITORY_URL_LIST, store.getDefaultString(P_REPOSITORY_URL_LIST));
            store.setValue(P_REPOSITORY_URL, store.getDefaultString(P_REPOSITORY_URL));
        } else {
            // set the active repository to the top most element
            store.setValue(P_REPOSITORY_URL, split[0]);
        }
    }

    private final class ModelRepositoryListEditor extends ListEditor {

        private ModelRepositoryListEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        @Override
        protected String[] parseString(String stringList) {
            return split(stringList);
        }

        @Override
        protected String getNewInputObject() {
            InputDialog inputDialog = new InputDialog(getFieldEditorParent().getShell(), PREFPAGE_URI_MODEL_REPOSITORY,
                    PREFPAGE_URI_INSERT, "http://download.eclipse.org/recommenders/models/<version>",
                    new IInputValidator() {

                        @Override
                        public String isValid(String newText) {
                            if (isValidRepoURI(newText)) {
                                return null;
                            } else {
                                return PREFPAGE_URI_INVALID;
                            }
                        }

                        private boolean isValidRepoURI(String uri) {
                            try {
                                new URI(uri);
                            } catch (URISyntaxException e) {
                                return false;
                            }
                            return true;
                        }

                    });
            if (inputDialog.open() == Window.OK) {
                return inputDialog.getValue();
            }
            return null;
        }

        @Override
        protected String createList(String[] items) {
            return join(items);
        }

    }

    private static String[] split(String stringList) {
        Iterable<String> split = Splitter.on(URL_SEPARATOR).omitEmptyStrings().split(stringList);
        return Iterables.toArray(split, String.class);
    }

    private static String join(String[] items) {
        return Joiner.on(URL_SEPARATOR).join(items);
    }
}

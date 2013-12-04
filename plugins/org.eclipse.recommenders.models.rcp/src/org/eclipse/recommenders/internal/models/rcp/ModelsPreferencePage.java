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

import static org.eclipse.recommenders.internal.models.rcp.Constants.P_REPOSITORY_ENABLE_AUTO_DOWNLOAD;
import static org.eclipse.recommenders.internal.models.rcp.Constants.P_REPOSITORY_URL_LIST;
import static org.eclipse.recommenders.internal.models.rcp.Messages.PREFPAGE_ENABLE_AUTO_DOWNLOAD;
import static org.eclipse.recommenders.internal.models.rcp.Messages.PREFPAGE_MODEL_REPOSITORY_HEADLINE;
import static org.eclipse.recommenders.internal.models.rcp.Messages.PREFPAGE_MODEL_REPOSITORY_INTRO;
import static org.eclipse.recommenders.internal.models.rcp.Messages.PREFPAGE_URI;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.base.Joiner;

public class ModelsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private ModelRepositoryListEditor repoEditor;

    public ModelsPreferencePage() {
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
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        String repositoryURLList = Joiner.on(ModelsRcpPreferences.URL_SEPARATOR).join(repoEditor.getItems());
        store.setValue(P_REPOSITORY_URL_LIST, repositoryURLList);
        return super.performOk();
    }

    private final class ModelRepositoryListEditor extends ListEditor {

        private ModelRepositoryListEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
            getList().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean hasMoreThanOneUrl = getList().getItems().length > 1;
                    getRemoveButton().setEnabled(hasMoreThanOneUrl);
                }
            });
        }

        public String[] getItems() {
            return super.getList().getItems();

        }

        @Override
        protected String[] parseString(String stringList) {
            return ModelsRcpPreferences.splitRemoteRepositoryString(stringList);
        }

        @Override
        protected String getNewInputObject() {
            InputDialog inputDialog = Dialogs.newModelRepositoryUrlDialog(getShell(), getItems());
            if (inputDialog.open() == Window.OK) {
                return inputDialog.getValue();
            }
            return null;
        }

        @Override
        protected String createList(String[] items) {
            return ModelsRcpPreferences.joinRemoteRepositoriesToString(items);
        }
    }
}

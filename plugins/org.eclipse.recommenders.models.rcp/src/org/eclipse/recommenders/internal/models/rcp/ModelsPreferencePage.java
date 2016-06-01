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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.internal.models.rcp.l10n.Messages;
import org.eclipse.recommenders.utils.Urls;
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
        setMessage(Messages.PREFPAGE_TITLE_MODEL_REPOSITORIES);
        setDescription(Messages.PREFPAGE_DESCRIPTION_MODEL_REPOSITORIES);
    }

    @Override
    protected void createFieldEditors() {
        repoEditor = new ModelRepositoryListEditor(PREF_REPOSITORY_URL_LIST, Messages.FIELD_LABEL_REPOSITORY_URIS,
                getFieldEditorParent());
        addField(repoEditor);
        addField(new BooleanFieldEditor(PREF_REPOSITORY_ENABLE_AUTO_DOWNLOAD, Messages.FIELD_LABEL_ENABLE_AUTO_DOWNLOAD,
                getFieldEditorParent()));
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        String repositoryURLList = Joiner.on(ModelsRcpPreferences.URL_SEPARATOR).join(repoEditor.getItems());
        store.setValue(PREF_REPOSITORY_URL_LIST, repositoryURLList);
        return super.performOk();
    }

    private static final class ModelRepositoryListEditor extends ListEditor {

        private final Map<String, String> toUnobfuscatedUrls = new HashMap<>();

        private ModelRepositoryListEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        public String[] getItems() {
            return super.getList().getItems();
        }

        @Override
        protected String getNewInputObject() {
            InputDialog inputDialog = Dialogs.newModelRepositoryUrlDialog(getShell(), getItems());
            if (inputDialog.open() == Window.OK) {
                String unobfuscatedUrl = inputDialog.getValue();
                String obfuscatedUrl = Urls.toStringWithMaskedPassword(Urls.toUrl(unobfuscatedUrl), '*');
                toUnobfuscatedUrls.put(obfuscatedUrl, unobfuscatedUrl);
                return obfuscatedUrl;
            }
            return null;
        }

        @Override
        protected String[] parseString(String string) {
            String[] unobfuscatedUrls = ModelsRcpPreferences.splitRemoteRepositoryString(string);
            String[] list = new String[unobfuscatedUrls.length];
            for (int i = 0; i < unobfuscatedUrls.length; i++) {
                list[i] = Urls.toStringWithMaskedPassword(Urls.toUrl(unobfuscatedUrls[i]), '*');
                toUnobfuscatedUrls.put(list[i], unobfuscatedUrls[i]);
            }
            return list;
        }

        @Override
        protected String createList(String[] items) {
            String[] unobfuscated = new String[items.length];
            for (int i = 0; i < items.length; i++) {
                unobfuscated[i] = toUnobfuscatedUrls.get(items[i]);
            }
            return ModelsRcpPreferences.joinRemoteRepositoriesToString(unobfuscated);
        }
    }
}

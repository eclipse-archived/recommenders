/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye, Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Cheng Chen - initial API and implementation.
 */

package org.eclipse.recommenders.snipmatch.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * SnipMatch local setting preference page.
 */
public class GitRepositorySettingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private DirectoryFieldEditor snippetsDir = null;
    private FileFieldEditor snippetsIndexFile = null;
    private Button updateIndexButton = null;

    public GitRepositorySettingPreferencePage() {
        super();
        setPreferenceStore(SnipMatchPlugin.getDefault().getPreferenceStore());
    }

    protected Control createContents(Composite parent) {
        Composite storeSettingGroup = new Composite(parent, SWT.LEFT);
        storeSettingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        storeSettingGroup.setLayout(new GridLayout());

        snippetsDir = new DirectoryFieldEditor(PreferenceConstants.SNIPPETS_STORE_DIR, "&Snippets directory:",
                storeSettingGroup);
        snippetsDir.setPreferencePage(this);
        snippetsDir.setPreferenceStore(getPreferenceStore());
        snippetsDir.load();

        snippetsIndexFile = new FileFieldEditor(PreferenceConstants.SNIPPETS_INDEX_FILE, "&Search engine file path:",
                storeSettingGroup);
        snippetsIndexFile.setPreferencePage(this);
        snippetsIndexFile.setPreferenceStore(getPreferenceStore());
        snippetsIndexFile.load();

        updateIndexButton = new Button(storeSettingGroup, SWT.NONE);
        updateIndexButton.setText(" Update index ");

        updateIndexButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                performOk();
                SnipMatchPlugin.getDefault().getSearchEngine().updateIndex();
            }
        });

        return storeSettingGroup;
    }

    public void dispose() {
        super.dispose();
    }

    protected void performDefaults() {
        this.snippetsDir.loadDefault();
        this.snippetsIndexFile.loadDefault();
        super.performDefaults();
    }

    public boolean performOk() {
        this.snippetsDir.store();
        this.snippetsIndexFile.store();
        return true;
    }

    public void init(IWorkbench workbench) {
    }

}

/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.recommenders.internal.news.rcp.Constants;
import org.eclipse.recommenders.internal.news.rcp.PreferenceConstants;
import org.eclipse.recommenders.internal.news.rcp.l10n.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class NewsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private BooleanFieldEditor enabledEditor;
    private FeedListEditor feedEditor;
    private IntegerFieldEditor startupEditor;

    public NewsPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PLUGIN_ID));
        setMessage(Messages.PREFPAGE_TITLE);
        setDescription(Messages.PREFPAGE_DESCRIPTION);
    }

    @Override
    protected void createFieldEditors() {
        enabledEditor = new BooleanFieldEditor(PreferenceConstants.NEWS_ENABLED, Messages.FIELD_LABEL_NEWS_ENABLED, 0,
                getFieldEditorParent()) {
            @Override
            protected void valueChanged(boolean oldValue, boolean newValue) {
                super.valueChanged(oldValue, newValue);
                startupEditor.setEnabled(enabledEditor.getBooleanValue(), getFieldEditorParent());
            }
        };
        addField(enabledEditor);

        startupEditor = new IntegerFieldEditor(PreferenceConstants.POLLING_DELAY, Messages.FIELD_LABEL_STARTUP_DELAY,
                getFieldEditorParent(), 4);
        startupEditor.setEnabled(getPreferenceStore().getBoolean(PreferenceConstants.NEWS_ENABLED),
                getFieldEditorParent());
        addField(startupEditor);

        final Composite bottomGroup = new Composite(getFieldEditorParent(), SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(bottomGroup);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(bottomGroup);

        feedEditor = new FeedListEditor(PreferenceConstants.FEED_LIST_SORTED, Messages.FIELD_LABEL_FEEDS, bottomGroup);
        addField(feedEditor);

        addField(new LinkEditor(Messages.PREFPAGE_NOTIFICATION_ENABLEMENT,
                "org.eclipse.mylyn.commons.notifications.preferencePages.Notifications", //$NON-NLS-1$
                getFieldEditorParent()));
        addField(new LinkEditor(Messages.PREFPAGE_WEB_BROWSER_SETTINGS, "org.eclipse.ui.browser.preferencePage", //$NON-NLS-1$
                getFieldEditorParent()));

        Dialog.applyDialogFont(getControl());
    }
}

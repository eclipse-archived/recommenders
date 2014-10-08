/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Haftstein - initial implementation
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public PreferencePage() {
        super(GRID);

    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.PLUGIN_ID));
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(PROP_SERVER, Messages.FIELD_LABEL_SERVER, getFieldEditorParent()));
        addField(new StringFieldEditor(PROP_NAME, Messages.FIELD_LABEL_NAME, getFieldEditorParent()));
        addField(new StringFieldEditor(PROP_EMAIL, Messages.FIELD_LABEL_EMAIL, getFieldEditorParent()));
        addField(new ComboFieldEditor(PROP_SEND_ACTION, Messages.FIELD_LABEL_ACTION, createModeLabelAndValues(),
                getFieldEditorParent()));

        BooleanFieldEditor anonymizeStacktracesFieldEditor = new BooleanFieldEditor(PROP_ANONYMIZE_STACKTRACES,
                Messages.FIELD_LABEL_ANONYMIZE_STACKTRACES, getFieldEditorParent());
        DefaultToolTip anonymizeStacktracesToolTip = new DefaultToolTip(
                anonymizeStacktracesFieldEditor.getDescriptionControl(getFieldEditorParent()));
        anonymizeStacktracesToolTip.setText(Messages.TOOLTIP_ANONYMIZE_STACKTRACES);
        addField(anonymizeStacktracesFieldEditor);

        BooleanFieldEditor clearMessagesFieldEditor = new BooleanFieldEditor(PROP_ANONYMIZE_MESSAGES,
                Messages.FIELD_LABEL_ANONYMIZE_MESSAGES, getFieldEditorParent());
        DefaultToolTip clearMessagesToolTip = new DefaultToolTip(
                clearMessagesFieldEditor.getDescriptionControl(getFieldEditorParent()));
        clearMessagesToolTip.setText(Messages.TOOLTIP_CLEAR_MESSAGES);
        addField(clearMessagesFieldEditor);

        BooleanFieldEditor skipSimilarErrorsFieldEditor = new BooleanFieldEditor(PROP_SKIP_SIMILAR_ERRORS,
                Messages.FIELD_LABEL_SKIP_SIMILAR_ERRORS, getFieldEditorParent());
        DefaultToolTip skipSimilarErrorsToolTip = new DefaultToolTip(
                skipSimilarErrorsFieldEditor.getDescriptionControl(getFieldEditorParent()));
        skipSimilarErrorsToolTip.setText(Messages.TOOLTIP_SKIP_SIMILAR);
        addField(skipSimilarErrorsFieldEditor);

        addLinks(getFieldEditorParent());
    }

    private void addLinks(Composite parent) {
        Composite feedback = new Composite(parent, SWT.NONE);
        feedback.setLayout(new RowLayout(SWT.VERTICAL));
        Link learnMoreLink = new Link(feedback, SWT.NONE);
        learnMoreLink.setText(Messages.LINK_LEARN_MORE);
        learnMoreLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Browsers.openInExternalBrowser(HELP_URL);
            }
        });

        Link feedbackLink = new Link(feedback, SWT.NONE);
        feedbackLink.setText(Messages.LINK_PROVIDE_FEEDBACK);
        feedbackLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Browsers.openInExternalBrowser(FEEDBACK_FORM_URL);
            }
        });
    }

    private static String[][] createModeLabelAndValues() {
        SendAction[] modes = SendAction.values();
        String[][] labelAndValues = new String[modes.length][2];
        for (int i = 0; i < modes.length; i++) {
            SendAction mode = modes[i];
            labelAndValues[i][0] = descriptionForMode(mode);
            labelAndValues[i][1] = mode.name();
        }
        return labelAndValues;
    }

    private static String descriptionForMode(SendAction mode) {
        switch (mode) {
        case ASK:
            return Messages.FIELD_LABEL_ACTION_REPORT_ASK;
        case IGNORE:
            return Messages.FIELD_LABEL_ACTION_REPORT_NEVER;
        case SILENT:
            return Messages.FIELD_LABEL_ACTION_REPORT_ALWAYS;
        case PAUSE_DAY:
            return Messages.FIELD_LABEL_ACTION_REPORT_PAUSE_DAY;
        case PAUSE_RESTART:
            return Messages.FIELD_LABEL_ACTION_REPORT_PAUSE_RESTART;
        default:
            return mode.name();
        }
    }

}

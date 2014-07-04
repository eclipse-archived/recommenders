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

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.eventbus.EventBus;

public class SnipmatchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private EventBus bus = InjectionService.getInstance().requestInstance(EventBus.class);

    private final UriInputValidator uriValidator = new UriInputValidator();
    private final BranchInputValidator branchValidator = new BranchInputValidator();
    private final FieldDecoration errorDecoration;

    private StringFieldEditor snippetsRepoFetchUrlField;
    private StringFieldEditor snippetsRepoPushUrlField;
    private StringFieldEditor snippetsRepoPushBranchField;
    private ControlDecoration snippetsRepoFetchUrlDecoration;
    private ControlDecoration snippetsRepoPushUrlDecoration;
    private ControlDecoration snippetsRepoPushBranchDecoration;

    public SnipmatchPreferencePage() {
        super(GRID);
        setDescription(Messages.PREFPAGE_DESCRIPTION);
        errorDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
    }

    @Override
    public void init(IWorkbench workbench) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID);
        setPreferenceStore(store);
    }

    @Override
    public void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(parent);

        addFetchGroup(parent);
        addPushGroup(parent);
    }

    private void addFetchGroup(Composite parent) {
        Group fetchGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        fetchGroup.setText(Messages.GROUP_FETCH_SETTINGS);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(fetchGroup);

        snippetsRepoFetchUrlField = new StringFieldEditor(PREF_SNIPPETS_REPO_FETCH_URL,
                Messages.PREFPAGE_LABEL_SNIPPETS_REPO_FETCH_URL, fetchGroup) {
            @Override
            protected boolean doCheckState() {
                String errorMessage = uriValidator.isValid(getStringValue());
                snippetsRepoFetchUrlDecoration.setDescriptionText(errorMessage);
                if (errorMessage == null) {
                    snippetsRepoFetchUrlDecoration.hide();
                } else {
                    snippetsRepoFetchUrlDecoration.show();
                }
                return errorMessage == null;
            }

            @Override
            protected void adjustForNumColumns(int numColumns) {
                ((GridData) getTextControl().getLayoutData()).horizontalSpan = numColumns - 2;
            }
        };

        snippetsRepoFetchUrlDecoration = new ControlDecoration(snippetsRepoFetchUrlField.getTextControl(fetchGroup),
                SWT.LEFT | SWT.TOP);
        snippetsRepoFetchUrlDecoration.setImage(errorDecoration.getImage());

        updateMargins(fetchGroup);
        addField(snippetsRepoFetchUrlField);
    }

    private void addPushGroup(Composite parent) {
        Group pushGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        pushGroup.setText(Messages.GROUP_PUSH_SETTINGS);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(pushGroup);

        snippetsRepoPushUrlField = new StringFieldEditor(PREF_SNIPPETS_REPO_PUSH_URL,
                Messages.PREFPAGE_LABEL_SNIPPETS_REPO_PUSH_URL, pushGroup) {

            @Override
            protected boolean doCheckState() {
                String errorMessage = uriValidator.isValid(getStringValue());
                snippetsRepoPushUrlDecoration.setDescriptionText(errorMessage);
                if (errorMessage == null) {
                    snippetsRepoPushUrlDecoration.hide();
                } else {
                    snippetsRepoPushUrlDecoration.show();
                }
                return errorMessage == null;
            }
        };

        snippetsRepoPushUrlDecoration = new ControlDecoration(snippetsRepoPushUrlField.getTextControl(pushGroup),
                SWT.LEFT | SWT.TOP);
        snippetsRepoPushUrlDecoration.setImage(errorDecoration.getImage());

        addField(snippetsRepoPushUrlField);

        Label pushBranchDescription = new Label(pushGroup, SWT.NONE | SWT.WRAP);
        pushBranchDescription.setText(MessageFormat.format(Messages.PREFPAGE_LABEL_SNIPPETS_PUSH_SETTINGS_DESCRIPTION,
                Snippet.FORMAT_VERSION));
        GridDataFactory.fillDefaults().span(3, 1).grab(true, false).hint(100, SWT.DEFAULT)
        .applyTo(pushBranchDescription);

        snippetsRepoPushBranchField = new StringFieldEditorWithPrefix(PREF_SNIPPETS_REPO_PUSH_BRANCH,
                Messages.PREFPAGE_LABEL_SNIPPETS_REPO_PUSH_BRANCH, pushGroup);

        snippetsRepoPushBranchDecoration = new ControlDecoration(snippetsRepoPushBranchField.getTextControl(pushGroup),
                SWT.LEFT | SWT.TOP);
        snippetsRepoPushBranchDecoration.setImage(errorDecoration.getImage());

        addField(snippetsRepoPushBranchField);
        updateMargins(pushGroup);
    }

    private void updateMargins(Group group) {
        GridLayout layout = (GridLayout) group.getLayout();
        layout.marginWidth = 5;
        layout.marginHeight = 5;
    }

    @Override
    public boolean performOk() {
        boolean dirty = isDirty();
        boolean ok = super.performOk();
        if (ok && dirty) {
            bus.post(new EclipseGitSnippetRepository.SnippetRepositoryConfigurationChangedEvent());
        }
        return ok;
    }

    private boolean isDirty() {
        String oldRepoFetchUrl = getPreferenceStore().getString(PREF_SNIPPETS_REPO_FETCH_URL);
        if (!snippetsRepoFetchUrlField.getStringValue().equals(oldRepoFetchUrl)) {
            return true;
        }
        String oldRepoPushUrl = getPreferenceStore().getString(PREF_SNIPPETS_REPO_PUSH_URL);
        if (!snippetsRepoPushUrlField.getStringValue().equals(oldRepoPushUrl)) {
            return true;
        }
        String oldRepoPushBranch = getPreferenceStore().getString(PREF_SNIPPETS_REPO_PUSH_BRANCH);
        if (!snippetsRepoPushBranchField.getStringValue().equals(oldRepoPushBranch)) {
            return true;
        }
        return false;
    }

    private final class StringFieldEditorWithPrefix extends StringFieldEditor {

        private Label prefixLabel;

        private StringFieldEditorWithPrefix(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        @Override
        protected void adjustForNumColumns(int numColumns) {
            ((GridData) getTextControl().getLayoutData()).horizontalSpan = numColumns - 2;
        }

        @Override
        public int getNumberOfControls() {
            return 3;
        }

        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns) {
            super.doFillIntoGrid(parent, numColumns - 1);
            prefixLabel = getPrefixControl(parent);
            GridData gd = new GridData();
            gd.horizontalAlignment = GridData.FILL;
            gd.widthHint = prefixLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
            prefixLabel.setLayoutData(gd);
        }

        private Label getPrefixControl(Composite parent) {
            if (prefixLabel == null) {
                prefixLabel = new Label(parent, SWT.NONE);
                prefixLabel.setText('/' + Snippet.FORMAT_VERSION);
                prefixLabel.addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        prefixLabel = null;
                    }
                });
            }
            return prefixLabel;
        }

        @Override
        protected boolean doCheckState() {
            String message = branchValidator.isValid(getStringValue());
            snippetsRepoPushBranchDecoration.setDescriptionText(message);
            if (message == null) {
                snippetsRepoPushBranchDecoration.hide();
            } else {
                snippetsRepoPushBranchDecoration.show();
            }
            return message == null;
        }
    }

    private final class UriInputValidator implements IInputValidator {
        @Override
        public String isValid(String newText) {
            if (newText.isEmpty()) {
                return Messages.PREFPAGE_ERROR_INVALID_BRANCH_PREFIX_FORMAT;
            }
            try {
                new URI(newText);
                return null;
            } catch (URISyntaxException e) {
                return e.getMessage();
            }
        }
    }
}

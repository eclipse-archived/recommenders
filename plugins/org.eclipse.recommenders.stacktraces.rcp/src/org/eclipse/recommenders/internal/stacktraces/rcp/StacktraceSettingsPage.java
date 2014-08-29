/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Daniel Haftstein - added support for multiple stacktraces
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_INFORMATION;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.stacktraces.rcp.StacktraceWizard.WizardPreferences;
import org.eclipse.recommenders.internal.stacktraces.rcp.StacktracesRcpPreferences.Mode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;

class StacktraceSettingsPage extends WizardPage {
    private ComboViewer actionComboViewer;
    private Text emailText;
    private Text nameText;
    private Button anonymizeStacktracesButton;
    private Button clearMessagesButton;
    private WizardPreferences wizardPreferences;

    protected StacktraceSettingsPage(WizardPreferences wizardPreferences) {
        super(StacktraceSettingsPage.class.getName());
        this.wizardPreferences = wizardPreferences;
    }

    @Override
    public void createControl(Composite parent) {
        setTitle(Messages.SETTINGSPAGE_TITEL);
        setDescription(Messages.SETTINGSPAGE_DESC);
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        GridLayoutFactory layoutFactory = GridLayoutFactory.fillDefaults().numColumns(2);
        GridDataFactory dataFactory = GridDataFactory.fillDefaults().grab(true, false);
        Group personalGroup = new Group(container, SWT.SHADOW_ETCHED_IN | SWT.SHADOW_ETCHED_OUT | SWT.SHADOW_IN
                | SWT.SHADOW_OUT);
        personalGroup.setText(Messages.SETTINGSPAGE_GROUPLABEL_PERSONAL);
        layoutFactory.applyTo(personalGroup);
        dataFactory.applyTo(personalGroup);
        FieldDecoration infoDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(DEC_INFORMATION);
        {
            new Label(personalGroup, SWT.NONE).setText(Messages.FIELD_LABEL_NAME);
            nameText = new Text(personalGroup, SWT.BORDER);
            nameText.setText(wizardPreferences.name);
            nameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent event) {
                    wizardPreferences.name = nameText.getText();
                }
            });
            dataFactory.applyTo(nameText);
            ControlDecoration dec = new ControlDecoration(nameText, SWT.TOP | SWT.LEFT);
            dec.setImage(infoDecoration.getImage());
            dec.setDescriptionText(Messages.FIELD_DESC_NAME);
        }
        {
            new Label(personalGroup, SWT.NONE).setText(Messages.FIELD_LABEL_EMAIL);
            emailText = new Text(personalGroup, SWT.BORDER);
            emailText.setText(wizardPreferences.email);
            emailText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent event) {
                    wizardPreferences.email = emailText.getText();
                }
            });
            dataFactory.applyTo(emailText);
            ControlDecoration dec = new ControlDecoration(emailText, SWT.TOP | SWT.LEFT);
            dec.setImage(infoDecoration.getImage());
            dec.setDescriptionText(Messages.FIELD_DESC_EMAIL);
        }
        {
            new Label(personalGroup, SWT.NONE).setText(Messages.FIELD_LABEL_ACTION);
            actionComboViewer = new ComboViewer(personalGroup, SWT.READ_ONLY);
            actionComboViewer.setContentProvider(ArrayContentProvider.getInstance());
            actionComboViewer.setInput(Lists.newArrayList(Mode.class.getEnumConstants()));
            actionComboViewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    Mode mode = (Mode) element;
                    switch (mode) {
                    case ASK:
                        return Messages.FIELD_LABEL_ACTION_REPORT_ASK;
                    case IGNORE:
                        return Messages.FIELD_LABEL_ACTION_REPORT_NEVER;
                    case SILENT:
                        return Messages.FIELD_LABEL_ACTION_REPORT_ALWAYS;
                    default:
                        return super.getText(element);
                    }
                }
            });
            actionComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    if (!event.getSelection().isEmpty()) {
                        IStructuredSelection selection = (IStructuredSelection) actionComboViewer.getSelection();
                        Mode mode = (Mode) selection.getFirstElement();
                        if (mode != null) {
                            wizardPreferences.mode = mode;
                        }
                    }

                }
            });
            actionComboViewer.setSelection(new StructuredSelection(wizardPreferences.mode));
            dataFactory.applyTo(actionComboViewer.getControl());
        }
        {
            anonymizeStacktracesButton = new Button(container, SWT.CHECK);
            anonymizeStacktracesButton.setText(Messages.FIELD_LABEL_ANONYMIZE_STACKTRACES);
            anonymizeStacktracesButton.setSelection(wizardPreferences.anonymize);
            anonymizeStacktracesButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    wizardPreferences.anonymize = anonymizeStacktracesButton.getSelection();
                }
            });
            clearMessagesButton = new Button(container, SWT.CHECK);
            clearMessagesButton.setText(Messages.FIELD_LABEL_CLEAR_MESSAGES);
            clearMessagesButton.setSelection(wizardPreferences.clearMessages);
            clearMessagesButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    wizardPreferences.clearMessages = clearMessagesButton.getSelection();
                }
            });

        }
        {
            Composite feedback = new Composite(container, SWT.NONE);
            layoutFactory.applyTo(feedback);
            dataFactory.grab(true, true).applyTo(feedback);
            {
                Link feedbackLink = new Link(feedback, SWT.NONE);
                dataFactory.align(SWT.BEGINNING, SWT.END).applyTo(feedbackLink);
                feedbackLink.setText(Messages.LINK_LEARN_MORE);
                feedbackLink.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Browsers.openInExternalBrowser(HELP_URL);
                    }
                });
            }

            {
                Link feedbackLink = new Link(feedback, SWT.NONE);
                dataFactory.align(SWT.END, SWT.END).applyTo(feedbackLink);
                feedbackLink.setText(Messages.LINK_PROVIDE_FEEDBACK);
                feedbackLink.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Browsers.openInExternalBrowser(FEEDBACK_FORM_URL);
                    }
                });
            }

        }
        setControl(container);
    }

    @Override
    public void performHelp() {
        Browsers.openInExternalBrowser(HELP_URL);
    }
}

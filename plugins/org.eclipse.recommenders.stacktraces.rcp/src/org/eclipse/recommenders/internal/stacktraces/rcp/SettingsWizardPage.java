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

import static org.eclipse.emf.databinding.EMFProperties.value;
import static org.eclipse.jface.databinding.swt.WidgetProperties.*;
import static org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_INFORMATION;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.SendAction;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

class SettingsWizardPage extends WizardPage {
    private ComboViewer actionComboViewer;
    private Text txtEmail;
    private Text txtName;
    private Button btnAnonymizeStacktraces;
    private Button btnClearMessages;
    private Settings settings;

    protected SettingsWizardPage(Settings settings) {
        super(SettingsWizardPage.class.getName());
        this.settings = settings;
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
            txtName = new Text(personalGroup, SWT.BORDER);
            txtName.setMessage(Messages.FIELD_MESSAGE_NAME);
            dataFactory.applyTo(txtName);
            ControlDecoration dec = new ControlDecoration(txtName, SWT.TOP | SWT.LEFT);
            dec.setImage(infoDecoration.getImage());
            dec.setDescriptionText(Messages.FIELD_DESC_NAME);
        }
        {
            new Label(personalGroup, SWT.NONE).setText(Messages.FIELD_LABEL_EMAIL);
            txtEmail = new Text(personalGroup, SWT.BORDER);
            txtEmail.setMessage(Messages.FIELD_MESSAGE_EMAIL);
            dataFactory.applyTo(txtEmail);
            ControlDecoration dec = new ControlDecoration(txtEmail, SWT.TOP | SWT.LEFT);
            dec.setImage(infoDecoration.getImage());
            dec.setDescriptionText(Messages.FIELD_DESC_EMAIL);
        }
        {
            new Label(personalGroup, SWT.NONE).setText(Messages.FIELD_LABEL_ACTION);
            actionComboViewer = new ComboViewer(personalGroup, SWT.READ_ONLY);
            actionComboViewer.setContentProvider(ArrayContentProvider.getInstance());
            actionComboViewer.setInput(SendAction.values());
            actionComboViewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    SendAction mode = (SendAction) element;
                    switch (mode) {
                    case ASK:
                        return Messages.FIELD_LABEL_ACTION_REPORT_ASK;
                    case IGNORE:
                        return Messages.FIELD_LABEL_ACTION_REPORT_NEVER;
                    case SILENT:
                        return Messages.FIELD_LABEL_ACTION_REPORT_ALWAYS;
                    case PAUSE:
                        return Messages.FIELD_LABEL_ACTION_REPORT_ALWAYS;
                    default:
                        return super.getText(element);
                    }
                }
            });
            actionComboViewer.setSelection(new StructuredSelection(settings.getAction()));
            dataFactory.applyTo(actionComboViewer.getControl());
        }
        {
            btnAnonymizeStacktraces = new Button(container, SWT.CHECK);
            btnAnonymizeStacktraces.setText(Messages.FIELD_LABEL_ANONYMIZE_STACKTRACES);

            btnClearMessages = new Button(container, SWT.CHECK);
            btnClearMessages.setText(Messages.FIELD_LABEL_ANONYMIZE_MESSAGES);
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
        createDataBindingContext();
    }

    private DataBindingContext createDataBindingContext() {
        DataBindingContext context = new DataBindingContext();

        ModelPackage pkg = ModelPackage.eINSTANCE;

        IObservableValue ovTxtName = text(SWT.Modify).observe(txtName);
        IObservableValue ovSetName = value(pkg.getSettings_Name()).observe(settings);
        context.bindValue(ovTxtName, ovSetName, null, null);

        IObservableValue ovTxtEmail = text(SWT.Modify).observe(txtEmail);
        IObservableValue ovSetEmail = value(pkg.getSettings_Email()).observe(settings);
        context.bindValue(ovTxtEmail, ovSetEmail, null, null);

        IObservableValue ovBtnAnonSt = selection().observe(btnAnonymizeStacktraces);
        IObservableValue ovSetAnonSt = value(pkg.getSettings_AnonymizeStrackTraceElements()).observe(settings);
        context.bindValue(ovBtnAnonSt, ovSetAnonSt, null, null);

        IObservableValue ovBtnAnonMsg = selection().observe(btnClearMessages);
        IObservableValue ovSetAnonMsg = value(pkg.getSettings_AnonymizeMessages()).observe(settings);
        context.bindValue(ovBtnAnonMsg, ovSetAnonMsg, null, null);

        IObservableValue ovVwrAction = ViewersObservables.observeSinglePostSelection(actionComboViewer);
        IObservableValue ovSetAction = value(pkg.getSettings_Action()).observe(settings);
        context.bindValue(ovVwrAction, ovSetAction, null, null);

        return context;
    }

    @Override
    public void performHelp() {
        Browsers.openInExternalBrowser(HELP_URL);
    }
}

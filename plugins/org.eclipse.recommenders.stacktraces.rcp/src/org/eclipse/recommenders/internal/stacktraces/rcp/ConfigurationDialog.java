/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.emf.databinding.EMFProperties.value;
import static org.eclipse.jface.databinding.swt.WidgetProperties.*;
import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.*;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ModelPackage;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConfigurationDialog extends TitleAreaDialog {

    /**
     * Return code to indicate a cancel using the esc-button.
     */
    public static final int ESC_CANCEL = 42 + 42;
    public static final ImageDescriptor TITLE_IMAGE_DESC = ImageDescriptor.createFromFile(ConfigurationDialog.class,
            "/icons/wizban/stackframes_wiz.gif"); //$NON-NLS-1$
    private Text emailText;
    private Text nameText;
    private Button anonymizeStacktracesButton;
    private Button clearMessagesButton;

    private Settings settings;

    public ConfigurationDialog(Shell parentShell, final Settings settings) {
        super(parentShell);
        this.settings = settings;
        setHelpAvailable(false);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("An Error Was Logged");
        shell.addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    e.doit = false;
                    setReturnCode(ESC_CANCEL);
                    close();
                }
            }
        });
    }

    @Override
    public void create() {
        super.create();
        setTitle("Do you want to enable Error Reporting in Eclipse?");
        String message = "Error events may reveal issues in Eclipse. Thus we ask you to report them to eclipse.org. To help improving Eclipse, please enable the reporter.";
        setMessage(message);

        // move focus away from first text-field to show its message-hint
        anonymizeStacktracesButton.setFocus();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages.CONFIGURATIONDIALOG_ENABLE, true);
        createButton(parent, IDialogConstants.CANCEL_ID, Messages.CONFIGURATIONDIALOG_DISABLE, false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        setTitleImage(TITLE_IMAGE_DESC.createImage());
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
        //
        Composite personalGroup = createPersonalGroup(container);
        GridDataFactory.fillDefaults().indent(10, 10).grab(true, false).applyTo(personalGroup);
        //
        Group makeAnonymousGroup = makeAnonymousGroup(container);
        GridDataFactory.fillDefaults().applyTo(makeAnonymousGroup);
        //
        Composite linksComposite = createLinksComposite(container);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(linksComposite);

        createDataBindingContext();
        return container;
    }

    private Composite createPersonalGroup(Composite container) {
        Composite personalGroup = new Composite(container, SWT.NONE);
        // personalGroup.setText(Messages.SETTINGSPAGE_GROUPLABEL_PERSONAL);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(personalGroup);
        GridDataFactory grab = GridDataFactory.fillDefaults().grab(true, false);
        Label nameLabel = new Label(personalGroup, SWT.NONE);
        nameLabel.setText(Messages.FIELD_LABEL_NAME);
        String nameTooltip = Messages.FIELD_MESSAGE_NAME;
        nameLabel.setToolTipText(nameTooltip);
        nameText = new Text(personalGroup, SWT.BORDER);
        nameText.setMessage(nameTooltip);
        nameText.setToolTipText(nameTooltip);
        grab.applyTo(nameText);

        Label emailLabel = new Label(personalGroup, SWT.NONE);
        emailLabel.setText(Messages.FIELD_LABEL_EMAIL);
        String emailTooltip = Messages.FIELD_MESSAGE_EMAIL + Messages.FIELD_DESC_EMAIL;
        emailLabel.setToolTipText(emailTooltip);
        emailText = new Text(personalGroup, SWT.BORDER);
        emailText.setMessage(Messages.FIELD_MESSAGE_EMAIL);
        emailText.setToolTipText(emailTooltip);
        grab.applyTo(emailText);
        return personalGroup;
    }

    private Group makeAnonymousGroup(Composite container) {
        Group makeAnonymousGroup = new Group(container, SWT.SHADOW_ETCHED_IN | SWT.SHADOW_ETCHED_OUT | SWT.SHADOW_IN
                | SWT.SHADOW_OUT);
        makeAnonymousGroup.setLayout(new RowLayout(SWT.VERTICAL));
        makeAnonymousGroup.setText(Messages.CONFIGURATIONDIALOG_ANONYMIZATION);
        anonymizeStacktracesButton = new Button(makeAnonymousGroup, SWT.CHECK);
        anonymizeStacktracesButton.setText(Messages.FIELD_LABEL_ANONYMIZE_STACKTRACES);
        anonymizeStacktracesButton.setToolTipText(Messages.TOOLTIP_MAKE_STACKTRACE_ANONYMOUS);

        clearMessagesButton = new Button(makeAnonymousGroup, SWT.CHECK);
        clearMessagesButton.setText(Messages.FIELD_LABEL_ANONYMIZE_MESSAGES);
        clearMessagesButton.setToolTipText(Messages.TOOLTIP_MAKE_MESSAGES_ANONYMOUS);
        return makeAnonymousGroup;
    }

    private Composite createLinksComposite(Composite container) {
        Composite linksComposite = new Composite(container, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(linksComposite);
        Link learnMoreLink = new Link(linksComposite, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.BEGINNING, SWT.END).applyTo(learnMoreLink);
        learnMoreLink.setText(Messages.LINK_LEARN_MORE);
        learnMoreLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Browsers.openInExternalBrowser(HELP_URL);
            }
        });
        Link feedbackLink = new Link(linksComposite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.END).applyTo(feedbackLink);
        feedbackLink.setText(Messages.LINK_PROVIDE_FEEDBACK);
        feedbackLink.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Browsers.openInExternalBrowser(FEEDBACK_FORM_URL);
            }
        });
        return linksComposite;
    }

    private void createDataBindingContext() {
        DataBindingContext context = new DataBindingContext();

        ModelPackage pkg = ModelPackage.eINSTANCE;

        IObservableValue ovTxtName = text(SWT.Modify).observe(nameText);
        IObservableValue ovSetName = value(pkg.getSettings_Name()).observe(settings);
        context.bindValue(ovTxtName, ovSetName, null, null);

        IObservableValue ovTxtEmail = text(SWT.Modify).observe(emailText);
        IObservableValue ovSetEmail = value(pkg.getSettings_Email()).observe(settings);
        context.bindValue(ovTxtEmail, ovSetEmail, null, null);

        IObservableValue ovBtnAnonSt = selection().observe(anonymizeStacktracesButton);
        IObservableValue ovSetAnonSt = value(pkg.getSettings_AnonymizeStrackTraceElements()).observe(settings);
        context.bindValue(ovBtnAnonSt, ovSetAnonSt, null, null);

        IObservableValue ovBtnAnonMsg = selection().observe(clearMessagesButton);
        IObservableValue ovSetAnonMsg = value(pkg.getSettings_AnonymizeMessages()).observe(settings);
        context.bindValue(ovBtnAnonMsg, ovSetAnonMsg, null, null);
    }
}

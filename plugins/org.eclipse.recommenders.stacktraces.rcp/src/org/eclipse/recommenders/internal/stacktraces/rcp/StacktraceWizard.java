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
import static org.eclipse.recommenders.internal.stacktraces.rcp.StacktracesRcpPreferences.*;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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

public class StacktraceWizard extends Wizard implements IWizard {

    class StacktracePage extends WizardPage {

        private ComboViewer actionComboViewer;
        private Text emailTxt;
        private Text nameTxt;
        private Button anonymizeStacktracesButton;
        private Button clearMessagesButton;

        protected StacktracePage() {
            super(StacktracePage.class.getName());
        }

        @Override
        public void createControl(Composite parent) {
            setTitle("An error has been logged. Help us fixing it.");
            setDescription("Please provide any additional information\nthat may help us to reproduce the problem (optional).");
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout());

            GridLayoutFactory glFactory = GridLayoutFactory.fillDefaults().numColumns(2);
            GridDataFactory gdFactory = GridDataFactory.fillDefaults().grab(true, false);
            Group personal = new Group(container, SWT.SHADOW_ETCHED_IN | SWT.SHADOW_ETCHED_OUT | SWT.SHADOW_IN
                    | SWT.SHADOW_OUT);
            personal.setText("Personal Information");
            glFactory.applyTo(personal);
            gdFactory.applyTo(personal);
            FieldDecoration infoDec = FieldDecorationRegistry.getDefault().getFieldDecoration(DEC_INFORMATION);
            {
                new Label(personal, SWT.NONE).setText("Name:");
                nameTxt = new Text(personal, SWT.BORDER);
                nameTxt.setText(prefs.name);
                gdFactory.applyTo(nameTxt);
                ControlDecoration dec = new ControlDecoration(nameTxt, SWT.TOP | SWT.LEFT);
                dec.setImage(infoDec.getImage());
                dec.setDescriptionText("Optional. May be helpful for the team to see who reported the issue.");
            }
            {
                new Label(personal, SWT.NONE).setText("Email:");
                emailTxt = new Text(personal, SWT.BORDER);
                emailTxt.setText(prefs.email);
                gdFactory.applyTo(emailTxt);
                ControlDecoration dec = new ControlDecoration(emailTxt, SWT.TOP | SWT.LEFT);
                dec.setImage(infoDec.getImage());
                dec.setDescriptionText("Optional. Your email address allows us to get in touch with you when this issue has been fixed.");
            }
            {
                new Label(personal, SWT.NONE).setText("Action:");
                actionComboViewer = new ComboViewer(personal, SWT.READ_ONLY);
                actionComboViewer.setContentProvider(ArrayContentProvider.getInstance());
                actionComboViewer.setInput(Lists.newArrayList("ask", "ignore", "silent"));
                actionComboViewer.setLabelProvider(new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        if (MODE_ASK.equals(element)) {
                            return "Report now but ask me again next time.";
                        } else if (MODE_IGNORE.equals(element)) {
                            return "Don't report and never ask me again.";
                        } else if (MODE_SILENT.equals(element)) {
                            return "I love to help. Send all errors you see to the dev team immediately.";
                        }
                        return super.getText(element);
                    }
                });
                actionComboViewer.setSelection(new StructuredSelection(prefs.mode));
                gdFactory.applyTo(actionComboViewer.getControl());
            }
            {
                anonymizeStacktracesButton = new Button(container, SWT.CHECK);
                anonymizeStacktracesButton.setText("Anonymize stacktraces");
                clearMessagesButton = new Button(container, SWT.CHECK);
                clearMessagesButton.setText("Clear messages");

            }
            {
                Composite feedback = new Composite(container, SWT.NONE);
                // Color color = feedback.getDisplay().getSystemColor(SWT.COLOR_RED);
                // feedback.setBackground(color);
                glFactory.applyTo(feedback);
                gdFactory.grab(true, true).applyTo(feedback);
                {
                    Link feedbackLink = new Link(feedback, SWT.NONE);
                    gdFactory.align(SWT.BEGINNING, SWT.END).applyTo(feedbackLink);
                    feedbackLink.setText("<a>Learn more...</a>");
                    feedbackLink.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            BrowserUtils
                            .openInExternalBrowser("https://docs.google.com/document/d/14vRLXcgSwy0rEbpJArsR_FftOJW1SjWUAmZuzc2O8YI/pub");
                        }
                    });
                }

                {
                    Link feedbackLink = new Link(feedback, SWT.NONE);
                    gdFactory.align(SWT.END, SWT.END).applyTo(feedbackLink);
                    feedbackLink.setText("<a>Provide feedback...</a>");
                    feedbackLink.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            BrowserUtils
                            .openInExternalBrowser("https://docs.google.com/a/codetrails.com/forms/d/1wd9AzydLv_TMa7ZBXHO7zQIhZjZCJRNMed-6J4fVNsc/viewform");
                        }
                    });
                }

            }
            setControl(container);
        }

        public void performFinish() {
            String mode = (String) ((IStructuredSelection) actionComboViewer.getSelection()).getFirstElement();
            prefs.setMode(mode);
            prefs.setName(nameTxt.getText());
            prefs.setEmail(emailTxt.getText());
            prefs.setAnonymize(String.valueOf(anonymizeStacktracesButton.getSelection()));
            prefs.setClearMsg(String.valueOf(clearMessagesButton.getSelection()));

        }
    }

    class JsonPage extends WizardPage {

        private TableViewer tableViewer;
        private StyledText messageText;

        protected JsonPage() {
            super(JsonPage.class.getName());
            setTitle("Review your data");
            setDescription("This is what get's send to the team.");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().numColumns(3).applyTo(container);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
            {
                Composite tableComposite = new Composite(container, SWT.NONE);
                tableViewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
                        | SWT.FULL_SELECTION | SWT.BORDER);
                GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).span(1, 2).grab(true, true)
                .applyTo(tableComposite);
                TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
                column.setLabelProvider(new ColumnLabelProvider() {

                    @Override
                    public String getText(Object element) {
                        IStatus event = (IStatus) element;
                        return event.getMessage();
                    }
                });
                TableColumnLayout tableColumnLayout = new TableColumnLayout();
                tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(100));
                tableComposite.setLayout(tableColumnLayout);
                tableViewer.setContentProvider(new ObservableListContentProvider());
                tableViewer.setInput(errors);
                tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        if (!event.getSelection().isEmpty()) {
                            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                            IStatus selected = (IStatus) selection.getFirstElement();
                            messageText.setText(GsonUtil.serialize(Stacktraces.createDto(selected, prefs)));
                        }
                    }
                });
            }
            {
                Composite messageComposite = new Composite(container, SWT.NONE);
                GridLayoutFactory.fillDefaults().applyTo(messageComposite);
                Label messageLabel = new Label(messageComposite, SWT.NONE);
                messageLabel.setText("Message:");
                GridDataFactory.fillDefaults().applyTo(messageLabel);
                messageText = new StyledText(messageComposite, SWT.V_SCROLL | SWT.BORDER);
                messageText.setEditable(false);
                GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 300).grab(true, false).applyTo(messageText);

                GridDataFactory.fillDefaults().span(2, 1).applyTo(messageComposite);
                if (!errors.isEmpty()) {
                    tableViewer.setSelection(new StructuredSelection(tableViewer.getElementAt(0)));
                }
            }
            {
                Composite commentComposite = new Composite(container, SWT.NONE);
                GridLayoutFactory.fillDefaults().applyTo(commentComposite);
                Label commentLabel = new Label(commentComposite, SWT.NONE);
                commentLabel.setText("Comment:");
                GridDataFactory.fillDefaults().applyTo(commentLabel);
                StyledText commentText = new StyledText(commentComposite, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
                GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 75).applyTo(commentText);
                GridDataFactory.fillDefaults().span(2, 1).applyTo(commentComposite);
            }
            setControl(container);
        }
    }

    private StacktracesRcpPreferences prefs;
    private StacktracePage page = new StacktracePage();
    private IObservableList errors;

    public StacktraceWizard(StacktracesRcpPreferences prefs, IObservableList errors) {
        this.prefs = prefs;
        this.errors = errors;
    }

    @Override
    public void addPages() {
        setWindowTitle("We noticed an error...");
        ImageDescriptor img = ImageDescriptor.createFromFile(getClass(), "/icons/wizban/stackframes_wiz.gif");
        setDefaultPageImageDescriptor(img);
        addPage(page);
        addPage(new JsonPage());
    }

    @Override
    public boolean performFinish() {
        page.performFinish();
        return true;
    }

}

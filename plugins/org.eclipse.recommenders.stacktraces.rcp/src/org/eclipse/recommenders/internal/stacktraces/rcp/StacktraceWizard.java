/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_INFORMATION;
import static org.eclipse.recommenders.internal.stacktraces.rcp.StacktracesRcpPreferences.*;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;

public class StacktraceWizard extends Wizard implements IWizard {

    class StacktracePage extends WizardPage {

        private ComboViewer v;
        private Text emailTxt;
        private Text nameTxt;

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
                v = new ComboViewer(personal, SWT.READ_ONLY);
                v.setContentProvider(ArrayContentProvider.getInstance());
                v.setInput(Lists.newArrayList("ask", "ignore", "silent"));
                v.setLabelProvider(new LabelProvider() {
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
                v.setSelection(new StructuredSelection(prefs.mode));
                gdFactory.applyTo(v.getControl());
            }
            setControl(container);
        }

        public void performFinish() {
            String mode = Selections.<String>getFirstSelected(v.getSelection()).orNull();
            prefs.setMode(mode);
            prefs.setName(nameTxt.getText());
            prefs.setEmail(emailTxt.getText());
        }
    }

    class JsonPage extends WizardPage {

        protected JsonPage() {
            super(JsonPage.class.getName());
            setTitle("Review your data");
            setDescription("This is what get's send to the team.");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            GridLayoutFactory.fillDefaults().applyTo(container);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
            final StyledText text = new StyledText(container, SWT.V_SCROLL);
            GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 300).grab(true, false).applyTo(text);
            text.setText(error);
            setControl(text);
        }
    }

    StacktracesRcpPreferences prefs;
    String error;
    StacktracePage page = new StacktracePage();

    public StacktraceWizard(StacktracesRcpPreferences prefs, String error) {
        this.prefs = prefs;
        this.error = error;
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

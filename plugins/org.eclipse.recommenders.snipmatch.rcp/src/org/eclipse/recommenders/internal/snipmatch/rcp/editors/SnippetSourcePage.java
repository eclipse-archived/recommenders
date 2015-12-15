/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Marcel Bruch - Initial design and API
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static org.eclipse.jface.databinding.swt.WidgetProperties.text;
import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.HELP_URL;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.rcp.SnippetEditorInput;
import org.eclipse.recommenders.utils.rcp.Browsers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class SnippetSourcePage extends FormPage {

    private ISnippet snippet;
    private ScrolledForm form;
    private Text textWidget;
    private AbstractFormPart codePart;
    private DataBindingContext context;

    public SnippetSourcePage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        form = managedForm.getForm();

        createHeader(form);

        toolkit.decorateFormHeading(form.getForm());

        Composite body = form.getBody();
        toolkit.paintBordersFor(body);
        body.setLayout(new FillLayout(SWT.HORIZONTAL));

        codePart = new AbstractFormPart() {
            @Override
            public void initialize(IManagedForm managedForm) {
                super.initialize(managedForm);
                textWidget = managedForm.getToolkit().createText(managedForm.getForm().getBody(), snippet.getCode(),
                        SWT.WRAP | SWT.MULTI);
                textWidget.setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
            }

            @Override
            public void refresh() {
                context.updateTargets();
                super.refresh();
                updateMessage();
            }

            @Override
            public void commit(boolean onSave) {
                if (onSave) {
                    super.commit(onSave);
                }
            }

            @Override
            public void dispose() {
                context.dispose();
                super.dispose();
            }
        };
        managedForm.addPart(codePart);
        context = createDataBindingContext();
    }

    private void createHeader(ScrolledForm form) {
        form.setText(Messages.EDITOR_TITLE_RAW_SOURCE);
        SharedImages sharedImages = InjectionService.getInstance().getInjector().getInstance(SharedImages.class);

        Action showHelpAction = new Action(Messages.EDITOR_TOOLBAR_ITEM_HELP,
                sharedImages.getDescriptor(SharedImages.Images.ELCL_HELP)) {
            @Override
            public void run() {
                Browsers.tryOpenInExternalBrowser(HELP_URL);
            };
        };
        EditorUtils.addActionToForm(form, showHelpAction, Messages.EDITOR_TOOLBAR_ITEM_HELP);
    }

    @Override
    public void setFocus() {
        super.setFocus();
        textWidget.setFocus();
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        snippet = ((SnippetEditorInput) input).getSnippet();
        registerEditorInputListener();
        super.init(site, input);
    }

    private void registerEditorInputListener() {

        getEditor().addPropertyListener(new IPropertyListener() {

            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == PROP_INPUT) {
                    setInputWithNotify(getEditor().getEditorInput());
                }
            }
        });

    }

    @Override
    protected void setInputWithNotify(IEditorInput input) {
        snippet = ((SnippetEditorInput) input).getSnippet();
        context.dispose();
        context = createDataBindingContext();
        super.setInputWithNotify(input);
    }

    private void updateMessage() {
        String sourceValid = SnippetSourceValidator.isSourceValid(textWidget.getText());
        if (sourceValid.isEmpty()) {
            form.setMessage(null, IMessageProvider.NONE);
        } else {
            form.setMessage(sourceValid, IMessageProvider.ERROR);
        }
    }

    private DataBindingContext createDataBindingContext() {
        DataBindingContext ctx = new DataBindingContext();

        IObservableValue snippetBeanCode = BeanProperties.value(Snippet.class, "code", String.class).observe(snippet); //$NON-NLS-1$
        IObservableValue textWidgetCode = text(SWT.Modify).observe(textWidget);

        ctx.bindValue(textWidgetCode, snippetBeanCode);

        snippetBeanCode.addChangeListener(new IChangeListener() {

            @Override
            public void handleChange(ChangeEvent event) {
                if (!textWidget.getText().equals(snippet.getCode())) {
                    codePart.markStale();
                } else {
                    codePart.markDirty();
                }
                updateMessage();
            }
        });
        return ctx;
    }
}

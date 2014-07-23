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

import static org.eclipse.core.databinding.beans.PojoProperties.value;
import static org.eclipse.jface.databinding.swt.WidgetProperties.text;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class SnippetSourcePage extends FormPage {

    private ISnippet snippet;
    private Text txtCode;
    private DataBindingContext ctx;
    private ScrolledForm form;

    public SnippetSourcePage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        form = managedForm.getForm();
        form.setText(getTitle());
        Composite body = form.getBody();
        toolkit.decorateFormHeading(form.getForm());
        toolkit.paintBordersFor(body);
        Composite formBody = managedForm.getForm().getBody();
        formBody.setLayout(GridLayoutFactory.fillDefaults().create());

        txtCode = managedForm.getToolkit().createText(formBody, "New Text", SWT.MULTI); //$NON-NLS-1$
        txtCode.setEditable(true);
        txtCode.setFont(JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(txtCode);

        initDataBindings();
        form.reflow(true);
    }

    private void initDataBindings() {
        ctx = new DataBindingContext();

        // code
        IObservableValue wpTxtCode = text(SWT.Modify).observe(txtCode);
        IObservableValue ppCode = value(Snippet.class, "code", String.class).observe(snippet); //$NON-NLS-1$
        ctx.bindValue(wpTxtCode, ppCode, null, null);

        for (Object o : ctx.getValidationStatusProviders()) {
            if (o instanceof Binding) {
                ((Binding) o).getTarget().addChangeListener(new IChangeListener() {

                    @Override
                    public void handleChange(org.eclipse.core.databinding.observable.ChangeEvent event) {
                        ((SnippetEditor) getEditor()).setDirty(true);
                        String sourceValid = SnippetSourceValidator.isSourceValid(txtCode.getText());
                        if (sourceValid.isEmpty()) {
                            form.setMessage(null, IMessageProvider.NONE);
                        } else {
                            form.setMessage(sourceValid, IMessageProvider.ERROR);
                        }
                    }
                });
            }
        }

    }

    @Override
    public void init(IEditorSite site, IEditorInput input) {
        snippet = ((SnippetEditorInput) input).getSnippet();
        super.init(site, input);
    }

    public void update() {
        ctx.dispose();
        initDataBindings();
    }

    @Override
    public void dispose() {
        super.dispose();
        // TODO: ctx is sometimes null. this is a workaround, see that ctx is
        // always initialized.
        if (ctx != null) {
            ctx.dispose();
        }
    }
}

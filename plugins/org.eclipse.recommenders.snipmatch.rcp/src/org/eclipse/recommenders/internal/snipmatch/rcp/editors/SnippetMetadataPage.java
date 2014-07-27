/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Stefan Prisca - initial API and implementation
 *      Marcel Bruch - changed to use jface databinding
 *      Olav Lenz - clean up metadata page.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.core.databinding.beans.BeanProperties.value;
import static org.eclipse.jface.databinding.swt.WidgetProperties.*;
import static org.eclipse.jface.databinding.viewers.ViewerProperties.singleSelection;
import static org.eclipse.jface.fieldassist.FieldDecorationRegistry.DEC_INFORMATION;
import static org.eclipse.recommenders.internal.snipmatch.rcp.Messages.*;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.property.value.SelfValueProperty;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.recommenders.internal.snipmatch.rcp.Messages;
import org.eclipse.recommenders.rcp.utils.ObjectToBooleanConverter;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.LocationConstraint;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.rcp.SnippetEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
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

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class SnippetMetadataPage extends FormPage {

    private static final String[] SNIPMATCH_LOCATION_CONSTRAINTS = { SNIPMATCH_CONTEXT_FILE, SNIPMATCH_CONTEXT_JAVA,
        SNIPMATCH_CONTEXT_JAVA_STATEMENTS, SNIPMATCH_CONTEXT_JAVA_MEMBERS, SNIPMATCH_CONTEXT_JAVADOC };

    private ISnippet snippet;

    private AbstractFormPart contentsPart;

    private Text txtName;
    private Text txtDescription;
    private Combo comboContext;
    private Text txtUuid;

    private ListViewer listViewerExtraSearchTerms;
    private ListViewer listViewerTags;

    private Composite btnContainerExtraSearchTerms;
    private Composite btnContainerTags;

    private Button btnAddExtraSearchTerm;
    private Button btnAddTag;
    private Button btnRemoveExtraSearchTerm;
    private Button btnRemoveTag;

    private IObservableList ppExtraSearchTerms;
    private IObservableList ppTags;
    private DataBindingContext context;

    private final Image decorationImage = FieldDecorationRegistry.getDefault()
            .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

    public SnippetMetadataPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        ScrolledForm form = managedForm.getForm();
        form.setText(Messages.EDITOR_TITLE_METADATA);
        Composite body = form.getBody();
        toolkit.decorateFormHeading(form.getForm());
        toolkit.paintBordersFor(body);
        managedForm.getForm().getBody().setLayout(new GridLayout(3, false));

        contentsPart = new AbstractFormPart() {

            @Override
            public void initialize(IManagedForm managedForm) {
                super.initialize(managedForm);

                Label lblName = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPET_NAME, SWT.NONE);
                lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

                int horizontalIndent = decorationImage.getBounds().width + 2;

                txtName = managedForm.getToolkit().createText(managedForm.getForm().getBody(), snippet.getName(),
                        SWT.NONE);
                txtName.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
                        .span(2, 1).indent(horizontalIndent, 0).create());

                final ControlDecoration nameDecoration = new ControlDecoration(txtName, SWT.LEFT);
                nameDecoration.setDescriptionText(Messages.ERROR_SNIPPET_NAME_CANNOT_BE_EMPTY);
                nameDecoration.setImage(decorationImage);
                nameDecoration.setMarginWidth(1);

                txtName.addModifyListener(new ModifyListener() {
                    @Override
                    public void modifyText(ModifyEvent arg0) {
                        if (isNullOrEmpty(txtName.getText())) {
                            nameDecoration.show();
                        } else {
                            nameDecoration.hide();
                        }
                    }
                });

                Label lblDescription = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPET_DESCRIPTION, SWT.NONE);
                lblDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

                txtDescription = managedForm.getToolkit().createText(managedForm.getForm().getBody(),
                        snippet.getDescription(), SWT.NONE);
                txtDescription.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                        .grab(true, false).span(2, 1).indent(horizontalIndent, 0).create());

                Label lblContext = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPET_CONTEXT, SWT.NONE);
                lblContext.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

                comboContext = new Combo(managedForm.getForm().getBody(), SWT.DROP_DOWN | SWT.READ_ONLY);
                managedForm.getToolkit().adapt(comboContext, true, true);
                comboContext.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
                        .span(2, 1).indent(horizontalIndent, 0).create());

                final ControlDecoration contextErrorDecoration = new ControlDecoration(comboContext, SWT.LEFT);
                contextErrorDecoration.setDescriptionText(Messages.ERROR_SNIPPET_CONTEXT_CANNOT_BE_EMPTY + "\n"
                        + Messages.EDITOR_DESCRIPTION_CONTEXT);
                contextErrorDecoration.setImage(decorationImage);
                contextErrorDecoration.setMarginWidth(1);

                final ControlDecoration contextDescriptionDecoration = new ControlDecoration(comboContext, SWT.LEFT);
                FieldDecoration infoDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                        DEC_INFORMATION);
                contextDescriptionDecoration.setImage(infoDecoration.getImage());
                contextDescriptionDecoration.setDescriptionText(Messages.EDITOR_DESCRIPTION_CONTEXT);

                comboContext.addModifyListener(new ModifyListener() {

                    @Override
                    public void modifyText(ModifyEvent e) {
                        if (comboContext.getSelectionIndex() == -1) {
                            contextErrorDecoration.show();
                            contextDescriptionDecoration.hide();
                        } else {
                            contextErrorDecoration.hide();
                            contextDescriptionDecoration.show();
                        }
                    }

                });

                comboContext.setItems(SNIPMATCH_LOCATION_CONSTRAINTS);
                comboContext.select(snippet.getLocationConstraint().getIndex());

                Label lblExtraSearchTerms = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPETS_EXTRA_SEARCH_TERMS, SWT.NONE);
                lblExtraSearchTerms.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

                listViewerExtraSearchTerms = new ListViewer(managedForm.getForm().getBody(), SWT.BORDER | SWT.V_SCROLL);
                List lstExtraSearchTerm = listViewerExtraSearchTerms.getList();
                lstExtraSearchTerm.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                        .indent(horizontalIndent, 0).create());

                btnContainerExtraSearchTerms = managedForm.getToolkit().createComposite(
                        managedForm.getForm().getBody(), SWT.NONE);
                btnContainerExtraSearchTerms.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
                managedForm.getToolkit().paintBordersFor(btnContainerExtraSearchTerms);
                btnContainerExtraSearchTerms.setLayout(new GridLayout(1, false));

                btnAddExtraSearchTerm = managedForm.getToolkit().createButton(btnContainerExtraSearchTerms,
                        Messages.EDITOR_BUTTON_ADD_EXTRASEARCH_TERM, SWT.NONE);
                btnAddExtraSearchTerm.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        createExtraSearchTermInputDialog(btnContainerExtraSearchTerms.getShell()).open();
                    }
                });
                btnAddExtraSearchTerm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                btnRemoveExtraSearchTerm = managedForm.getToolkit().createButton(btnContainerExtraSearchTerms,
                        Messages.EDITOR_BUTTON_REMOVE_EXTRA_SEARCH_TERM, SWT.NONE);
                btnRemoveExtraSearchTerm.setEnabled(false);
                btnRemoveExtraSearchTerm.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Optional<String> o = Selections.getFirstSelected(listViewerExtraSearchTerms);
                        if (o.isPresent()) {
                            ppExtraSearchTerms.remove(o.get());
                        }
                    }
                });
                btnRemoveExtraSearchTerm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                Label lblTag = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPETS_TAG, SWT.NONE);
                lblTag.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

                listViewerTags = new ListViewer(managedForm.getForm().getBody(), SWT.BORDER | SWT.V_SCROLL);
                List lstTags = listViewerTags.getList();
                lstTags.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).indent(horizontalIndent, 0)
                        .create());

                btnContainerTags = managedForm.getToolkit().createComposite(managedForm.getForm().getBody(), SWT.NONE);
                btnContainerTags.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
                managedForm.getToolkit().paintBordersFor(btnContainerExtraSearchTerms);
                btnContainerTags.setLayout(new GridLayout(1, false));

                btnAddTag = managedForm.getToolkit().createButton(btnContainerTags, Messages.EDITOR_BUTTON_ADD_TAGS,
                        SWT.NONE);
                btnAddTag.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        createTagInputDialog(btnContainerTags.getShell()).open();
                    }
                });
                btnAddTag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                btnRemoveTag = managedForm.getToolkit().createButton(btnContainerTags,
                        Messages.EDITOR_BUTTON_REMOVE_TAGS, SWT.NONE);
                btnRemoveTag.setEnabled(false);
                btnRemoveTag.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Optional<String> o = Selections.getFirstSelected(listViewerTags);
                        if (o.isPresent()) {
                            ppTags.remove(o.get());
                        }
                    }
                });
                btnRemoveTag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

                Label lblUuid = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPET_UUID, SWT.NONE);
                lblUuid.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

                txtUuid = managedForm.getToolkit().createText(managedForm.getForm().getBody(),
                        snippet.getUuid().toString(), SWT.READ_ONLY);
                txtUuid.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).indent(horizontalIndent, 0)
                        .create());
            }

            @Override
            public void commit(boolean onSave) {
                if (onSave) {
                    super.commit(onSave);
                }
            }

            @Override
            public void refresh() {
                context.updateTargets();
                super.refresh();
            }

        };

        managedForm.addPart(contentsPart);
        context = createDataBindingContext();
    }

    private InputDialog createExtraSearchTermInputDialog(Shell shell) {
        IInputValidator validator = new IInputValidator() {

            @Override
            public String isValid(String newText) {
                if (isNullOrEmpty(newText)) {
                    return ""; //$NON-NLS-1$
                }
                if (snippet.getExtraSearchTerms().contains(newText)) {
                    return Messages.DIALOG_VALIDATOR_EXTRA_SEARCH_TERM_ALREADY_ADDED;
                }
                return null;
            }
        };
        return new InputDialog(shell, Messages.DIALOG_TITLE_ENTER_NEW_EXTRA_SEARCH_TERM,
                Messages.DIALOG_MESSAGE_ENTER_NEW_EXTRA_SEARCH_TERM, "", validator) { //$NON-NLS-1$
            @Override
            protected void okPressed() {
                ppExtraSearchTerms.add(getValue());
                super.okPressed();
            }
        };
    }

    private InputDialog createTagInputDialog(Shell shell) {
        IInputValidator validator = new IInputValidator() {

            @Override
            public String isValid(String newText) {
                if (isNullOrEmpty(newText)) {
                    return ""; //$NON-NLS-1$
                }
                if (snippet.getTags().contains(newText)) {
                    return Messages.DIALOG_VALIDATOR_TAG_ALREADY_ADDED;
                }
                return null;
            }
        };
        return new InputDialog(shell, Messages.DIALOG_TITLE_ENTER_NEW_TAG, Messages.DIALOG_MESSAGE_ENTER_NEW_TAG,
                "", validator) { //$NON-NLS-1$
            @Override
            protected void okPressed() {
                ppTags.add(getValue());
                super.okPressed();
            }
        };
    }

    private DataBindingContext createDataBindingContext() {
        DataBindingContext context = new DataBindingContext();

        // name
        IObservableValue wpTxtNameText = text(SWT.Modify).observe(txtName);
        IObservableValue ppName = value(Snippet.class, "name", String.class).observe(snippet); //$NON-NLS-1$
        context.bindValue(wpTxtNameText, ppName, null, null);
        ppName.addChangeListener(new IChangeListener() {
            @Override
            public void handleChange(ChangeEvent event) {
                if (!txtName.getText().equals(snippet.getName())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }
        });

        // description
        IObservableValue wpTxtDescriptionText = text(SWT.Modify).observe(txtDescription);
        IObservableValue ppDescription = value(Snippet.class, "description", String.class).observe(snippet); //$NON-NLS-1$
        context.bindValue(wpTxtDescriptionText, ppDescription, null, null);
        ppDescription.addChangeListener(new IChangeListener() {
            @Override
            public void handleChange(ChangeEvent event) {
                if (!txtDescription.getText().equals(snippet.getDescription())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }
        });

        // context
        IObservableValue wpTxtContext = WidgetProperties.singleSelectionIndex().observe(comboContext);
        IObservableValue ppContext = value(Snippet.class, "locationConstraint", LocationConstraint.class).observe(snippet); //$NON-NLS-1$
        context.bindValue(wpTxtContext, ppContext, new UpdateValueStrategy() {
            @Override
            public Object convert(Object value) {
                return LocationConstraint.valueOf((Integer) value);
            }
        }, new UpdateValueStrategy() {
            @Override
            public Object convert(Object value) {
                return ((LocationConstraint) value).getIndex();
            }
        });
        ppContext.addChangeListener(new IChangeListener() {
            @Override
            public void handleChange(ChangeEvent event) {
                if (!LocationConstraint.valueOf(comboContext.getSelectionIndex()).equals(
                        snippet.getLocationConstraint())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }
        });

        // Extra search terms
        ppExtraSearchTerms = BeanProperties.list(Snippet.class, "extraSearchTerms", String.class).observe(snippet); //$NON-NLS-1$
        ViewerSupport.bind(listViewerExtraSearchTerms, ppExtraSearchTerms, new SelfValueProperty(String.class));
        ppExtraSearchTerms.addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                if (!Arrays.equals(listViewerExtraSearchTerms.getList().getItems(), snippet.getExtraSearchTerms()
                        .toArray())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }
        });

        // tags
        ppTags = BeanProperties.list(Snippet.class, "tags", String.class).observe(snippet); //$NON-NLS-1$
        ViewerSupport.bind(listViewerTags, ppTags, new SelfValueProperty(String.class));
        ppTags.addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {
                if (!Arrays.equals(listViewerTags.getList().getItems(), snippet.getTags().toArray())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }
        });

        // uuid
        IObservableValue wpUuidText = text(SWT.Modify).observe(txtUuid);
        IObservableValue ppUuid = value(Snippet.class, "uuid", UUID.class).observe(snippet); //$NON-NLS-1$
        context.bindValue(wpUuidText, ppUuid, null, null);
        ppUuid.addChangeListener(new IChangeListener() {
            @Override
            public void handleChange(ChangeEvent event) {
                if (!txtUuid.getText().equals(snippet.getUuid().toString())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }
        });

        // enable buttons
        IObservableValue vpExtraSearchTermsSelection = singleSelection().observe(listViewerExtraSearchTerms);
        IObservableValue wpBtnRemoveExtraSearchTermEnable = enabled().observe(btnRemoveExtraSearchTerm);

        UpdateValueStrategy strategy = new UpdateValueStrategy();
        strategy.setConverter(new ObjectToBooleanConverter());
        context.bindValue(vpExtraSearchTermsSelection, wpBtnRemoveExtraSearchTermEnable, strategy, null);

        IObservableValue vpTagSelection = singleSelection().observe(listViewerTags);
        IObservableValue wpBtnRemoveTagsEnable = enabled().observe(btnRemoveTag);
        context.bindValue(vpTagSelection, wpBtnRemoveTagsEnable, strategy, null);

        return context;
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

    @Override
    public void dispose() {
        context.dispose();
        super.dispose();
    }
}

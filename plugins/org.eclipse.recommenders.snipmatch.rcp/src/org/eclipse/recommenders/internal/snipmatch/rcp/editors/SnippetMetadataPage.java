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
import static org.eclipse.recommenders.internal.snipmatch.rcp.JavaEditorSearchContext.resolve;
import static org.eclipse.recommenders.rcp.SharedImages.Images.OBJ_JAR;
import static org.eclipse.recommenders.snipmatch.Location.*;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.property.value.SelfValueProperty;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.snipmatch.rcp.Messages;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.IDependencyListener;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.utils.ObjectToBooleanConverter;
import org.eclipse.recommenders.rcp.utils.Selections;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.Location;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class SnippetMetadataPage extends FormPage {

    private static final Location[] SNIPMATCH_LOCATIONS = { FILE, JAVA, JAVA_STATEMENTS, JAVA_TYPE_MEMBERS, JAVADOC };

    private ISnippet snippet;

    private AbstractFormPart contentsPart;

    private Text txtName;
    private Text txtDescription;
    private ComboViewer comboLocation;
    private Text txtUuid;

    private ListViewer listViewerDependencies;
    private ListViewer listViewerExtraSearchTerms;
    private ListViewer listViewerTags;

    private Composite btnContainerDependencies;
    private Composite btnContainerExtraSearchTerms;
    private Composite btnContainerTags;

    private Button btnAddDependency;
    private Button btnAddExtraSearchTerm;
    private Button btnAddTag;

    private Button btnRemoveDependency;
    private Button btnRemoveExtraSearchTerm;
    private Button btnRemoveTag;

    private IObservableSet ppDependencies;
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

                Label lblLocation = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPET_LOCATION, SWT.NONE);
                lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

                comboLocation = new ComboViewer(managedForm.getForm().getBody(), SWT.DROP_DOWN | SWT.READ_ONLY);

                managedForm.getToolkit().adapt(comboLocation.getCombo(), true, true);
                comboLocation.setContentProvider(ArrayContentProvider.getInstance());
                comboLocation.setInput(SNIPMATCH_LOCATIONS);
                comboLocation.setLabelProvider(new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        if (element instanceof Location) {
                            Location location = (Location) element;
                            switch (location) {
                            case FILE:
                                return Messages.SNIPMATCH_LOCATION_FILE;
                            case JAVA:
                                return Messages.SNIPMATCH_LOCATION_JAVA;
                            case JAVA_STATEMENTS:
                                return Messages.SNIPMATCH_LOCATION_JAVA_STATEMENTS;
                            case JAVA_TYPE_MEMBERS:
                                return Messages.SNIPMATCH_LOCATION_JAVA_MEMBERS;
                            case JAVADOC:
                                return Messages.SNIPMATCH_LOCATION_JAVADOC;
                            default:
                                break;
                            }
                        }
                        return super.getText(element);
                    }
                });
                comboLocation.getCombo().setLayoutData(
                        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).span(2, 1)
                        .indent(horizontalIndent, 0).create());

                final ControlDecoration locationErrorDecoration = new ControlDecoration(comboLocation.getCombo(),
                        SWT.LEFT);
                locationErrorDecoration.setDescriptionText(Messages.ERROR_SNIPPET_LOCATION_CANNOT_BE_EMPTY + "\n" //$NON-NLS-1$
                        + Messages.EDITOR_DESCRIPTION_LOCATION);
                locationErrorDecoration.setImage(decorationImage);
                locationErrorDecoration.setMarginWidth(1);

                final ControlDecoration locationDescriptionDecoration = new ControlDecoration(comboLocation.getCombo(),
                        SWT.LEFT);
                FieldDecoration infoDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                        DEC_INFORMATION);
                locationDescriptionDecoration.setImage(infoDecoration.getImage());
                locationDescriptionDecoration.setDescriptionText(Messages.EDITOR_DESCRIPTION_LOCATION);
                locationDescriptionDecoration.setMarginWidth(1);

                comboLocation.addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        if (event.getSelection().isEmpty()) {
                            locationErrorDecoration.show();
                            locationDescriptionDecoration.hide();
                        } else {
                            locationErrorDecoration.hide();
                            locationDescriptionDecoration.show();
                        }
                    }

                });
                comboLocation.setSelection(new StructuredSelection(snippet.getLocation()));

                Label lblExtraSearchTerms = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPETS_EXTRA_SEARCH_TERMS, SWT.NONE);
                lblExtraSearchTerms.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

                listViewerExtraSearchTerms = new ListViewer(managedForm.getForm().getBody(), SWT.BORDER | SWT.V_SCROLL);
                List lstExtraSearchTerm = listViewerExtraSearchTerms.getList();
                lstExtraSearchTerm.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                        .indent(horizontalIndent, 0).create());

                final ControlDecoration extraSearchTermsDescriptionDecoration = new ControlDecoration(
                        listViewerExtraSearchTerms.getList(), SWT.TOP | SWT.LEFT);
                extraSearchTermsDescriptionDecoration.setImage(infoDecoration.getImage());
                extraSearchTermsDescriptionDecoration
                .setDescriptionText(Messages.EDITOR_DESCRIPTION_EXTRA_SEARCH_TERMS);
                extraSearchTermsDescriptionDecoration.setMarginWidth(1);

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

                final ControlDecoration tagsDescriptionDecoration = new ControlDecoration(listViewerTags.getList(),
                        SWT.TOP | SWT.LEFT);
                tagsDescriptionDecoration.setImage(infoDecoration.getImage());
                tagsDescriptionDecoration.setDescriptionText(Messages.EDITOR_DESCRIPTION_TAGS);
                tagsDescriptionDecoration.setMarginWidth(1);

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

                Label lblDependencies = managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
                        Messages.EDITOR_LABEL_SNIPPET_DEPENENCIES, SWT.NONE);
                lblDependencies.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));

                listViewerDependencies = new ListViewer(managedForm.getForm().getBody(), SWT.BORDER | SWT.V_SCROLL);
                List lstDependencies = listViewerDependencies.getList();
                lstDependencies.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
                        .indent(horizontalIndent, 0).create());

                final ControlDecoration dependencyDescriptionDecoration = new ControlDecoration(
                        listViewerDependencies.getList(), SWT.TOP | SWT.LEFT);
                dependencyDescriptionDecoration.setImage(infoDecoration.getImage());
                dependencyDescriptionDecoration.setDescriptionText(Messages.EDITOR_DESCRIPTION_DEPENDENCIES);
                dependencyDescriptionDecoration.setMarginWidth(1);

                btnContainerDependencies = managedForm.getToolkit().createComposite(managedForm.getForm().getBody(),
                        SWT.NONE);
                btnContainerDependencies.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
                managedForm.getToolkit().paintBordersFor(btnContainerDependencies);
                btnContainerDependencies.setLayout(new GridLayout(1, false));

                btnAddDependency = managedForm.getToolkit().createButton(btnContainerDependencies,
                        Messages.EDITOR_BUTTON_ADD_DEPENDENCY, SWT.NONE);
                btnAddDependency.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Shell shell = btnContainerDependencies.getShell();
                        ProjectCoordinateSelectionDialog dialog = new ProjectCoordinateSelectionDialog(shell);
                        dialog.setInitialPattern(""); //$NON-NLS-1$
                        dialog.open();

                        Object[] result = dialog.getResult();
                        if (result != null) {
                            for (Object object : result) {
                                ppDependencies.add(object);
                            }
                        }
                    }
                });
                btnAddDependency.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                btnRemoveDependency = managedForm.getToolkit().createButton(btnContainerDependencies,
                        Messages.EDITOR_BUTTON_REMOVE_TAGS, SWT.NONE);
                btnRemoveDependency.setEnabled(false);
                btnRemoveDependency.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Optional<String> o = Selections.getFirstSelected(listViewerDependencies);
                        if (o.isPresent()) {
                            ppDependencies.remove(o.get());
                        }
                    }
                });
                btnRemoveDependency.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

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

    class ProjectCoordinateSelectionDialog extends FilteredItemsSelectionDialog {

        private Collection<ProjectCoordinate> pcs;
        private final SharedImages images;
        private LabelProvider labelProvider;

        public ProjectCoordinateSelectionDialog(Shell shell) {
            super(shell, true);
            setTitle(Messages.DIALOG_TITLE_SELECT_DEPENDENCY);

            IDependencyListener dependencyListener = InjectionService.getInstance().requestInstance(
                    IDependencyListener.class);

            images = InjectionService.getInstance().requestInstance(SharedImages.class);

            ImmutableSet<DependencyInfo> dependencies = dependencyListener.getDependencies();
            this.pcs = changeVersionsToZero(resolve(dependencies));

            labelProvider = new LabelProvider() {
                @Override
                public String getText(Object element) {
                    if (element == null) {
                        return "";
                    }
                    if (element instanceof ProjectCoordinate) {
                        return getStringForDependency((ProjectCoordinate) element);
                    }
                    return element.toString();
                }

                @Override
                public Image getImage(Object element) {
                    return images.getImage(OBJ_JAR);
                }

            };
            setListLabelProvider(labelProvider);
            setDetailsLabelProvider(labelProvider);
        }

        @Override
        protected Control createContents(Composite parent) {
            Control control = super.createContents(parent);

            // final Text text = cast(getPatternControl());
            //
            // final IInputValidator validator = new IInputValidator() {
            //
            // @Override
            // public String isValid(String newText) {
            // if (isNullOrEmpty(newText)) {
            //                        return ""; //$NON-NLS-1$
            // }
            // ProjectCoordinate pc = ProjectCoordinate.valueOf(newText);
            // if (snippet.getNeededDependencies().contains(pc)) {
            // return Messages.DIALOG_VALIDATOR_DEPENDENCY_ALREADY_ADDED;
            // }
            //
            // return null;
            // }
            // };
            //
            // text.addModifyListener(new ModifyListener() {
            //
            // @Override
            // public void modifyText(ModifyEvent e) {
            // if (getResult().length == 0) {
            // if (validator.isValid(text.getText()) != null) {
            // getOkButton().setEnabled(true);
            // } else {
            // getOkButton().setEnabled(false);
            // }
            // }
            // }
            // });

            return control;
        }

        private Collection<ProjectCoordinate> changeVersionsToZero(Set<ProjectCoordinate> resolved) {
            Set<ProjectCoordinate> result = Sets.newHashSet();
            for (ProjectCoordinate projectCoordinate : resolved) {
                result.add(new ProjectCoordinate(projectCoordinate.getGroupId(), projectCoordinate.getArtifactId(),
                        "0.0.0")); //$NON-NLS-1$
            }
            return result;
        }

        @Override
        protected Control createExtendedContentArea(Composite parent) {
            return null;
        }

        private static final String DIALOG_SETTINGS = "ProjectCoordinateSelectionDialog"; //$NON-NLS-1$

        @Override
        protected IDialogSettings getDialogSettings() {
            IDialogSettings settings = JavaPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

            if (settings == null) {
                settings = JavaPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
            }

            return settings;
        }

        @Override
        protected IStatus validateItem(Object item) {
            return Status.OK_STATUS;
        }

        @Override
        protected ItemsFilter createFilter() {
            return new ItemsFilter() {
                @Override
                public boolean matchItem(Object item) {
                    return matches(item.toString());
                }

                @Override
                public String getPattern() {
                    String pattern = super.getPattern();
                    if (pattern.equals("")) { //$NON-NLS-1$
                        return "?"; //$NON-NLS-1$
                    }
                    return pattern;
                }

                @Override
                public boolean isConsistentItem(Object item) {
                    return true;
                }
            };
        }

        @Override
        protected Comparator getItemsComparator() {
            return new Comparator() {
                @Override
                public int compare(Object arg0, Object arg1) {
                    return arg0.toString().compareTo(arg1.toString());
                }
            };
        }

        @Override
        protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
                IProgressMonitor progressMonitor) throws CoreException {
            for (ProjectCoordinate pc : pcs) {
                contentProvider.add(pc, itemsFilter);
            }
        }

        @Override
        public String getElementName(Object item) {
            return labelProvider.getText(item);
        }
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

    private InputDialog createDependencyInputDialog(Shell shell) {
        IInputValidator validator = new IInputValidator() {

            @Override
            public String isValid(String newText) {
                if (isNullOrEmpty(newText)) {
                    return ""; //$NON-NLS-1$
                }
                ProjectCoordinate pc = ProjectCoordinate.valueOf(newText);
                if (snippet.getNeededDependencies().contains(pc)) {
                    return Messages.DIALOG_VALIDATOR_DEPENDENCY_ALREADY_ADDED;
                }

                return null;
            }
        };
        return new InputDialog(shell, Messages.DIALOG_TITLE_ENTER_NEW_DEPENDENCY,
                Messages.DIALOG_MESSAGE_ENTER_NEW_DEPENDENCY, "", validator) {
            @Override
            protected void okPressed() {
                ppDependencies.add(ProjectCoordinate.valueOf(getValue()));
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

        // location
        IObservableValue wpTxtLocation = ViewerProperties.singleSelection().observe(comboLocation);
        IObservableValue ppLocation = value(Snippet.class, "location", Location.class).observe(snippet); //$NON-NLS-1$
        context.bindValue(wpTxtLocation, ppLocation);
        ppLocation.addChangeListener(new IChangeListener() {
            @Override
            public void handleChange(ChangeEvent event) {
                IStructuredSelection selection = (IStructuredSelection) comboLocation.getSelection();
                if (!selection.getFirstElement().equals(snippet.getLocation())) {
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

        // dependencies
        ppDependencies = BeanProperties
                .set(Snippet.class, "neededDependencies", ProjectCoordinate.class).observe(snippet); //$NON-NLS-1$
        ViewerSupport.bind(listViewerDependencies, ppDependencies, new SimpleValueProperty() {

            @Override
            public Object getValueType() {
                return ProjectCoordinate.class;
            }

            @Override
            protected Object doGetValue(Object source) {
                ProjectCoordinate pc = cast(source);
                if (pc == null) {
                    System.out.println("");
                }
                return getStringForDependency(pc);
            }

            @Override
            protected void doSetValue(Object source, Object value) {
            }

            @Override
            public INativePropertyListener adaptListener(ISimplePropertyListener listener) {
                return null;
            }

        });
        ppDependencies.addSetChangeListener(new ISetChangeListener() {

            @Override
            public void handleSetChange(SetChangeEvent event) {
                Set<ProjectCoordinate> pcs = convert(listViewerDependencies.getList().getItems());
                if (!pcs.equals(snippet.getNeededDependencies())) {
                    contentsPart.markStale();
                } else {
                    contentsPart.markDirty();
                }
            }

            private Set<ProjectCoordinate> convert(String[] strings) {
                Set<ProjectCoordinate> result = Sets.newHashSet();
                for (String projectCoordinate : strings) {
                    result.add(ProjectCoordinate.valueOf(projectCoordinate + ":0.0.0")); //$NON-NLS-1$
                }
                return result;
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

        IObservableValue vpDependencySelection = singleSelection().observe(listViewerDependencies);
        IObservableValue wpBtnRemoveDependenciesEnable = enabled().observe(btnRemoveDependency);
        context.bindValue(vpDependencySelection, wpBtnRemoveDependenciesEnable, strategy, null);

        return context;
    }

    private String getStringForDependency(ProjectCoordinate pc) {
        return pc.getGroupId() + ":" + pc.getArtifactId(); //$NON-NLS-1$
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

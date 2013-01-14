/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 *    Marcel Bruch - adapted UI for latest model archive store.
 *    Patrick Gottschaemmer, Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import static org.apache.commons.lang3.builder.CompareToBuilder.reflectionCompare;
import static org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionPlugin.PLUGIN_ID;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.swt.SWT.BORDER;
import static org.eclipse.swt.SWT.READ_ONLY;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.recommenders.completion.rcp.calls.l10n.Messages;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.wiring.CallsCompletionPlugin;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.rcp.RCPUtils;
import org.eclipse.recommenders.utils.rcp.internal.ContentAssistEnablementBlock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class CallPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPropertyChangeListener {

    public static final String ID_MAX_PROPOSALS = "recommenders.calls.max_proposals"; //$NON-NLS-1$
    public static final String ID_MIN_PROBABILITY = "recommenders.calls.min_probability"; //$NON-NLS-1$

    private final IClasspathEntryInfoProvider cpeInfoProvider;
    private final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore;
    private List<Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>>> mappings;
    final IModelRepository repository;

    private ContentAssistEnablementBlock enablement;
    private IntegerFieldEditor maxProposals;
    private IntegerFieldEditor minProbability;

    private Text rootName;
    private Text rootVersion;
    private Text rootFingerprint;
    private Text modelCoordinate;
    private ComboViewer modelStatus;
    private TableViewer tableViewer;
    private WritableValue mValue;
    private WritableValue rValue;

    @Inject
    public CallPreferencePage(final IClasspathEntryInfoProvider cpeInfoProvider,
            final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore, final IModelRepository repository) {
        this.cpeInfoProvider = cpeInfoProvider;
        this.modelStore = modelStore;
        this.repository = repository;
    }

    @Override
    public void init(final IWorkbench workbench) {
        setPreferenceStore(CallsCompletionPlugin.getDefault().getPreferenceStore());
        setDescription(Messages.PREFPAGE_DESCRIPTION);
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        createConfigurationBlock(composite);
        createModelInformationBlock(composite);
        return composite;
    }

    private void createConfigurationBlock(final Composite composite) {
        enablement = new ContentAssistEnablementBlock(composite, Messages.PREFPAGE_ENABLE_CALL_COMPLETION,
                CallsCompletionProposalComputer.CATEGORY_ID);
        final Composite group = new Composite(composite, SWT.NONE);
        group.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        group.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        maxProposals = createIntegerField(group, ID_MAX_PROPOSALS, Messages.PREFPAGE_MAX_NUMBER_OF_PROPOSALS, 1, 100);
        minProbability = createIntegerField(group, ID_MIN_PROBABILITY, Messages.PREFPAGE_MIN_PROBABILITY_OF_PROPOSAL,
                1, 100);
    }

    private IntegerFieldEditor createIntegerField(final Composite parent, final String name, final String text,
            final int min, final int max) {
        final IntegerFieldEditor editor = new IntegerFieldEditor(name, text, parent);
        editor.setPreferenceStore(getPreferenceStore());
        editor.setPage(this);
        editor.setValidRange(min, max);
        editor.setPropertyChangeListener(this);
        editor.load();
        return editor;
    }

    private void createModelInformationBlock(final Composite composite) {
        new Label(composite, SWT.NONE).setText(Messages.PREFPAGE_TABLE_DESCRIPTION);
        computeMappings();
        final SashForm form = new SashForm(composite, SWT.HORIZONTAL);
        form.setLayout(new FillLayout());
        createTable(form);
        createDetails(form);
        bindValues();
        form.setWeights(new int[] { 50, 50 });
    }

    private void computeMappings() {
        mappings = Lists.newLinkedList();
        for (final File root : cpeInfoProvider.getFiles()) {
            final Optional<ClasspathEntryInfo> opt = cpeInfoProvider.getInfo(root);
            if (!opt.isPresent()) {
                continue;
            }
            final ClasspathEntryInfo cpei = opt.get();

            final ModelArchiveMetadata<?, ?> metadata = modelStore.findOrCreateMetadata(root);
            final Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> entry = Tuple.newTuple(cpei, metadata);
            mappings.add(entry);
        }
    }

    private void bindValues() {
        final DataBindingContext ctx = new DataBindingContext();
        {
            rValue = new WritableValue();
            bindValue(ctx, rootName, ClasspathEntryInfo.class, ClasspathEntryInfo.P_SYMBOLIC_NAME, rValue);
            bindValue(ctx, rootFingerprint, ClasspathEntryInfo.class, ClasspathEntryInfo.P_FINGERPRINT, rValue);
            bindValue(ctx, rootVersion, ClasspathEntryInfo.class, ClasspathEntryInfo.P_VERSION, rValue);
        }
        {
            mValue = new WritableValue();
            bindValue(ctx, modelCoordinate, ModelArchiveMetadata.class, ModelArchiveMetadata.P_COORDINATE, mValue);

            final IObservableValue widgetValue = ViewerProperties.singlePostSelection().observe(modelStatus);
            final IObservableValue modelValue = BeanProperties.value(ModelArchiveMetadata.class,
                    ModelArchiveMetadata.P_STATUS).observeDetail(mValue);
            ctx.bindValue(widgetValue, modelValue);
        }
    }

    private void bindValue(final DataBindingContext ctx, final Widget widget, final Class<?> clazz,
            final String property, final IObservableValue value) {
        final IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(widget);
        final IObservableValue modelValue = BeanProperties.value(clazz, property).observeDetail(value);
        ctx.bindValue(widgetValue, modelValue);
    }

    private void createDetails(final SashForm form) {
        final Composite parent = new Composite(form, SWT.NONE);
        parent.setLayout(new GridLayout());
        final GridDataFactory f = GridDataFactory.fillDefaults().grab(true, false);
        {
            final Group rootContainer = new Group(parent, SWT.SHADOW_ETCHED_IN);
            rootContainer.setText(Messages.PREFPAGE_PACKAGE_ROOT_INFO);
            rootContainer.setLayout(new GridLayout(2, false));
            rootContainer.setLayoutData(f.create());

            createLabel(rootContainer, Messages.PREFPAGE_PACKAGE_ROOT_NAME);
            rootName = createText(rootContainer, READ_ONLY | BORDER);

            createLabel(rootContainer, Messages.PREFPAGE_PACKAGE_ROOT_VERSION);
            rootVersion = createText(rootContainer, READ_ONLY | BORDER);

            createLabel(rootContainer, Messages.PREFPAGE_PACKAGE_ROOT_FINGERPRINT);
            rootFingerprint = createText(rootContainer, READ_ONLY | BORDER);
        }
        {
            final Group modelContainer = new Group(parent, SWT.SHADOW_ETCHED_IN);
            modelContainer.setText(Messages.PREFPAGE_MODEL_INFO);
            modelContainer.setLayout(new GridLayout(2, false));
            modelContainer.setLayoutData(f.create());

            createLabel(modelContainer, Messages.PREFPAGE_MODEL_CCORDINATE);
            modelCoordinate = createText(modelContainer, SWT.BORDER);

            createLabel(modelContainer, Messages.PREFPAGE_MODEL_RESOLUTION_STATUS);
            modelStatus = new ComboViewer(modelContainer, SWT.BORDER);
            modelStatus.setContentProvider(new ArrayContentProvider());
            modelStatus.setInput(ModelArchiveResolutionStatus.values());
        }
    }

    private void createTable(final Composite container) {
        final ScrolledComposite sc = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL);
        sc.setLayoutData(new GridLayout());
        sc.setExpandHorizontal(true);
        final Composite tContainer = new Composite(sc, SWT.NONE);
        tContainer.setSize(200, 500);
        tableViewer = new TableViewer(tContainer, SWT.FULL_SELECTION);
        final Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object e1, final Object e2) {
                final Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> t1 = cast(e1);
                final File f1 = t1.getSecond().getLocation();
                final Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> t2 = cast(e2);
                final File f2 = t2.getSecond().getLocation();
                return reflectionCompare(f1, f2);
            }
        });
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final Optional<Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>>> e = RCPUtils.first(event
                        .getSelection());
                mValue.setValue(e.get().getSecond());
                rValue.setValue(e.get().getFirst());
            }
        });

        final TableColumnLayout tableColumnLayout = new TableColumnLayout();
        final Image versionImage = loadImage("/icons/obj16/file_version.png"); //$NON-NLS-1$
        final Image versionUnknownImage = loadImage("/icons/obj16/file_version_unknown.png"); //$NON-NLS-1$
        final Image modelImage = loadImage("/icons/obj16/model.png"); //$NON-NLS-1$
        final Image modelUnknownImage = loadImage("/icons/obj16/model_unknown.png"); //$NON-NLS-1$

        ColumnViewerToolTipSupport.enableFor(tableViewer);
        TableViewerColumn column = createTableViewerColumn(tableViewer, Messages.PREFPAGE_TABLE_COLUMN_FILE, 200, 0);
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(100));
        column.setLabelProvider(new PackageFragmentRootLabelProvider());

        column = createTableViewerColumn(tableViewer, "", 20, 1); //$NON-NLS-1$
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnPixelData(20));
        column.setLabelProvider(new VersionLabelProvider(versionUnknownImage, versionImage));

        column = createTableViewerColumn(tableViewer, "", 20, 2); //$NON-NLS-1$
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnPixelData(20));
        column.setLabelProvider(new ModelLabelProvider(repository, modelImage, modelUnknownImage));
        tContainer.setLayout(tableColumnLayout);
        tableViewer.setInput(mappings);

        sc.setContent(tContainer);
    }

    protected Text createText(final Composite parent, final int style) {
        final Text text = new Text(parent, style);
        text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(150, SWT.DEFAULT)
                .align(GridData.FILL, GridData.BEGINNING).create());
        return text;
    }

    protected Image loadImage(final String name) {
        final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, name);
        return desc.createImage();
    }

    protected Label createLabel(final Composite parent, final String text) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int bound,
            final int colNumber) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(false);
        column.setMoveable(false);
        return viewerColumn;
    }

    @Override
    protected void performDefaults() {
        maxProposals.loadDefault();
        minProbability.loadDefault();
    }

    @Override
    public boolean performOk() {
        maxProposals.store();
        minProbability.store();
        return super.performOk();
    }

    @Override
    public void setVisible(final boolean visible) {
        // respond to changes in Java > Editor > Content Assist > Advanced:
        // this works only one-way. We respond to changes made in JDT but JDT page may show deprecated values.
        enablement.loadSelection();
        super.setVisible(visible);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        if (event.getProperty().equals(FieldEditor.IS_VALID)) {
            setValid(maxProposals.isValid() && minProbability.isValid());
        }
    }

}

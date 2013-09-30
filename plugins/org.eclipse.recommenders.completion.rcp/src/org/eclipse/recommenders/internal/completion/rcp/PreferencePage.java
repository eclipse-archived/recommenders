/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.jface.databinding.viewers.ViewerProperties.checkedElements;
import static org.eclipse.jface.layout.GridDataFactory.*;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor.EnabledSessionProcessorPredicate;
import org.eclipse.recommenders.rcp.utils.ObjectToBooleanConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage {

    Set<SessionProcessorDescriptor> processors;
    Set<SessionProcessorDescriptor> enabled;
    private CheckboxTableViewer viewer;
    private DataBindingContext ctx;
    private IViewerObservableSet checked;
    private Button configureBtn;
    private IViewerObservableValue selected;

    @Inject
    public PreferencePage(SessionProcessorDescriptor[] pr) {
        processors = ImmutableSet.copyOf(pr);
        enabled = Sets.filter(processors, new EnabledSessionProcessorPredicate());
    }

    @Override
    public void init(IWorkbench workbench) {
        setMessage("Recommenders Completion Settings");
        setDescription("Configure which session processors to enable on Recommenders intelligent code completion. Processors may offer advanced configuration options.");

    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(fillDefaults().grab(true, true).create());
        container.setLayout(new GridLayout(2, false));
        viewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
        viewer.setSorter(new ViewerSorter());
        viewer.getTable().setLayoutData(fillDefaults().hint(300, 150).grab(true, false).create());
        ColumnViewerToolTipSupport.enableFor(viewer);
        configureBtn = new Button(container, SWT.PUSH);
        configureBtn.setText("Configure");
        configureBtn.setLayoutData(swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).create());
        configureBtn.setEnabled(false);
        configureBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SessionProcessorDescriptor value = cast(selected.getValue());
                String id = value.getPreferencePage().orNull();
                PreferencesUtil.createPreferenceDialogOn(getShell(), id, null, null);
            }
        });

        initDataBindings();
        return container;
    }

    protected void initDataBindings() {
        ctx = new DataBindingContext();
        ObservableSetContentProvider cp = new ObservableSetContentProvider();
        viewer.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object o) {
                return s(o).getName();
            }

            @Override
            public Image getImage(Object o) {
                return s(o).getIcon();
            }

            @Override
            public String getToolTipText(Object o) {
                return s(o).getDescription();
            }

            private SessionProcessorDescriptor s(Object element) {
                return cast(element);
            }
        });
        viewer.setContentProvider(cp);
        IObservableSet processors = Properties.selfSet(this.processors).observe(this.processors);
        viewer.setInput(processors);
        checked = checkedElements(SessionProcessorDescriptor.class).observe(viewer);
        viewer.setCheckedElements(enabled.toArray());
        selected = ViewersObservables.observeSinglePostSelection(viewer);
        ISWTObservableValue configure = WidgetProperties.enabled().observe(configureBtn);
        UpdateValueStrategy strategy = new UpdateValueStrategy();
        strategy.setConverter(new ObjectToBooleanConverter() {
            @Override
            public Boolean convert(Object fromObject) {
                Boolean convert = super.convert(fromObject);
                SessionProcessorDescriptor value = cast(selected.getValue());
                return convert && value.getPreferencePage().isPresent();
            }

        });
        ctx.bindValue(selected, configure, strategy, null);
    }

    @Override
    public boolean performOk() {
        for (SessionProcessorDescriptor d : (Set<SessionProcessorDescriptor>) checked) {
            d.setEnabled(true);
        }
        for (SessionProcessorDescriptor d : Sets.difference(processors, checked)) {
            d.setEnabled(false);
        }
        return super.performOk();
    }

    @Override
    public void dispose() {
        ctx.dispose();
    }
}

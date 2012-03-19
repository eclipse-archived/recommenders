/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata.ModelArchiveResolutionStatus;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.inject.assistedinject.Assisted;

public class ModelDetailsSection extends AbstractSection {

    private final IModelArchiveStore<IType, IObjectMethodCallsNet> modelStore;

    private Text nameText;

    private ModelArchiveMetadata<ITypeName, IObjectMethodCallsNet> model;
    private WritableValue value;
    private ComboViewer statusText;

    @Inject
    public ModelDetailsSection(@Assisted final PreferencePage preferencePage, @Assisted final Composite parent,
            IModelArchiveStore<IType, IObjectMethodCallsNet> store) {
        super(preferencePage, parent, "Matched model");
        this.modelStore = store;
    }

    @Override
    protected void createDetailsContainer(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, SWT.BORDER);
        createLabel(parent, "Status:");
        statusText = new ComboViewer(parent, SWT.BORDER);
        statusText.setContentProvider(new ArrayContentProvider());
        statusText.setInput(ModelArchiveResolutionStatus.values());
        bindValues();
    }

    @Override
    protected void createButtons(Composite parent) {
    }

    private void bindValues() {
        value = new WritableValue();
        DataBindingContext ctx = new DataBindingContext();

        IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(nameText);
        IObservableValue modelValue = BeanProperties.value(ModelArchiveMetadata.class,
                ModelArchiveMetadata.P_COORDINATE).observeDetail(value);
        ctx.bindValue(widgetValue, modelValue);

        widgetValue = ViewerProperties.singlePostSelection().observe(statusText);
        modelValue = BeanProperties.value(ModelArchiveMetadata.class, ModelArchiveMetadata.P_STATUS).observeDetail(
                value);
        ctx.bindValue(widgetValue, modelValue);

    }

    public void selectionChanged(final File file) {
        this.file = file;
        // this.model = modelStore.getModel(file);
        // value.doSetValue(model);

    }

}

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
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelResolutionData;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.CallModelStore;
import org.eclipse.recommenders.rcp.IClasspathEntryInfoProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.google.inject.assistedinject.Assisted;

public class ModelDetailsSection extends AbstractSection {

    private final CallModelStore modelStore;
    private final IClasspathEntryInfoProvider cpeInfoProvider;

    private Text nameText;

    private File file;
    private CallModelResolutionData model;
    private WritableValue value;
    private Text statusText;

    @Inject
    public ModelDetailsSection(@Assisted final PreferencePage preferencePage, @Assisted final Composite parent,
            final CallModelStore modelStore, IClasspathEntryInfoProvider cpeInfoProvider) {
        super(preferencePage, parent, "Matched model");
        this.modelStore = modelStore;
        this.cpeInfoProvider = cpeInfoProvider;
    }

    @Override
    protected void createDetailsContainer(final Composite parent) {
        createLabel(parent, "Name:");
        nameText = createText(parent, SWT.READ_ONLY | SWT.BORDER);
        createLabel(parent, "Status:");
        statusText = createText(parent, SWT.READ_ONLY | SWT.BORDER);
        bindValues();
    }

    @Override
    protected void createButtons(Composite parent) {
    }

    private void bindValues() {
        value = new WritableValue();
        DataBindingContext ctx = new DataBindingContext();
        IObservableValue widgetValue = WidgetProperties.text(SWT.Modify).observe(nameText);
        IObservableValue modelValue = BeanProperties.value(CallModelResolutionData.class,
                CallModelResolutionData.P_COORDINATE).observeDetail(value);
        ctx.bindValue(widgetValue, modelValue);

        widgetValue = WidgetProperties.text(SWT.Modify).observe(statusText);
        modelValue = BeanProperties.value(CallModelResolutionData.class, CallModelResolutionData.P_STATUS)
                .observeDetail(value);
        ctx.bindValue(widgetValue, modelValue);
    }

    public void selectionChanged(final File file) {
        this.file = file;
        this.model = modelStore.getModel(file);
        value.doSetValue(model);

    }

}

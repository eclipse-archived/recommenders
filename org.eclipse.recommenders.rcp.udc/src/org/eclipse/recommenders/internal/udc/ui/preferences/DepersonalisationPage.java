/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.preferences;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.internal.udc.Activator;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.ui.depersonalisation.DepersonalisationController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DepersonalisationPage extends PreferencePage implements IWorkbenchPreferencePage, PropertyChangeListener {
    DepersonalisationController controller;

    /**
     * @wbp.parser.constructor
     */
    public DepersonalisationPage() {
    }

    public DepersonalisationPage(final String title) {
    }

    public DepersonalisationPage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(final IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Below is a preview of usage data that will be uploaded. You can choose to depersonalize "
                + "that data to remove names of classes and variables.");
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createDepersonalisationSection(composite);
        return composite;
    }

    private void createDepersonalisationSection(final Composite parent) {
        controller = new DepersonalisationController();
        controller.createControl(parent);
        controller.setDepersonalisationEnabled(PreferenceUtil.isDepesonalisationRequired());

        controller.addPropertyChangeListener(this);

    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        controller.setDepersonalisationEnabled(false);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent arg0) {
        if (arg0.getPropertyName().equals(DepersonalisationController.errorMsgProperty)) {
            setErrorMessage(controller.getErrorMessage());
        }
    }

    @Override
    protected void performApply() {
        super.performApply();
        storePreferences();
    }

    @Override
    public boolean performOk() {
        storePreferences();
        return super.performOk();
    }

    private void storePreferences() {
        PreferenceUtil.setDepersonalisationRequired(controller.isDepersonalisationRequired());
    }
}

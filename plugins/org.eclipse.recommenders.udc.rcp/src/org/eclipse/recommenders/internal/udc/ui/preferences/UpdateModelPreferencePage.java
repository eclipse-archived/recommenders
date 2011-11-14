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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.calls.rcp.store.UpdateAllModelsJob;
import org.eclipse.recommenders.internal.udc.PreferenceKeys;
import org.eclipse.recommenders.internal.udc.PreferenceUtil;
import org.eclipse.recommenders.internal.udc.UpdateCompletedListener;
import org.eclipse.recommenders.internal.udc.UpdatePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.wb.swt.ResourceManager;

public class UpdateModelPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private Button btnUpdateModels;
    private Text updateIntervalText;
    private Button btnUpdateNow;
    private Text lastUpdateText;
    private IPreferenceChangeListener preferenceChangeListener;
    private final ControlDecorationDelegate decorationDelegate = new ControlDecorationDelegate();

    /**
     * @wbp.parser.constructor
     */
    public UpdateModelPreferencePage() {
    }

    public UpdateModelPreferencePage(final String title) {
        super(title);
    }

    public UpdateModelPreferencePage(final String title, final ImageDescriptor image) {
        super(title, image);
    }

    @Override
    public void init(final IWorkbench workbench) {
        setDescription("Activate or deactivate automatic updates for Recommenders models.");
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout gl_composite = new GridLayout(1, false);
        gl_composite.verticalSpacing = 20;
        composite.setLayout(gl_composite);

        btnUpdateModels = new Button(composite, SWT.CHECK);
        btnUpdateModels.setText("Update models");

        createScheduleSection(composite);

        btnUpdateNow = new Button(composite, SWT.NONE);
        btnUpdateNow.setImage(ResourceManager.getPluginImage("org.eclipse.recommenders.rcp.codecompletion.calls",
                "icons/obj16/refresh.gif"));
        btnUpdateNow.setText("Update Now");

        initializeContent();

        addListeners();

        return composite;
    }

    private void addListeners() {
        btnUpdateModels.addSelectionListener(createUpdateModelsSelectedListener());
        preferenceChangeListener = createPreferenceChangeListener();
        PreferenceUtil.getPluginNode().addPreferenceChangeListener(preferenceChangeListener);

        updateIntervalText.addModifyListener(createUpdateIntervalModifiedListener());

        btnUpdateNow.addSelectionListener(createUpdateNowSelectionListener());
    }

    private SelectionListener createUpdateNowSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateNow();
            }

        };
    }

    private void updateNow() {
        final UpdateAllModelsJob job = InjectionService.getInstance().requestInstance(UpdateAllModelsJob.class);
        job.addJobChangeListener(new UpdateCompletedListener());
        job.schedule();
    }

    private ModifyListener createUpdateIntervalModifiedListener() {
        return new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                validatePage();
            }
        };
    }

    private IPreferenceChangeListener createPreferenceChangeListener() {
        return new IPreferenceChangeListener() {

            @Override
            public void preferenceChange(final PreferenceChangeEvent event) {
                if (!event.getKey().equals(PreferenceKeys.lastUpdateDate)) {
                    return;
                }
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        setLastUploadDate(UpdatePreferences.getLastUpdateDate());
                    }
                });
            }
        };
    }

    private SelectionListener createUpdateModelsSelectedListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateIntervalEnablement();
            }
        };
    }

    private void createScheduleSection(final Composite composite) {
        final Group grpSchedule = new Group(composite, SWT.NONE);
        final GridLayout gl_grpSchedule = new GridLayout(1, false);
        gl_grpSchedule.marginWidth = 0;
        grpSchedule.setLayout(gl_grpSchedule);
        grpSchedule.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpSchedule.setText("Update Schedule");

        createIntervalSection(grpSchedule);

        createLastUploadSection(grpSchedule);
    }

    private void createIntervalSection(final Group grpSchedule) {
        final Composite composite = new Composite(grpSchedule, SWT.NONE);
        final GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_composite.widthHint = 212;
        composite.setLayoutData(gd_composite);

        final GridLayout gl_composite = new GridLayout(3, false);
        gl_composite.marginHeight = 0;
        composite.setLayout(gl_composite);

        final Label lblEvery = new Label(composite, SWT.NONE);
        lblEvery.setSize(28, 15);
        lblEvery.setText("Every");

        updateIntervalText = new Text(composite, SWT.BORDER);
        final GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text.horizontalIndent = 5;
        gd_text.minimumWidth = 25;
        updateIntervalText.setLayoutData(gd_text);
        updateIntervalText.setSize(76, 21);

        final Label lblDaysOnStartup = new Label(composite, SWT.NONE);
        lblDaysOnStartup.setSize(90, 15);
        lblDaysOnStartup.setText("Day(s) on startup");
    }

    private void createLastUploadSection(final Group grpSchedule) {
        final Composite composite_2 = new Composite(grpSchedule, SWT.NONE);
        composite_2.setLayout(new GridLayout(2, false));

        final Label lblLastUpdate = new Label(composite_2, SWT.NONE);
        lblLastUpdate.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblLastUpdate.setBounds(0, 0, 55, 15);
        lblLastUpdate.setText("Last Update:");

        lastUpdateText = new Text(composite_2, SWT.BORDER);
        lastUpdateText.setEditable(false);
        lastUpdateText.setEnabled(false);
        lastUpdateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    private void initializeContent() {
        updateIntervalText.setText(UpdatePreferences.getUpdateInterval() + "");
        btnUpdateModels.setSelection(UpdatePreferences.isUpdateModels());
        setLastUploadDate(UpdatePreferences.getLastUpdateDate());
        updateIntervalEnablement();
    }

    private void setLastUploadDate(final long lastUpdateDate) {
        if (lastUpdateDate == 0) {
            lastUpdateText.setText("N/A");
        } else {
            final SimpleDateFormat dateFormat = new SimpleDateFormat();
            lastUpdateText.setText(dateFormat.format(new Date(lastUpdateDate)));
        }
        lastUpdateText.pack(true);
        lastUpdateText.getParent().pack();

    }

    private void updateIntervalEnablement() {
        updateIntervalText.setEnabled(btnUpdateModels.getSelection());
    }

    private void savePreference() {
        UpdatePreferences.setUpdateInterval(Integer.valueOf(updateIntervalText.getText()));
        UpdatePreferences.setUpdateModels(btnUpdateModels.getSelection());
    }

    protected boolean isUpdateIntervalValid() {
        try {
            final int days = Integer.valueOf(updateIntervalText.getText());
            if (days < 1) {
                decorationDelegate.setDecoratorText(updateIntervalText, "Enter an interval of at least one day");
                return false;
            }

        } catch (final Exception e) {
            decorationDelegate.setDecoratorText(updateIntervalText, "Invalid number");
            return false;
        }
        return true;
    }

    @Override
    public boolean performOk() {
        savePreference();
        return super.performOk();
    }

    @Override
    protected void performApply() {
        savePreference();
        super.performApply();
    }

    @Override
    public void dispose() {
        PreferenceUtil.getPluginNode().removePreferenceChangeListener(preferenceChangeListener);
        super.dispose();
    }

    private void validatePage() {
        decorationDelegate.clearDecorations();
        final boolean valid = isUpdateIntervalValid();
        if (valid) {
            this.setErrorMessage(null);
            this.setValid(true);
        } else {
            this.setErrorMessage("Page contains errors");
            this.setValid(false);
        }

    }
}

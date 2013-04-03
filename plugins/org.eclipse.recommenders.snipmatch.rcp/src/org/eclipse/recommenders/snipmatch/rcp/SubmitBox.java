/**
 * Copyright (c) 2011,2012 Doug Wightman, Zi Ye, Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    Doug Wightman, Zi Ye - initial API and implementation and/or initial documentation
 *    Cheng Chen - Refactor for SnipMatch view
 *    
 */

package org.eclipse.recommenders.snipmatch.rcp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.EffectParameter;
import org.eclipse.recommenders.snipmatch.core.MatchEnvironment;
import org.eclipse.recommenders.snipmatch.search.ClientSwitcher;
import org.eclipse.recommenders.snipmatch.view.ViewConstants;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * This is the interface for creating an editing snippets.
 */
public class SubmitBox extends ClientSwitcher {

    private MatchEnvironment[] envs;
    private MatchEnvironment activeEnv;
    private Effect effect;
    private Shell shell;
    private Composite[] pages;
    private Button submitButton;
    private Button cancelButton;
    private boolean editing;

    // page 1 widgets: General
    private Text minorTypeText;
    private Text sumText;

    // page 2 widgets: Input
    private List paramsList;
    private Button addParamButton;
    private Button editParamButton;
    private Button removeParamButton;
    private List patsList;
    private Button addPatButton;
    private Button editPatButton;
    private Button removePatButton;

    // page 3 widgets: Code
    private StyledText codeText;
    private Button insertFormulaButton;

    private final Font codeFont = JFaceResources.getTextFont();

    private String editFilePath = null;

    public SubmitBox() {
    }

    /**
     * Show the snippet creation/editing interface.
     * 
     * @param toEdit
     *            The snippet to edit. If null, then the interface is used to create a new snippet.
     */
    public void show(String summary, String filePath, Effect toEdit) {
        editFilePath = filePath;
        envs = new MatchEnvironment[] { new JavaSnippetMatchEnvironment() };
        editing = toEdit != null;

        if (editing)
            effect = toEdit;
        else {
            effect = new Effect();
            effect.setEnvironmentName("javasnippet");
        }

        if (shell != null && !shell.isDisposed())
            shell.dispose();

        shell = new Shell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.CLOSE);

        if (editing)
            shell.setText("Edit Snippet");
        else
            shell.setText("Submit Snippet");

        shell.setSize(640, 480);

        FormLayout fl = new FormLayout();
        shell.setLayout(fl);

        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.top = new FormAttachment(0);
        fd.bottom = new FormAttachment(100, -40);

        CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
        tabFolder.setLayoutData(fd);

        String[] tabNames = new String[] { "General", "Input", "Code" };

        CTabItem[] tabs = new CTabItem[tabNames.length];
        pages = new Composite[tabNames.length];

        for (int i = 0; i < tabNames.length; i++) {

            fl = new FormLayout();
            fl.spacing = 5;
            fl.marginWidth = 5;
            fl.marginHeight = 5;

            pages[i] = new Composite(tabFolder, SWT.BORDER);
            pages[i].setLayout(fl);

            tabs[i] = new CTabItem(tabFolder, SWT.NONE, i);
            tabs[i].setText("   " + tabNames[i] + "   ");
            tabs[i].setControl(pages[i]);
        }

        {
            Composite page = pages[0];

            Label minorTypeExampleLabel;

            {
                fd = new FormData();
                fd.left = new FormAttachment(0);
                fd.right = new FormAttachment(0, 80);

                Label minorTypeLabel = new Label(page, SWT.NONE);
                minorTypeLabel.setText("Return Type: ");
                minorTypeLabel.setLayoutData(fd);

                fd = new FormData();
                fd.left = new FormAttachment(minorTypeLabel);
                fd.right = new FormAttachment(100);

                minorTypeText = new Text(page, SWT.BORDER);
                minorTypeText.setLayoutData(fd);

                fd = new FormData();
                fd.left = new FormAttachment(minorTypeText, 0, SWT.LEFT);
                fd.top = new FormAttachment(minorTypeText);

                minorTypeExampleLabel = new Label(page, SWT.NONE);
                minorTypeExampleLabel.setText("(e.g., int, String, java.io.File[], $array, void)");
                minorTypeExampleLabel.setLayoutData(fd);
            }

            {
                fd = new FormData();
                fd.left = new FormAttachment(0);
                fd.top = new FormAttachment(minorTypeExampleLabel);

                Label sumLabel = new Label(page, SWT.NONE);
                sumLabel.setText("Summary: ");
                sumLabel.setLayoutData(fd);

                fd = new FormData();
                fd.left = new FormAttachment(minorTypeText, 0, SWT.LEFT);
                fd.right = new FormAttachment(minorTypeText, 0, SWT.RIGHT);
                fd.top = new FormAttachment(minorTypeExampleLabel);
                fd.bottom = new FormAttachment(100);

                sumText = new Text(page, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
                sumText.setLayoutData(fd);
            }
        }

        {
            Composite page = pages[1];

            {
                fl = new FormLayout();
                fl.marginWidth = 5;
                fl.marginHeight = 5;
                fl.spacing = 5;

                fd = new FormData();
                fd.left = new FormAttachment(0);
                fd.right = new FormAttachment(100);
                fd.top = new FormAttachment(0);
                fd.bottom = new FormAttachment(50);

                Group patsGroup = new Group(page, SWT.NONE);
                patsGroup.setText("Patterns");
                patsGroup.setLayout(fl);
                patsGroup.setLayoutData(fd);

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(0);
                    fd.right = new FormAttachment(0, 300);
                    fd.top = new FormAttachment(0);
                    fd.bottom = new FormAttachment(100, 0);

                    patsList = new List(patsGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
                    patsList.setLayoutData(fd);

                    patsList.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            editPatButton.setEnabled(true);
                            removePatButton.setEnabled(true);
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            editPatButton.setEnabled(true);
                            removePatButton.setEnabled(true);
                        }
                    });
                }

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(patsList);
                    fd.right = new FormAttachment(100);
                    fd.top = new FormAttachment(0);

                    addPatButton = new Button(patsGroup, SWT.NONE);
                    addPatButton.setText("Add Pattern");
                    addPatButton.setLayoutData(fd);

                    addPatButton.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            showAddPatternDialog();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            showAddPatternDialog();
                        }
                    });
                }

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(addPatButton, 0, SWT.LEFT);
                    fd.right = new FormAttachment(100);
                    fd.top = new FormAttachment(addPatButton);

                    editPatButton = new Button(patsGroup, SWT.NONE);
                    editPatButton.setText("Edit Pattern");
                    editPatButton.setEnabled(false);
                    editPatButton.setLayoutData(fd);

                    editPatButton.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            showEditPatternDialog();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            showEditPatternDialog();
                        }
                    });
                }

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(editPatButton, 0, SWT.LEFT);
                    fd.right = new FormAttachment(100);
                    fd.top = new FormAttachment(editPatButton);

                    removePatButton = new Button(patsGroup, SWT.NONE);
                    removePatButton.setText("Remove Pattern");
                    removePatButton.setEnabled(false);
                    removePatButton.setLayoutData(fd);

                    removePatButton.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            removePattern();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            removePattern();
                        }

                        private void removePattern() {

                            effect.removePattern(patsList.getSelectionIndex());
                            patsList.remove(patsList.getSelectionIndex());

                            if (patsList.getItemCount() == 0) {
                                editPatButton.setEnabled(false);
                                removePatButton.setEnabled(false);
                            }
                        }
                    });
                }
            }

            {
                fl = new FormLayout();
                fl.marginWidth = 5;
                fl.marginHeight = 5;
                fl.spacing = 5;

                fd = new FormData();
                fd.left = new FormAttachment(0);
                fd.right = new FormAttachment(100);
                fd.top = new FormAttachment(50);
                fd.bottom = new FormAttachment(100);

                Group paramsGroup = new Group(page, SWT.NONE);
                paramsGroup.setText("Parameters");
                paramsGroup.setLayout(fl);
                paramsGroup.setLayoutData(fd);

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(0);
                    fd.right = new FormAttachment(0, 300);
                    fd.top = new FormAttachment(0);
                    fd.bottom = new FormAttachment(100, 0);

                    paramsList = new List(paramsGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
                    paramsList.setLayoutData(fd);

                    paramsList.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            editParamButton.setEnabled(true);
                            removeParamButton.setEnabled(true);
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            editParamButton.setEnabled(true);
                            removeParamButton.setEnabled(true);
                        }
                    });
                }

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(paramsList);
                    fd.right = new FormAttachment(100);
                    fd.top = new FormAttachment(0);

                    addParamButton = new Button(paramsGroup, SWT.NONE);
                    addParamButton.setText("Add Parameter");
                    addParamButton.setLayoutData(fd);

                    addParamButton.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            showAddParameterDialog();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            showAddParameterDialog();
                        }
                    });
                }

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(addParamButton, 0, SWT.LEFT);
                    fd.right = new FormAttachment(100);
                    fd.top = new FormAttachment(addParamButton);

                    editParamButton = new Button(paramsGroup, SWT.NONE);
                    editParamButton.setText("Edit Parameter");
                    editParamButton.setEnabled(false);
                    editParamButton.setLayoutData(fd);

                    editParamButton.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            showEditParameterDialog();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            showEditParameterDialog();
                        }
                    });
                }

                {
                    fd = new FormData();
                    fd.left = new FormAttachment(editParamButton, 0, SWT.LEFT);
                    fd.right = new FormAttachment(100);
                    fd.top = new FormAttachment(editParamButton);

                    removeParamButton = new Button(paramsGroup, SWT.NONE);
                    removeParamButton.setText("Remove Parameter");
                    removeParamButton.setEnabled(false);
                    removeParamButton.setLayoutData(fd);

                    removeParamButton.addSelectionListener(new SelectionListener() {

                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            removeParam();
                        }

                        @Override
                        public void widgetDefaultSelected(SelectionEvent e) {
                            removeParam();
                        }

                        private void removeParam() {

                            effect.removeParameter(paramsList.getSelectionIndex());
                            paramsList.remove(paramsList.getSelectionIndex());

                            if (paramsList.getItemCount() == 0) {
                                editParamButton.setEnabled(false);
                                removeParamButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        }

        {
            Composite page = pages[2];

            {
                fd = new FormData();
                fd.left = new FormAttachment(0);
                fd.right = new FormAttachment(100);
                fd.top = new FormAttachment(0);
                fd.bottom = new FormAttachment(100, -30);

                codeText = new StyledText(page, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
                codeText.setFont(codeFont);
                codeText.setMargins(8, 5, 8, 5);
                codeText.setLayoutData(fd);
            }

            {
                fd = new FormData();
                fd.left = new FormAttachment(100, -155);
                fd.right = new FormAttachment(100);
                fd.bottom = new FormAttachment(100);

                insertFormulaButton = new Button(page, SWT.NONE);
                insertFormulaButton.setText("Insert Formula");
                insertFormulaButton.setLayoutData(fd);

                insertFormulaButton.addSelectionListener(new SelectionListener() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        showInsertFormulaDialog();
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        showInsertFormulaDialog();
                    }
                });
            }
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(0, 10);
            fd.bottom = new FormAttachment(100, -13);
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(100, -120);
            fd.right = new FormAttachment(100, -5);
            fd.bottom = new FormAttachment(100, -5);

            cancelButton = new Button(shell, SWT.NONE);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(fd);

            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    shell.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    shell.close();
                }
            });
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(cancelButton, -120, SWT.LEFT);
            fd.right = new FormAttachment(cancelButton, -5);
            fd.bottom = new FormAttachment(100, -5);

            submitButton = new Button(shell, SWT.NONE);
            submitButton.setText("Submit");
            submitButton.setLayoutData(fd);

            submitButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    submitEffect();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    submitEffect();
                }
            });

            shell.setDefaultButton(submitButton);
        }

        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {

                resetFields();
            }
        });

        shell.open();
    }

    /**
     * Reset all the fields.
     */
    private void resetFields() {

        for (int i = 0; i < envs.length; i++) {
            if (envs[i].getName().equals(effect.getEnvironmentName())) {
                activeEnv = envs[i];
                break;
            }
        }

        minorTypeText.setText(effect.getMinorType());

        sumText.setText(effect.getSummary());

        patsList.removeAll();
        editPatButton.setEnabled(false);
        removePatButton.setEnabled(false);

        for (String pat : effect.getPatterns()) {
            patsList.add(pat);
        }

        paramsList.removeAll();
        editParamButton.setEnabled(false);
        removeParamButton.setEnabled(false);

        for (EffectParameter param : effect.getParameters()) {
            paramsList.add(param.getName() + " (" + param.getMinorType() + ")");
        }

        codeText.setText(effect.getCode());
    }

    private void showAddPatternDialog() {

        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        dialog.setText("Add Pattern");
        dialog.setSize(260, 115);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 5;
        fl.marginHeight = 5;
        fl.spacing = 5;
        dialog.setLayout(fl);

        FormData fd;
        final Text patText;
        Button okButton;
        Button cancelButton;

        {
            Label patLabel = new Label(dialog, SWT.NONE);
            patLabel.setText("Pattern:");

            fd = new FormData();
            fd.left = new FormAttachment(0, 60);
            fd.right = new FormAttachment(100);

            patText = new Text(dialog, SWT.BORDER);
            patText.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(patText, 0, SWT.LEFT);
            fd.top = new FormAttachment(patText);

            Label patExampleLabel = new Label(dialog, SWT.NONE);
            patExampleLabel.setText("(e.g., popup $msg, length of $list)");
            patExampleLabel.setLayoutData(fd);
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(100, -100);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(100);

            cancelButton = new Button(dialog, SWT.NONE);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(fd);

            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    dialog.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    dialog.close();
                }
            });
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(cancelButton, -100, SWT.LEFT);
            fd.right = new FormAttachment(cancelButton);
            fd.bottom = new FormAttachment(100);

            okButton = new Button(dialog, SWT.NONE);
            okButton.setText("OK");
            okButton.setLayoutData(fd);

            okButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    addPattern();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    addPattern();
                }

                private void addPattern() {

                    patsList.add(patText.getText());
                    effect.addPattern(patText.getText());

                    editPatButton.setEnabled(true);
                    removePatButton.setEnabled(true);

                    patsList.select(patsList.getItemCount() - 1);

                    dialog.close();
                }
            });

            dialog.setDefaultButton(okButton);
        }

        dialog.open();
    }

    private void showEditPatternDialog() {

        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        dialog.setText("Edit Pattern");
        dialog.setSize(260, 115);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 5;
        fl.marginHeight = 5;
        fl.spacing = 5;
        dialog.setLayout(fl);

        FormData fd;
        final Text patText;
        Button okButton;
        Button cancelButton;

        {
            Label patternLabel = new Label(dialog, SWT.NONE);
            patternLabel.setText("Pattern:");

            fd = new FormData();
            fd.left = new FormAttachment(0, 60);
            fd.right = new FormAttachment(100);

            patText = new Text(dialog, SWT.BORDER);
            patText.setText(patsList.getItem(patsList.getSelectionIndex()));
            patText.setSelection(0, patText.getCharCount());
            patText.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(patText, 0, SWT.LEFT);
            fd.top = new FormAttachment(patText);

            Label patExampleLabel = new Label(dialog, SWT.NONE);
            patExampleLabel.setText("(e.g., popup $msg, length of $list)");
            patExampleLabel.setLayoutData(fd);
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(100, -100);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(100);

            cancelButton = new Button(dialog, SWT.NONE);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(fd);

            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    dialog.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    dialog.close();
                }
            });
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(cancelButton, -100, SWT.LEFT);
            fd.right = new FormAttachment(cancelButton);
            fd.bottom = new FormAttachment(100);

            okButton = new Button(dialog, SWT.NONE);
            okButton.setText("OK");
            okButton.setLayoutData(fd);

            okButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    editPattern();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    editPattern();
                }

                public void editPattern() {

                    patsList.setItem(patsList.getSelectionIndex(), patText.getText());
                    effect.setPattern(patsList.getSelectionIndex(), patText.getText());
                    dialog.close();
                }
            });

            dialog.setDefaultButton(okButton);
        }

        dialog.open();
    }

    private void showAddParameterDialog() {

        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        dialog.setText("Add Parameter");
        dialog.setSize(300, 150);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 5;
        fl.marginHeight = 5;
        fl.spacing = 5;
        dialog.setLayout(fl);

        FormData fd;
        final Text nameText;
        final Text minorTypeText;
        Button okButton;
        Button cancelButton;

        {
            Label nameLabel = new Label(dialog, SWT.NONE);
            nameLabel.setText("Name:");

            fd = new FormData();
            fd.left = new FormAttachment(0, 50);
            fd.right = new FormAttachment(100);

            nameText = new Text(dialog, SWT.BORDER);
            nameText.setLayoutData(fd);
        }

        {
            fd = new FormData();
            fd.top = new FormAttachment(nameText);

            Label minorTypeLabel = new Label(dialog, SWT.NONE);
            minorTypeLabel.setText("Type:");
            minorTypeLabel.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(nameText, 0, SWT.LEFT);
            fd.right = new FormAttachment(100);
            fd.top = new FormAttachment(minorTypeLabel, 0, SWT.TOP);

            minorTypeText = new Text(dialog, SWT.BORDER);
            minorTypeText.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(minorTypeText, 0, SWT.LEFT);
            fd.top = new FormAttachment(minorTypeText);

            Label minorTypeExampleLabel = new Label(dialog, SWT.NONE);
            minorTypeExampleLabel.setText("(e.g., int, String, java.io.File[], $array, void)");
            minorTypeExampleLabel.setLayoutData(fd);
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(100, -95);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(100);

            cancelButton = new Button(dialog, SWT.NONE);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(fd);

            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    dialog.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    dialog.close();
                }
            });
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(cancelButton, -80, SWT.LEFT);
            fd.right = new FormAttachment(cancelButton);
            fd.bottom = new FormAttachment(100);

            okButton = new Button(dialog, SWT.NONE);
            okButton.setText("OK");
            okButton.setLayoutData(fd);

            okButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    addParameter();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    addParameter();
                }

                private void addParameter() {

                    String name = nameText.getText();
                    String majorType = "expr";
                    String minorType = minorTypeText.getText();

                    if (name.startsWith("$"))
                        name = name.substring(1);

                    EffectParameter newParam = new EffectParameter();
                    newParam.setName(name);
                    newParam.setMajorType(majorType);
                    newParam.setMinorType(minorType);

                    paramsList.add(name + " (" + minorType + ")");
                    effect.addParameter(newParam);

                    editParamButton.setEnabled(true);
                    removeParamButton.setEnabled(true);

                    paramsList.select(paramsList.getItemCount() - 1);

                    dialog.close();
                }
            });

            dialog.setDefaultButton(okButton);
        }

        dialog.open();
    }

    private void showEditParameterDialog() {

        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        dialog.setText("Edit Parameter");
        dialog.setSize(300, 150);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 5;
        fl.marginHeight = 5;
        fl.spacing = 5;
        dialog.setLayout(fl);

        FormData fd;
        final Text nameText;
        final Text minorTypeText;
        Button okButton;
        Button cancelButton;

        {
            Label nameLabel = new Label(dialog, SWT.NONE);
            nameLabel.setText("Name:");

            fd = new FormData();
            fd.left = new FormAttachment(0, 50);
            fd.right = new FormAttachment(100);

            nameText = new Text(dialog, SWT.BORDER);
            nameText.setText(effect.getParameter(paramsList.getSelectionIndex()).getName());
            nameText.setLayoutData(fd);
        }

        {
            fd = new FormData();
            fd.top = new FormAttachment(nameText);

            Label minorTypeLabel = new Label(dialog, SWT.NONE);
            minorTypeLabel.setText("Type:");
            minorTypeLabel.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(nameText, 0, SWT.LEFT);
            fd.right = new FormAttachment(100);
            fd.top = new FormAttachment(minorTypeLabel, 0, SWT.TOP);

            minorTypeText = new Text(dialog, SWT.BORDER);
            minorTypeText.setText(effect.getParameter(paramsList.getSelectionIndex()).getMinorType());
            minorTypeText.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(minorTypeText, 0, SWT.LEFT);
            fd.top = new FormAttachment(minorTypeText);

            Label minorTypeExampleLabel = new Label(dialog, SWT.NONE);
            minorTypeExampleLabel.setText("(e.g., int, String, java.io.File[], $array)");
            minorTypeExampleLabel.setLayoutData(fd);
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(100, -95);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(100);

            cancelButton = new Button(dialog, SWT.NONE);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(fd);

            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    dialog.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    dialog.close();
                }
            });
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(cancelButton, -80, SWT.LEFT);
            fd.right = new FormAttachment(cancelButton);
            fd.bottom = new FormAttachment(100);

            okButton = new Button(dialog, SWT.NONE);
            okButton.setText("OK");
            okButton.setLayoutData(fd);

            okButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    editParameter();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    editParameter();
                }

                public void editParameter() {

                    String name = nameText.getText();
                    String majorType = "expr";
                    String minorType = minorTypeText.getText();

                    if (name.startsWith("$"))
                        name = name.substring(1);

                    EffectParameter param = effect.getParameter(paramsList.getSelectionIndex());

                    param.setName(name);
                    param.setMajorType(majorType);
                    param.setMinorType(minorType);

                    paramsList.setItem(paramsList.getSelectionIndex(), name + " (" + minorType + ")");

                    editParamButton.setEnabled(true);
                    removeParamButton.setEnabled(true);

                    dialog.close();
                }
            });

            dialog.setDefaultButton(okButton);
        }

        dialog.open();
    }

    private void showInsertFormulaDialog() {

        final Shell dialog = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
        dialog.setText("Insert Formula");
        dialog.setSize(500, 240);

        FormLayout fl = new FormLayout();
        fl.marginWidth = 5;
        fl.marginHeight = 5;
        fl.spacing = 5;
        dialog.setLayout(fl);

        FormData fd;
        Button insertButton;
        Button cancelButton;
        final List formulaList;

        {
            fd = new FormData();
            fd.left = new FormAttachment(0);
            fd.right = new FormAttachment(100);
            fd.top = new FormAttachment(0);
            fd.bottom = new FormAttachment(100, -35);

            formulaList = new List(dialog, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            formulaList.setLayoutData(fd);
            formulaList.setFont(codeFont);

            // TODO: Populate formula list with actual snippet formula objects from environment.
            formulaList.add("${import(name)}    Adds an import to the file.");
            formulaList.add("${iter}            Inserts a free name for an iterator.");
            formulaList.add("${freeName(base)}  Inserts a free variable name based on a base name.");
            formulaList.add("${elemType(var)}   Inserts the element type of a loopable variable.");
            formulaList.add("${snipType(snip)}  Inserts the snippet type of a nested argument.");
            formulaList.add("${helper}          Designates beginning of a helper class.");
            formulaList.add("${endHelper}       Designates end of a helper class.");
            formulaList.add("${cursor}          Designates final cursor position.");
            formulaList.add("${dollar}          Inserts a dollar sign.");
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(100, -95);
            fd.right = new FormAttachment(100);
            fd.bottom = new FormAttachment(100);

            cancelButton = new Button(dialog, SWT.NONE);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(fd);

            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    dialog.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    dialog.close();
                }
            });
        }

        {
            fd = new FormData();
            fd.left = new FormAttachment(cancelButton, -95, SWT.LEFT);
            fd.right = new FormAttachment(cancelButton);
            fd.bottom = new FormAttachment(100);

            insertButton = new Button(dialog, SWT.NONE);
            insertButton.setText("Insert");
            insertButton.setLayoutData(fd);

            insertButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    addFormula();
                    dialog.close();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    addFormula();
                    dialog.close();
                }

                public void addFormula() {

                    String selection = formulaList.getSelection()[0];
                    String formula = selection.substring(0, selection.indexOf(' '));

                    Point highlight = codeText.getSelectionRange();
                    String code = codeText.getText();
                    String prefix = code.substring(0, highlight.x);
                    String suffix = code.substring(highlight.x + highlight.y, code.length());

                    codeText.setText(prefix + formula + suffix);
                    codeText.setSelection(prefix.length(), prefix.length() + formula.length());
                }
            });

            dialog.setDefaultButton(insertButton);
        }

        dialog.open();
    }

    /**
     * Check a search pattern for errors.
     * 
     * @param pattern
     *            The pattern to check.
     * @return Whether or not the pattern is OK.
     */
    private boolean checkPattern(String pattern) {

        ArrayList<String> paramTokens = getParameterTokens(pattern);

        for (int i = 0; i < paramTokens.size(); i++) {
            for (int j = 0; j < paramTokens.size(); j++) {

                if (i != j && paramTokens.get(i).equals(paramTokens.get(j))) {
                    showErrorDialog("Pattern contains duplicate parameters.");
                    return false;
                }
            }
        }

        int numEqual = 0;

        for (int i = 0; i < effect.numPatterns(); i++) {

            String otherPattern = effect.getPattern(i);
            if (otherPattern.equals(pattern))
                numEqual++;

            ArrayList<String> otherParamTokens = getParameterTokens(effect.getPattern(i));

            if (!compareStringLists(paramTokens, otherParamTokens)) {
                showErrorDialog("Two or more patterns contain different parameters.");
                return false;
            }
        }

        if (numEqual >= 2) {
            showErrorDialog("Duplicate patterns detected.");
            return false;
        }

        return true;
    }

    /**
     * Check a snippet parameter for errors.
     * 
     * @param param
     *            The parameter to check.
     * @return Whether or not the parameter is OK.
     */
    private boolean checkParameter(EffectParameter param) {

        for (int i = 0; i < effect.numParameters(); i++) {

            EffectParameter otherParam = effect.getParameter(i);
            if (otherParam == param)
                continue;

            if (otherParam.getName().equals(param.getName())) {
                showErrorDialog("Duplicate parameters detected.");
                return false;
            }
        }

        return true;
    }

    /**
     * Submit changes to an effect, or submit a new effect.
     */
    private void submitEffect() {

        // Check to see there is at least one pattern.
        if (effect.numPatterns() == 0) {
            showErrorDialog("Snippet must specify at least one pattern.");
            return;
        }

        // Check each pattern for errors.
        for (int i = 0; i < effect.numPatterns(); i++) {
            if (!checkPattern(effect.getPattern(i)))
                return;
        }

        // Check each parameter for errors.
        for (int i = 0; i < effect.numParameters(); i++) {
            if (!checkParameter(effect.getParameter(i)))
                return;
        }

        ArrayList<String> paramTokens = getParameterTokens(effect.getPattern(0));
        ArrayList<String> paramNames = new ArrayList<String>();

        for (EffectParameter param : effect.getParameters()) {
            paramNames.add(param.getName());
        }

        Collections.sort(paramNames);

        // Check compatibility between parameters and patterns.
        if (!compareStringLists(paramTokens, paramNames)) {
            showErrorDialog("Parameters in patterns do not match the parameter list.");
            return;
        }

        // Make sure snippet code is not blank.
        if (codeText.getText().length() == 0) {
            showErrorDialog("Snippet does not have any code.");
            return;
        }

        // The major type is fixed for now, since the plugin is not using it.
        String majorType = "expr";

        effect.setEnvironmentName(activeEnv.getName());
        effect.setMajorType(majorType);
        effect.setMinorType(minorTypeText.getText());
        effect.setCode(codeText.getText());
        effect.setSummary(sumText.getText());

        if (editing) {
            GsonUtil.serialize(effect, new File(this.editFilePath));
            showInfoDialog("Save code snippet successfully.");
            shell.close();
        } else {
            GsonUtil.serialize(effect, new File(this.editFilePath));
            showInfoDialog("Save code snippet successfully, refresh to add into index file.");
            shell.close();
        }
    }

    private void showErrorDialog(String error) {

        MessageBox popup = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                SWT.ICON_ERROR | SWT.OK | SWT.APPLICATION_MODAL);

        popup.setText(ViewConstants.dialogTitle);
        popup.setMessage(error);
        popup.open();
    }

    private void showInfoDialog(String info) {

        MessageBox popup = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                SWT.ICON_INFORMATION | SWT.OK | SWT.APPLICATION_MODAL);

        popup.setText(ViewConstants.dialogTitle);
        popup.setMessage(info);
        popup.open();
    }

    /**
     * Returns a list of all the tokens in a search pattern that are parameters.
     * 
     * @param pattern
     *            The pattern to analyze.
     * @return A list of all the tokens in a search pattern that are parameters.
     */
    private ArrayList<String> getParameterTokens(String pattern) {

        ArrayList<String> tokens = new ArrayList<String>();

        for (String token : pattern.split(" ")) {
            if (token.startsWith("$"))
                tokens.add(token.substring(1));
        }

        Collections.sort(tokens);

        return tokens;
    }

    /**
     * Checks to see if two lists of strings are identical.
     * 
     * @param a
     * @param b
     * @return
     */
    private boolean compareStringLists(ArrayList<String> a, ArrayList<String> b) {

        if (a.size() != b.size())
            return false;

        for (int i = 0; i < a.size(); i++) {

            if (!a.get(i).equals(b.get(i)))
                return false;
        }

        return true;
    }
}

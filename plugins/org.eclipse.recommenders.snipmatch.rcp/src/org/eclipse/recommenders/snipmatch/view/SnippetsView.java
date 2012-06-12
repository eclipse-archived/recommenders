/**
 * Copyright (c) 2012 Cheng Chen
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Cheng Chen - initial API and implementation and/or initial documentation
*/

package org.eclipse.recommenders.snipmatch.view;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.recommenders.snipmatch.core.Effect;
import org.eclipse.recommenders.snipmatch.core.SummaryFileMap;
import org.eclipse.recommenders.snipmatch.preferences.PreferenceConstants;
import org.eclipse.recommenders.snipmatch.rcp.SnipMatchPlugin;
import org.eclipse.recommenders.snipmatch.rcp.SubmitBox;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.reflect.TypeToken;

public class SnippetsView extends ViewPart {

    private final String[] SNIPPET_TYPE = new String[] { "All", "Public", "Personal" };
    private List snippetsList;
    private Text searchText;

    private Button editButton;
    private Button enableButton;
    private Button disableButton;
    private Button deleteButton;

    private Map<String, SummaryFileMap> snippetsMap = new HashMap<String, SummaryFileMap>();
    private String currentFilePath = null;

    private SubmitBox editBox = null;

    @Override
    public void createPartControl(Composite parent) {
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        parent.setLayout(layout);

        Composite searchComposite = new Composite(parent, SWT.NONE);
        createSearchComposite(searchComposite);
        FormData searchFormData = new FormData();
        searchFormData.left = new FormAttachment(0, 5);
        searchFormData.right = new FormAttachment(100, -5);
        searchComposite.setLayoutData(searchFormData);

        Composite listComposite = new Composite(parent, SWT.NONE);
        FormData resultFormData = new FormData();
        resultFormData.left = new FormAttachment(0, 5);
        resultFormData.top = new FormAttachment(0, 35);
        resultFormData.right = new FormAttachment(100, -5);
        resultFormData.bottom = new FormAttachment(100, -5);
        listComposite.setLayoutData(resultFormData);
        createSnippetsListComposite(listComposite);

        loadSnippetsList();
        editBox = new SubmitBox();
    }

    private void createSearchComposite(Composite parent) {
        FormLayout layout = new FormLayout();
        parent.setLayout(layout);

        searchText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 10);
        formData.bottom = new FormAttachment(100, -5);
        formData.right = new FormAttachment(100, -100);
        searchText.setLayoutData(formData);
        searchText.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.keyCode == SWT.CR) {
                    e.doit = false;
                    searchAction();
                }
            }

        });

        Composite menuComposite = new Composite(parent, SWT.NONE);
        FormData buttonFormData = new FormData();
        buttonFormData.width = 90;
        buttonFormData.top = new FormAttachment(0, 3);
        buttonFormData.right = new FormAttachment(100, -10);
        menuComposite.setLayoutData(buttonFormData);
        menuComposite.setLayout(new RowLayout(SWT.VERTICAL));
        final Button b1 = new Button(menuComposite, SWT.NONE);
        b1.setLayoutData(new RowData(80, 25));
        b1.setText("Search");
        b1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                searchAction();
            }
        });
    }

    private void searchAction() {
        if (!searchText.getText().trim().isEmpty()) {
            searchSnippets(searchText.getText().trim().toLowerCase());
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }else
            loadSnippetsList();
    }

    private void createSnippetsListComposite(final Composite parent) {
        parent.setLayout(new FormLayout());

        snippetsList = new List(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0, 10);
        formData.right = new FormAttachment(100, -100);
        formData.bottom = new FormAttachment(100, -5);
        snippetsList.setLayoutData(formData);
        snippetsList.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                SummaryFileMap map = snippetsMap.get(snippetsList.getSelection()[0]);
                currentFilePath = map.filePath;
                editButton.setEnabled(true);
                deleteButton.setEnabled(true);

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }

        });

        Composite menuComposite = new Composite(parent, SWT.NONE);
        FormData buttonFormData = new FormData();
        buttonFormData.width = 90;
        buttonFormData.right = new FormAttachment(100, -10);
        menuComposite.setLayoutData(buttonFormData);
        menuComposite.setLayout(new RowLayout(SWT.VERTICAL));
        Button button1 = new Button(menuComposite, SWT.PUSH);
        button1.setLayoutData(new RowData(80, 25));
        button1.setText("Refresh");
        button1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                SnipMatchPlugin.getDefault().getSearchEngine().updateIndex();
                loadSnippetsList();
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
                searchText.setText("");
            }
        });

        Button newButton = new Button(menuComposite, SWT.PUSH);
        newButton.setLayoutData(new RowData(80, 25));
        newButton.setText("New");
        newButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                String path = SnipMatchPlugin.getDefault().getPreferenceStore()
                        .getString(PreferenceConstants.SNIPPETS_STORE_DIR);
                editBox.show("", new File(path, System.currentTimeMillis() + ".json").getPath(), null);
            }
        });

        editButton = new Button(menuComposite, SWT.PUSH);
        editButton.setLayoutData(new RowData(80, 25));
        editButton.setText("Edit");
        editButton.setEnabled(false);
        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                if (snippetsList.getSelectionCount() > 0) {
                    SummaryFileMap map = snippetsMap.get(snippetsList.getSelection()[0]);
                    File jsonFile = new File(map.filePath);
                    if (jsonFile.exists()) {
                        Effect toEdit = GsonUtil.deserialize(jsonFile, Effect.class);
                        editBox.show(getSummarylabel(map), map.filePath, toEdit);
                    }
                }
            }
        });

        deleteButton = new Button(menuComposite, SWT.PUSH);
        deleteButton.setLayoutData(new RowData(80, 25));
        deleteButton.setText("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                if (snippetsList.getSelectionCount() > 0) {
                    SummaryFileMap map = snippetsMap.get(snippetsList.getSelection()[0]);
                    File jsonFile = new File(map.filePath);
                    if (jsonFile.exists()
                            && MessageDialog.openConfirm(parent.getShell(), ViewConstants.dialogTitle,
                                    ViewConstants.deleteConfirm)) {
                        jsonFile.delete();
                        MessageDialog.openInformation(parent.getShell(), ViewConstants.dialogTitle,
                                ViewConstants.deleteSuccess);
                    }
                }
            }
        });
    }

    private void loadSnippetsList() {
        snippetsList.removeAll();
        String indexFilePath = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_INDEX_FILE);
        File indexFile = new File(indexFilePath);
        if (indexFile.exists()) {
            Type listType = new TypeToken<java.util.List<SummaryFileMap>>() {
            }.getType();
            java.util.List<SummaryFileMap> sfMapList = GsonUtil.deserialize(indexFile, listType);
            for (SummaryFileMap element : sfMapList) {
                String summary = getSummarylabel(element);
                snippetsList.add(summary);
                snippetsMap.put(summary, element);
            }
        }
    }

    private void searchSnippets(String text) {
        snippetsList.removeAll();
        String indexFilePath = SnipMatchPlugin.getDefault().getPreferenceStore()
                .getString(PreferenceConstants.SNIPPETS_INDEX_FILE);
        File indexFile = new File(indexFilePath);
        if (indexFile.exists()) {
            Type listType = new TypeToken<java.util.List<SummaryFileMap>>() {
            }.getType();
            java.util.List<SummaryFileMap> sfMapList = GsonUtil.deserialize(indexFile, listType);
            for (SummaryFileMap element : sfMapList) {
                if (element.summary.toLowerCase().contains(text))
                    snippetsList.add(getSummarylabel(element));
            }
        }
    }

    @Override
    public void setFocus() {
        snippetsList.setFocus();
    }

    private String getSummarylabel(SummaryFileMap element) {
        String[] summaryArray = element.summary.split(";");
        String summary = summaryArray.length > 0 ? summaryArray[0] : element.summary;
        return summary;
    }
}

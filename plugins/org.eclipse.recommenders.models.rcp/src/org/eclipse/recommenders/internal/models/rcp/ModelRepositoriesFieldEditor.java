/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static org.eclipse.recommenders.internal.models.rcp.Constants.PREF_REPOSITORY_URL_LIST;

import java.net.URI;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.recommenders.internal.models.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Uris;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.Lists;

public class ModelRepositoriesFieldEditor extends FieldEditor {

    private TableViewer tableViewer;

    private Composite buttonBox;
    private Button addButton;
    private Button editButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    private final ModelsRcpPreferences preferences;
    private final SharedImages images;

    public ModelRepositoriesFieldEditor(String name, Composite parent, ModelsRcpPreferences preferences,
            SharedImages images) {
        super(name, Messages.FIELD_LABEL_REPOSITORY_URIS, parent);
        this.preferences = preferences;
        this.images = images;
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control control = getLabelControl(parent);
        GridDataFactory.swtDefaults().span(numColumns, 1).applyTo(control);

        tableViewer = getTableControl(parent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(numColumns - 1, 1).grab(true, true)
                .applyTo(tableViewer.getTable());

        buttonBox = getButtonControl(parent);
        updateButtonStatus();
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBox);
    }

    private TableViewer getTableControl(Composite parent) {
        final TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        tableViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                String repositoryUrl = (String) element;
                URI uri = Uris.parseURI(repositoryUrl).orNull();
                if (uri == null) {
                    return repositoryUrl;
                }
                return Uris.toStringWithMaskedPassword(uri, '*');
            }

            @Override
            public Image getImage(Object element) {
                String repositoryUrl = (String) element;
                URI uri = Uris.parseURI(repositoryUrl).orNull();
                if (uri == null) {
                    return images.getImage(SharedImages.Images.OBJ_REPOSITORY);
                } else if (Uris.isPasswordProtected(uri)) {
                    return images.getImage(SharedImages.Images.OBJ_LOCKED_REPOSITORY);
                } else if (preferences.hasPassword(repositoryUrl)) {
                    return images.getImage(SharedImages.Images.OBJ_LOCKED_REPOSITORY);
                } else {
                    return images.getImage(SharedImages.Images.OBJ_REPOSITORY);
                }
            }
        });

        tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtonStatus();
            }
        });

        tableViewer.getTable().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TableItem item = tableViewer.getTable().getItem(new Point(e.x, e.y));
                if (item == null) {
                    return;
                }

                Rectangle bounds = item.getBounds();
                boolean isClickOnCheckbox = e.x < bounds.x;
                if (isClickOnCheckbox) {
                    return;
                }

                String selectedRepository = getSelectedRepository();
                editRepository(selectedRepository);
                updateButtonStatus();
            }
        });
        return tableViewer;
    }

    private Composite getButtonControl(Composite parent) {
        Composite box = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(box);

        addButton = createButton(box, Messages.PREFPAGE_BUTTON_ADD);
        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewRepository();
                updateButtonStatus();
            }
        });

        editButton = createButton(box, Messages.PREFPAGE_BUTTON_EDIT);
        editButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editRepository(getSelectedRepository());
                updateButtonStatus();
            }
        });

        editButton.setEnabled(false);

        removeButton = createButton(box, Messages.PREFPAGE_BUTTON_REMOVE);
        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeRepository(getSelectedRepository());
                updateButtonStatus();
            }
        });

        upButton = createButton(box, Messages.PREFPAGE_BUTTON_UP);
        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveRepositoryUp(getSelectedRepository());
                updateButtonStatus();
            }
        });
        downButton = createButton(box, Messages.PREFPAGE_BUTTON_DOWN);
        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                moveRepositoryDown(getSelectedRepository());
                updateButtonStatus();
            }
        });

        return box;
    }

    private String getSelectedRepository() {
        List<String> tableInput = getTableViewerInput();
        if (tableInput == null) {
            return null;
        }

        int index = tableViewer.getTable().getSelectionIndex();
        if (index < 0) {
            return null;
        }
        return tableInput.get(index);
    }

    private List<String> getTableViewerInput() {
        return (List<String>) tableViewer.getInput();
    }

    private void updateButtonStatus() {
        int selectionIndex = tableViewer.getTable().getSelectionIndex();
        String selectedRepository = getSelectedRepository();
        if (selectedRepository == null) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
            return;
        }
        editButton.setEnabled(true);
        removeButton.setEnabled(true);
        upButton.setEnabled(selectionIndex > 0);
        downButton.setEnabled(selectionIndex < tableViewer.getTable().getItemCount() - 1);
    }

    private void removeRepository(String selectedRepository) {
        List<String> list = getTableViewerInput();
        list.remove(selectedRepository);
        tableViewer.refresh();
    }

    private void addNewRepository() {
        RepositoryDetailsDialog newRepositoryDialog = new RepositoryDetailsDialog(null, null, getTableViewerInput(),
                preferences);
        if (newRepositoryDialog.open() == Window.OK) {
            String newRepositoryUrl = newRepositoryDialog.getRepositoryUrl();
            List<String> list = getTableViewerInput();
            list.add(newRepositoryUrl);
            tableViewer.refresh();
        }
    }

    private void editRepository(String selectedRepository) {
        RepositoryDetailsDialog editRepositoryDialog = new RepositoryDetailsDialog(null, selectedRepository,
                getTableViewerInput(), preferences);
        if (editRepositoryDialog.open() == Window.OK) {
            String updatedRepositoryUrl = editRepositoryDialog.getRepositoryUrl();
            List<String> list = getTableViewerInput();
            int indexOfOriginalRepository = list.indexOf(selectedRepository);
            list.remove(indexOfOriginalRepository);
            list.add(indexOfOriginalRepository, updatedRepositoryUrl);
            tableViewer.refresh();
        }
    }

    private void moveRepositoryUp(String selectedRepository) {
        List<String> list = getTableViewerInput();
        int indexOfRepository = list.indexOf(selectedRepository);
        list.remove(indexOfRepository);
        list.add(indexOfRepository - 1, selectedRepository);
        tableViewer.refresh();
    }

    private void moveRepositoryDown(String selectedRepository) {
        List<String> list = getTableViewerInput();
        int indexOfRepository = list.indexOf(selectedRepository);
        list.remove(indexOfRepository);
        list.add(indexOfRepository + 1, selectedRepository);
        tableViewer.refresh();
    }

    private Button createButton(Composite box, String text) {
        Button button = new Button(box, SWT.PUSH);
        button.setText(text);

        int widthHint = Math.max(convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH),
                button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(widthHint, SWT.DEFAULT).applyTo(button);

        return button;
    }

    @Override
    protected void doLoad() {
        tableViewer.setInput(Lists.newArrayList(preferences.remotes));
    }

    @Override
    protected void doLoadDefault() {
        String defaultRemotes = getPreferenceStore().getDefaultString(PREF_REPOSITORY_URL_LIST);
        tableViewer.setInput(Lists.newArrayList(ModelsRcpPreferences.splitRemoteRepositoryString(defaultRemotes)));
    }

    @Override
    protected void doStore() {
        List<String> repositories = getTableViewerInput();
        getPreferenceStore().setValue(PREF_REPOSITORY_URL_LIST,
                ModelsRcpPreferences.joinRemoteRepositoriesToString(repositories));
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }
}

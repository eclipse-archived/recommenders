/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Daniel Haftstein - added support for multiple stacktraces
 */
package org.eclipse.recommenders.internal.stacktraces.rcp;

import static org.eclipse.recommenders.internal.stacktraces.rcp.Constants.HELP_URL;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReport;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.ErrorReports;
import org.eclipse.recommenders.internal.stacktraces.rcp.model.Settings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class DetailsWizardPage extends WizardPage {

    private TableViewer tableViewer;
    private StyledText messageText;
    private IObservableList errors;
    private ErrorReport activeSelection;

    private static final Image ERROR_ICON = PlatformUI.getWorkbench().getSharedImages()
            .getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
    private StyledText commentText;
    private Settings settings;

    protected DetailsWizardPage(IObservableList errors, Settings settings) {
        super(DetailsWizardPage.class.getName());
        this.errors = errors;
        this.settings = settings;
        setTitle(Messages.PREVIEWPAGE_TITLE);
        setDescription(Messages.PREVIEWPAGE_DESC);
    }

    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(true).applyTo(container);

        Composite tableComposite = createTableComposite(container);
        GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).minSize(150, SWT.DEFAULT).span(1, 3).grab(false, true)
        .applyTo(tableComposite);

        Composite messageComposite = createMessageComposite(container);
        GridDataFactory.fillDefaults().span(2, 2).grab(true, true).applyTo(messageComposite);

        Composite commentComposite = createCommentComposite(container);
        GridDataFactory.fillDefaults().span(2, 1).hint(400, SWT.DEFAULT).grab(true, false).applyTo(commentComposite);
        setControl(container);
    }

    private Composite createTableComposite(Composite container) {
        Composite tableComposite = new Composite(container, SWT.NONE);
        tableViewer = new TableViewer(tableComposite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
                | SWT.BORDER);
        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
        column.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                ErrorReport event = (ErrorReport) element;
                return event.getStatus().getMessage();
            }

            @Override
            public Image getImage(Object element) {
                return ERROR_ICON;
            }
        });
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(100));
        tableComposite.setLayout(tableColumnLayout);
        tableViewer.setContentProvider(new ObservableListContentProvider());
        tableViewer.setInput(errors);
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (!event.getSelection().isEmpty()) {
                    IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                    activeSelection = (ErrorReport) selection.getFirstElement();
                    ErrorReport copy = ErrorReports.copy(activeSelection);
                    copy.setName(settings.getName());
                    copy.setEmail(settings.getEmail());
                    messageText.setText(ErrorReports.prettyPrint(copy));
                    String comment = activeSelection.getComment();
                    commentText.setText(comment == null ? "" : comment);
                }
            }

        });
        return tableComposite;
    }

    private Composite createMessageComposite(Composite container) {
        Composite messageComposite = new Composite(container, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(messageComposite);
        Label messageLabel = new Label(messageComposite, SWT.FILL);
        messageLabel.setText(Messages.PREVIEWPAGE_LABEL_MESSAGE);
        GridDataFactory.fillDefaults().applyTo(messageLabel);
        messageText = new StyledText(messageComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        messageText.setEditable(false);
        messageText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        GridDataFactory.fillDefaults().minSize(150, 1).hint(300, 300).grab(true, true).applyTo(messageText);
        return messageComposite;
    }

    private Composite createCommentComposite(Composite container) {
        Composite commentComposite = new Composite(container, SWT.FILL);
        GridLayoutFactory.fillDefaults().applyTo(commentComposite);
        Label commentLabel = new Label(commentComposite, SWT.NONE);
        commentLabel.setText(Messages.PREVIEWPAGE_LABEL_COMMENT);
        GridDataFactory.fillDefaults().applyTo(commentLabel);
        commentText = new StyledText(commentComposite, SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
        commentText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (activeSelection != null) {
                    activeSelection.setComment(commentText.getText());
                }
            }
        });
        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 75).applyTo(commentText);
        return commentComposite;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible && !errors.isEmpty()) {
            StructuredSelection selection = new StructuredSelection(tableViewer.getElementAt(0));
            tableViewer.setSelection(selection, true);
        }
        super.setVisible(visible);
    }

    @Override
    public void performHelp() {
        Browsers.openInExternalBrowser(HELP_URL);
    }
}

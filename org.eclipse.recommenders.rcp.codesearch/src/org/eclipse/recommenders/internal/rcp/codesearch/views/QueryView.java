/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.part.ViewPart;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class QueryView extends ViewPart {
    public static final String ID = QueryView.class.getName();
    private ScrolledComposite container;
    private SourceViewer viewer;
    private Action sendQueryAction;
    private IJavaProject issuingProject;
    private final CodesearchController controller;

    @Inject
    public QueryView(final CodesearchController controller) {
        this.controller = controller;

    }

    @Override
    public void createPartControl(final Composite parent) {
        container = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        createSourceViewer();
        container.setContent(viewer.getControl());
        container.setExpandHorizontal(true);
        container.setExpandVertical(true);
        createActions();
    }

    private void createActions() {
        sendQueryAction = new Action("Send Code Search Request") {
            @Override
            public void run() {
                final Request request = getInput();
                controller.sendRequest(request, issuingProject);
            }
        };
        sendQueryAction.setToolTipText("Submits the view's code search query");
        final ImageDescriptor id = WorkbenchImages.getWorkbenchImageDescriptor("/progress/progress_task.gif"); //$NON-NLS-1$
        sendQueryAction.setImageDescriptor(id);
        getViewSite().getActionBars().getToolBarManager().add(sendQueryAction);
    }

    private void createSourceViewer() {
        viewer = new SourceViewer(container, null, SWT.BORDER | SWT.V_SCROLL);
        final Control viewerControl = viewer.getControl();
        //
        final TextSourceViewerConfiguration configuration = new TextSourceViewerConfiguration();
        viewer.configure(configuration);
        viewer.setEditable(true);
        viewer.getTextWidget().setWordWrap(false);
        final Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        viewer.getTextWidget().setFont(font);
        viewerControl.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(200, 300).create());
    }

    public void setInput(final Request request, final IJavaProject issuingProject) {
        this.issuingProject = issuingProject;
        final Document doc = new Document();
        doc.set(GsonUtil.serialize(request));
        viewer.setDocument(doc);
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    public Request getInput() {
        final String json = viewer.getDocument().get();
        final Request request = GsonUtil.deserialize(json, Request.class);
        return request;
    }
}

/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.views;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;
import org.eclipse.recommenders.commons.codesearch.Response;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class ExamplesView extends ViewPart {
    public static final String ID = ExamplesView.class.getName();
    private ScrolledComposite scrollContainer;
    private Composite container;
    private Request request;
    private Response reply;

    @Override
    public void createPartControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.FILL);
        composite.setBackground(JavaUI.getColorManager().getColor(new RGB(255, 255, 255)));
        GridLayout gl = GridLayoutFactory.fillDefaults().spacing(1, 1).create();
        parent.setLayout(gl);
        gl.numColumns = 1;
        GridData gd = new GridData(GridData.FILL_BOTH);
        parent.setLayoutData(gd);
        gl = GridLayoutFactory.fillDefaults().spacing(1, 1).create();
        gl.numColumns = 2;
        composite.setLayout(gl);
        final Label label = new Label(composite, SWT.NONE);
        label.setText("Filter: ");
        final Text filterText = new Text(composite, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.minimumWidth = 300;
        filterText.setLayoutData(gd);
        filterText.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(final KeyEvent e) {
                disposeOldSourceViewers();
                createNewSourceViewers(filterText.getText());
            }

            @Override
            public void keyPressed(final KeyEvent e) {
            }
        });
        scrollContainer = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        container = new Composite(scrollContainer, SWT.NONE);
        container.setLayout(GridLayoutFactory.fillDefaults().spacing(1, 1).create());
        gd = new GridData(GridData.FILL_BOTH);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        scrollContainer.setLayoutData(gd);
        container.setBackground(JavaUI.getColorManager().getColor(new RGB(255, 255, 255)));
        scrollContainer.setContent(container);
        scrollContainer.setExpandHorizontal(true);
        scrollContainer.setExpandVertical(true);
    }

    public void setInput(final Request request, final Response reply) {
        this.request = request;
        this.reply = reply;
        disposeOldSourceViewers();
        createNewSourceViewers("");
    }

    private void disposeOldSourceViewers() {
        for (final Control child : container.getChildren()) {
            child.dispose();
        }
    }

    private void createNewSourceViewers(final String searchCritera) {
        if (reply.proposals.size() == 0) {
        }
        for (final Proposal codeExample : reply.proposals) {
            final ExampleSummaryPage page = new SimpleSummaryPage();
            page.createControl(container);
            page.setInput(request, reply, codeExample, searchCritera);
        }
        final int minWidth = 300;
        final int minHeight = reply.proposals.size() * 80;
        scrollContainer.setMinSize(minWidth, minHeight);
        scrollContainer.layout(true, true);
    }

    @Override
    public void setFocus() {
        container.setFocus();
    }
}

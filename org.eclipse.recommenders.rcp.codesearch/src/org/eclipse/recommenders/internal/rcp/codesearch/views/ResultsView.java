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

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.google.inject.Inject;

public class ResultsView extends ViewPart {
    public static final String ID = ResultsView.class.getName();
    private ScrolledComposite rootContainer;
    Composite summariesContainer;
    private final CodesearchController controller;

    @Inject
    public ResultsView(final CodesearchController controller) {
        this.controller = controller;
    }

    @Override
    public void createPartControl(final Composite parent) {
        rootContainer = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        rootContainer.setBackground(JavaUI.getColorManager().getColor(new RGB(255, 255, 255)));
        rootContainer.setExpandHorizontal(true);
        rootContainer.setExpandVertical(true);
        rootContainer.setLayout(GridLayoutFactory.fillDefaults().create());
        rootContainer.setLayoutData(GridDataFactory.fillDefaults().create());

        // Speed up scrolling when using a wheel mouse
        final ScrollBar vBar = rootContainer.getVerticalBar();
        vBar.setIncrement(10);

        summariesContainer = new Composite(rootContainer, SWT.NONE);
        summariesContainer.setBackground(JavaUI.getColorManager().getColor(new RGB(255, 255, 255)));
        summariesContainer.setLayout(GridLayoutFactory.fillDefaults().create());
        summariesContainer.setLayoutData(GridDataFactory.fillDefaults().create());
        rootContainer.setContent(summariesContainer);

        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(),
                new CopyAction(this, controller));
    }

    @Override
    public void setFocus() {
        summariesContainer.setFocus();
    }

    public Composite getSummaryArea() {
        return summariesContainer;
    }

    public void update() {

        final Point preferredSize = summariesContainer.computeSize(rootContainer.getParent().getSize().x - 20,
                SWT.DEFAULT);
        rootContainer.setMinSize(preferredSize);
        rootContainer.layout(true, true);
    }
}

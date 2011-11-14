/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.views.cu;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.utils.annotations.RequiresUIThread;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class CompilationUnitView extends ViewPart {
    private TreeViewer viewer;

    public CompilationUnitView() {
    }

    @Override
    public void createPartControl(final Composite parent) {
        viewer = new TreeViewer(parent, SWT.BORDER);
        viewer.setSorter(new ViewerSorter());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setContentProvider(new WorkbenchContentProvider());
    }

    @Override
    public void setFocus() {
    }

    @RequiresUIThread
    public void setInput(final CompilationUnit cu) {
        if (viewer.getInput() != cu) {
            viewer.setInput(cu);
        }
    }
}

/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc;

import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.view.ExtDocView;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com.google.inject.Inject;

final class UIManager implements IExtendedSelectionListener {

    private static ExtDocView extDocView;
    private static boolean isViewVisible = true;
    private static IJavaElementSelection lastSelection;

    @Inject
    public UIManager(final ExtDocView extDocView) {
        UIManager.extDocView = extDocView;
        extDocView.getSite().getPage().addPartListener(new ViewListener());
    }

    @Override
    public void update(final IJavaElementSelection selection) {
        if (isViewVisible && !isEqualToLastSelection(selection)) {
            extDocView.update(selection);
        }
        lastSelection = selection;
    }

    private boolean isEqualToLastSelection(final IJavaElementSelection selection) {
        if (lastSelection == null) {
            return false;
        }
        if (lastSelection.getElementLocation() != selection.getElementLocation()) {
            return false;
        }
        if (!lastSelection.getJavaElement().equals(selection.getJavaElement())) {
            return false;
        }
        if (!lastSelection.getEditor().equals(selection.getEditor())) {
            return false;
        }
        return true;
    }

    private static final class ViewListener implements IPartListener2 {

        @Override
        public void partActivated(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partBroughtToTop(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partClosed(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partDeactivated(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partOpened(final IWorkbenchPartReference partRef) {
        }

        @Override
        public void partHidden(final IWorkbenchPartReference partRef) {
            if (partRef.getPart(false).equals(extDocView)) {
                isViewVisible = false;
            }
        }

        @Override
        public void partVisible(final IWorkbenchPartReference partRef) {
            if (partRef.getPart(false).equals(extDocView)) {
                isViewVisible = true;
                extDocView.update(lastSelection);
            }
        }

        @Override
        public void partInputChanged(final IWorkbenchPartReference partRef) {
        }

    }

}

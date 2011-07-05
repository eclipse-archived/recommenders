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
import org.eclipse.recommenders.internal.rcp.extdoc.swt.ExtDocView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;

import com.google.inject.Inject;

final class UiManager implements IExtendedSelectionListener {

    private static ExtDocView extDocView;
    private static boolean isViewVisible = true;
    private static IJavaElementSelection lastSelection;

    private boolean hasViewListener;

    @Inject
    UiManager(final ExtDocView extDocView) {
        UiManager.extDocView = extDocView;
    }

    @Override
    public void selectionChanged(final IJavaElementSelection selection) {
        if (!hasViewListener) {
            initViewListener();
        }
        if (isViewVisible && extDocView.isLinkingEnabled() && isUiThread() && !isEqualToLastSelection(selection)) {
            extDocView.selectionChanged(selection);
        }
        lastSelection = selection;
    }

    private static boolean isUiThread() {
        return Display.getCurrent() != null;
    }

    private static boolean isEqualToLastSelection(final IJavaElementSelection selection) {
        if (lastSelection == null) {
            return false;
        }
        if (lastSelection.getElementLocation() != selection.getElementLocation()) {
            return false;
        }
        if (!lastSelection.getJavaElement().equals(selection.getJavaElement())) {
            return false;
        }
        if (lastSelection.getEditor() == null) {
            return selection.getEditor() == null;
        }
        return lastSelection.getEditor().equals(selection.getEditor());
    }

    private void initViewListener() {
        final IWorkbenchPartSite site = extDocView.getSite();
        if (site != null) {
            site.getPage().addPartListener(new ViewListener());
            hasViewListener = true;
        }
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
                extDocView.selectionChanged(lastSelection);
            }
        }

        @Override
        public void partInputChanged(final IWorkbenchPartReference partRef) {
        }

    }

}

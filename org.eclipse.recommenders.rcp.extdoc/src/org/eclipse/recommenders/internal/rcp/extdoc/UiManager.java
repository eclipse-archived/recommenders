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

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.internal.rcp.extdoc.view.ExtDocView;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;

import com.google.inject.Inject;

/**
 * Delegates update requests to the ExtDoc views, registers ExtDoc with the
 * editor's content assistant and stores the current {@link IWorkbenchPartSite}
 * as well as the last {@link IJavaElementSelection}.
 */
@SuppressWarnings("restriction")
public final class UiManager implements IExtendedSelectionListener {

    private final ExtDocView extDocView;
    private final ProviderStore providerStore;
    private final UpdateService updateService;

    private boolean isViewVisible = true;
    private boolean viewHasVisibilityListener;

    private IWorkbenchPartSite currentPartSite;
    private IJavaElementSelection lastSelection;

    @Inject
    UiManager(final ExtDocView extDocView, final ProviderStore providerStore, final UpdateService updateService) {
        this.extDocView = extDocView;
        this.providerStore = providerStore;
        this.updateService = updateService;
    }

    @Override
    public void selectionChanged(final IJavaElementSelection selection) {
        try {
            if (!viewHasVisibilityListener) {
                initViewVisibilityListener();
            }
            if (isViewVisible && extDocView.isLinkingEnabled() && isUiThread() && !isEqualToLastSelection(selection)) {
                extDocView.selectionChanged(selection);
            }
        } catch (final Exception e) {
            ExtDocPlugin.logException(e);
        }
        lastSelection = selection;
    }

    private void initViewVisibilityListener() {
        if (currentPartSite != null) {
            currentPartSite.getPage().addPartListener(new ViewVisibilityListener());
        }
        viewHasVisibilityListener = true;
    }

    private static boolean isUiThread() {
        return Display.getCurrent() != null;
    }

    private boolean isEqualToLastSelection(final IJavaElementSelection selection) {
        return selection == null ? lastSelection == null : selection.equals(lastSelection);
    }

    /**
     * @return The default ExtDoc interface to the current workbench page.
     */
    public Option<IWorkbenchPartSite> getWorkbenchSite() {
        return Option.wrap(currentPartSite);
    }

    /**
     * @return The last user selection that has been observed by ExtDoc.
     */
    public Option<IJavaElementSelection> getLastSelection() {
        return Option.wrap(lastSelection);
    }

    @Override
    public void javaEditorCreated(final JavaEditor editor) {
        final IWorkbenchPartSite site = editor.getSite();
        if (site != null) {
            currentPartSite = site;
        }
        ExtDocCodeAssistantHover.installToEditor(editor, this, providerStore, updateService);
    }

    private final class ViewVisibilityListener implements IPartListener2 {

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

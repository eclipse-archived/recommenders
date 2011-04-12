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
package org.eclipse.recommenders.internal.rcp;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.recommenders.rcp.IEditorChangedListener;
import org.eclipse.recommenders.rcp.IEditorDashboard;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class EditorTrackingService {
    private final Map<JavaEditor, IEditorDashboard> boards = Maps.newConcurrentMap();

    private final List<IEditorChangedListener> listener;

    private abstract class BackgroundNotificationJob extends WorkspaceJob {
        public BackgroundNotificationJob(final String title) {
            super(title);
            setSystem(true);
        }

        @Override
        public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
            monitor.beginTask("Notifying", listener.size());
            for (final IEditorChangedListener l : listener) {
                try {
                    monitor.subTask(subTaskTitle(l));
                    notifyListener(l, new SubProgressMonitor(monitor, 1));
                    monitor.worked(1);
                } catch (final CoreException x) {
                    RecommendersPlugin.log(x);
                } catch (final Exception x) {
                    RecommendersPlugin.logError(x, "Exception occurred during notification.");
                }
            }
            monitor.done();
            return Status.OK_STATUS;
        }

        private String subTaskTitle(final IEditorChangedListener l) {
            return format("%s", l.getClass().getSimpleName());
        }

        abstract protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                throws CoreException;
    }

    @Inject
    public EditorTrackingService(final List<IEditorChangedListener> listener) {
        this.listener = listener;
        waitForWorkbench();
    }

    private final IPartListener2 partListener = new IPartListener2() {
        @Override
        public void partActivated(final IWorkbenchPartReference partRef) {
            if (isJavaEditorRef(partRef)) {
                final IEditorDashboard board = findOrCreateDashboard(getJavaEditor(partRef));
                fireEditorActivated(board);
            }
        }

        @Override
        public void partDeactivated(final IWorkbenchPartReference partRef) {
            if (isJavaEditorRef(partRef)) {
                final IEditorDashboard board = findOrCreateDashboard(getJavaEditor(partRef));
                fireEditorDeactivated(board);
            }
        }

        @Override
        public void partClosed(final IWorkbenchPartReference partRef) {
            if (isJavaEditorRef(partRef)) {
                final IEditorDashboard board = findOrCreateDashboard(getJavaEditor(partRef));
                fireEditorClosed(board);
                releaseDashboard(partRef);
            }
        }

        @Override
        public void partOpened(final IWorkbenchPartReference partRef) {
            if (isJavaEditorRef(partRef)) {
                final IEditorDashboard board = findOrCreateDashboard(getJavaEditor(partRef));
                fireEditorOpened(board);
            }
        }

        @Override
        public void partHidden(final IWorkbenchPartReference partRef) {
            if (isJavaEditorRef(partRef)) {
                final IEditorDashboard board = findOrCreateDashboard(getJavaEditor(partRef));
                fireEditorHidden(board);
            }
        }

        @Override
        public void partVisible(final IWorkbenchPartReference partRef) {
            if (isJavaEditorRef(partRef)) {
                final IEditorDashboard board = findOrCreateDashboard(getJavaEditor(partRef));
                fireEditorVisble(board);
            }
        }

        @Override
        public void partInputChanged(final IWorkbenchPartReference partRef) {
            // we are not interested in such events.
        }

        @Override
        public void partBroughtToTop(final IWorkbenchPartReference partRef) {
            /*
             * see javadoc of partBroughtToTop: it's basically the same as
             * partActivated except this method is called whenever an element is
             * activated programmatically and not caused by a user action .
             */
            // partActivated(partRef);
        }

        private boolean isJavaEditorRef(final IWorkbenchPartReference partRef) {
            return getJavaEditor(partRef) != null;
        }

        private JavaEditor getJavaEditor(final IWorkbenchPartReference partRef) {
            if (partRef == null) {
                return null;
            }
            final IWorkbenchPart part = partRef.getPart(false);
            return (JavaEditor) (part instanceof JavaEditor ? part : null);
        }

        private synchronized IEditorDashboard findOrCreateDashboard(final JavaEditor editor) {
            ensureIsNotNull(editor);
            IEditorDashboard board = boards.get(editor);
            if (board == null) {
                board = EditorDashboard.create(editor);
                boards.put(editor, board);
            }
            return board;
        }

        private void releaseDashboard(final IWorkbenchPartReference partRef) {
            boards.remove(partRef);
        }
    };

    private void waitForWorkbench() {
        final Job j = new WorkbenchJob("") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
                    schedule(200);
                    return Status.OK_STATUS;
                }
                addPartListener();
                simulateEditorOpendEventForOpenEditors();
                simulateEditorActivatedEventForActiveEditorIfOne();
                return Status.OK_STATUS;
            }
        };
        j.setSystem(true);
        j.schedule();
    }

    private void addPartListener() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
        final IPartService partService = activeWorkbenchWindow.getPartService();
        partService.addPartListener(partListener);
    }

    // private void removePartListener() {
    // final IWorkbenchWindow ww =
    // PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    // if (ww == null) {
    // /*
    // * when workbench is shutting down this is null - we can safely return
    // then w/o removing the listener
    // * because eclipse deletes our reference.
    // */
    // return;
    // }
    // final IPartService s = ww.getPartService();
    // s.removePartListener(partListener);
    // }
    /**
     * If the workbench starts with an already opened editor our listener does
     * not get the activation event, thus, we need to artificially simulate that
     * event for the active editor (if one) to start the tracking service for
     * this editor.
     */
    private void simulateEditorActivatedEventForActiveEditorIfOne() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        final IWorkbenchPage activePage = activeWindow.getActivePage();
        if (!isActivePageAlreadyAvailableOnStartup(activePage)) {
            return;
        }
        final IWorkbenchPartReference activePart = activePage.getActivePartReference();
        partListener.partActivated(activePart);
    }

    private void simulateEditorOpendEventForOpenEditors() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
        final IWorkbenchPage activePage = activeWindow.getActivePage();
        for (final IEditorReference editorRefs : activePage.getEditorReferences()) {
            partListener.partOpened(editorRefs);
        }
    }

    private boolean isActivePageAlreadyAvailableOnStartup(final IWorkbenchPage activePage) {
        return null != activePage;
    }

    private void fireEditorActivated(final IEditorDashboard board) {
        new BackgroundNotificationJob(jobTitle("Notifying listeners: editor '%s' activated", board)) {
            @Override
            protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                    throws CoreException {
                l.editorActivated(board, monitor);
            }
        }.schedule();
    }

    private void fireEditorDeactivated(final IEditorDashboard board) {
        new BackgroundNotificationJob(jobTitle("Notifying listeners: editor '%s' deactivated", board)) {
            @Override
            protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                    throws CoreException {
                l.editorDeactivated(board, monitor);
            }
        }.schedule();
    }

    private void fireEditorClosed(final IEditorDashboard board) {
        new BackgroundNotificationJob(jobTitle("Notifying listeners: editor '%s' closed", board)) {
            @Override
            protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                    throws CoreException {
                l.editorClosed(board, monitor);
            }
        }.schedule();
    }

    private void fireEditorOpened(final IEditorDashboard board) {
        new BackgroundNotificationJob(jobTitle("Notifying listeners: editor '%s' opened", board)) {
            @Override
            protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                    throws CoreException {
                l.editorOpened(board, monitor);
            }
        }.schedule();
    }

    private void fireEditorHidden(final IEditorDashboard board) {
        new BackgroundNotificationJob(jobTitle("Notifying listeners: editor '%s' hidden", board)) {
            @Override
            protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                    throws CoreException {
                l.editorHidden(board, monitor);
            }
        }.schedule();
    }

    private void fireEditorVisble(final IEditorDashboard board) {
        new BackgroundNotificationJob(jobTitle("Notifying listeners: editor '%s' visible", board)) {
            @Override
            protected void notifyListener(final IEditorChangedListener l, final IProgressMonitor monitor)
                    throws CoreException {
                l.editorVisble(board, monitor);
            }
        }.schedule();
    }

    private String jobTitle(final String format, final IEditorDashboard board) {
        final JavaEditor editor = board.getEditor();
        final String name = editor.getTitle();
        return format(format, name);
    }
}
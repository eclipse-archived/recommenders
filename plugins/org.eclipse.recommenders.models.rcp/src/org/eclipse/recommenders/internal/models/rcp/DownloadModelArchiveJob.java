/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static java.text.MessageFormat.format;
import static org.eclipse.core.runtime.Status.OK_STATUS;
import static org.eclipse.recommenders.internal.models.rcp.LogMessages.ERROR_SAVE_PREFERENCES_FAILED;
import static org.eclipse.recommenders.models.IModelIndex.INDEX;
import static org.eclipse.recommenders.utils.Logs.log;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelArchiveDownloadedEvent;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.common.base.Objects;
import com.google.common.eventbus.EventBus;

public class DownloadModelArchiveJob extends Job {

    private static final int MAXIMUM_NUMBER_OF_DOWNLOADS_PER_JOB = 2;
    private static final int TOTAL_WORK_UNITS = 100000;

    private final IModelRepository repository;
    private final ModelCoordinate mc;
    private final boolean forceDownload;
    private final EventBus bus;

    public DownloadModelArchiveJob(IModelRepository repository, ModelCoordinate mc, boolean forceDownload,
            EventBus bus) {
        super(MessageFormat.format(Messages.JOB_RESOLVING_MODEL, mc));
        this.repository = repository;
        this.mc = mc;
        this.forceDownload = forceDownload;
        this.bus = bus;
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            String message = MessageFormat.format(Messages.TASK_RESOLVING_MODEL, mc);
            MultipleDownloadCallback downloadCallback = new MultipleDownloadCallback(monitor, message, TOTAL_WORK_UNITS,
                    MAXIMUM_NUMBER_OF_DOWNLOADS_PER_JOB);
            File result = repository.resolve(mc, forceDownload, downloadCallback).orNull();
            boolean isDownloadSuccessful = downloadCallback.isDownloadSucceeded();
            if (isDownloadSuccessful) {
                bus.post(new ModelArchiveDownloadedEvent(mc));
            }
            downloadCallback.finish();

            // Returns null if the model coordinate could not be resolved. This may because we are requesting an mc that
            // does not exist in the repository or because of the network being down.
            // Moreover, we can get *cached* null answers, i.e., the same negative result over and over again.
            if (result == null) {
                if (isIndex(mc)) {
                    // Failure to download the index is serious; display an error message.
                    final Display display = PlatformUI.getWorkbench().getDisplay();
                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            IndexDownloadFailureDialog dialog = new IndexDownloadFailureDialog(
                                    display.getActiveShell());
                            if (!dialog.isIgnored()) {
                                dialog.open();
                            }
                        }
                    });
                }

                // Log that as informational but do not open a (extra, in case of index downloads) popup.
                IStatus err = new Status(IStatus.INFO, Constants.BUNDLE_ID,
                        format(Messages.LOG_INFO_NO_MODEL_RESOLVED, mc));
                StatusManager.getManager().handle(err, StatusManager.LOG);
                return Status.CANCEL_STATUS;
            }
        } catch (Exception e) {
            return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                    format(Messages.LOG_ERROR_MODEL_RESOLUTION_FAILURE, mc), e);
        } finally {
            monitor.done();
        }
        return OK_STATUS;
    }

    private boolean isIndex(ModelCoordinate mc) {
        return Objects.equal(INDEX.getGroupId(), mc.getGroupId())
                && Objects.equal(INDEX.getArtifactId(), mc.getArtifactId())
                && Objects.equal(INDEX.getClassifier(), mc.getClassifier())
                && Objects.equal(INDEX.getExtension(), mc.getExtension());
    }

    private static final class IndexDownloadFailureDialog extends MessageDialogWithToggle {

        private IndexDownloadFailureDialog(Shell parentShell) {
            super(parentShell, Messages.DIALOG_TITLE_INDEX_DOWNLOAD_FAILURE, null,
                    Messages.DIALOG_MESSAGE_INDEX_DOWNLOAD_FAILURE, MessageDialog.ERROR,
                    new String[] { IDialogConstants.OK_LABEL }, 0, Messages.DIALOG_TOGGLE_IGNORE_DOWNLOAD_FAILURES,
                    false);
            setPrefStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_ID));
            setPrefKey(Constants.PREF_IGNORE_DOWNLOAD_FAILURES);
        }

        @Override
        protected Control createCustomArea(Composite parent) {
            Link link = new Link(parent, SWT.BEGINNING);
            link.setText(MessageFormat.format(Messages.DIALOG_MESSAGE_FILE_A_BUG,
                    "https://bugs.eclipse.org/bugs/enter_bug.cgi?product=Recommenders")); //$NON-NLS-1$
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    BrowserUtils.openInExternalBrowser(event.text);
                }
            });
            return link;
        }

        @Override
        protected void buttonPressed(int buttonId) {
            super.buttonPressed(buttonId);
            try {
                ((ScopedPreferenceStore) getPrefStore()).save();
            } catch (IOException e) {
                log(ERROR_SAVE_PREFERENCES_FAILED, e);
            }
        }

        public boolean isIgnored() {
            return getPrefStore().getString(getPrefKey()).equals(MessageDialogWithToggle.ALWAYS);
        }
    }
}

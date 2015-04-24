/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.utils.Constants.EXT_JSON;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.egit.core.IteratorService;
import org.eclipse.egit.core.internal.job.JobUtil;
import org.eclipse.egit.core.op.CommitOperation;
import org.eclipse.egit.core.op.PushOperation;
import org.eclipse.egit.core.op.PushOperationResult;
import org.eclipse.egit.core.op.ResetOperation;
import org.eclipse.egit.ui.Activator;
import org.eclipse.egit.ui.UIPreferences;
import org.eclipse.egit.ui.internal.commit.CommitHelper;
import org.eclipse.egit.ui.internal.commit.CommitJob;
import org.eclipse.egit.ui.internal.credentials.EGitCredentialsProvider;
import org.eclipse.egit.ui.internal.dialogs.CommitDialog;
import org.eclipse.egit.ui.internal.push.SimpleConfigurePushDialog;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.snipmatch.rcp.Repositories.SnippetRepositoryConfigurationChangedEvent;
import org.eclipse.recommenders.snipmatch.GitSnippetRepository;
import org.eclipse.recommenders.snipmatch.GitSnippetRepository.GitNoCurrentFormatBranchException;
import org.eclipse.recommenders.snipmatch.GitSnippetRepository.GitNoFormatBranchException;
import org.eclipse.recommenders.snipmatch.GitSnippetRepository.GitUpdateException;
import org.eclipse.recommenders.snipmatch.ISearchContext;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.snipmatch.Snippet;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.SnippetRepositoryClosedEvent;
import org.eclipse.recommenders.snipmatch.rcp.SnippetRepositoryContentChangedEvent;
import org.eclipse.recommenders.snipmatch.rcp.SnippetRepositoryOpenedEvent;
import org.eclipse.recommenders.snipmatch.rcp.model.EclipseGitSnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Urls;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.name.Names;

@SuppressWarnings("restriction")
public class EclipseGitSnippetRepository implements ISnippetRepository {

    private static final String SNIPPETS_DIR = "snippets/"; //$NON-NLS-1$

    private static final int COMMIT_MESSAGE_FIRST_LINE_LENGTH = 65;
    private static final int COMMIT_MESSAGE_LINE_LENGTH = 70;

    private static final Logger LOG = LoggerFactory.getLogger(EclipseGitSnippetRepository.class);

    private final EventBus bus;

    private volatile int timesOpened;
    private GitSnippetRepository delegate;
    private volatile boolean delegateOpen;

    private final Lock readLock;
    private final Lock writeLock;

    private volatile Job openJob = null;

    private final ISchedulingRule schedulingRule = new ISchedulingRule() {

        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule == this;
        }

        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }
    };

    private Set<String> notTracked;
    private Set<String> files;

    public EclipseGitSnippetRepository(String id, File basedir, String remoteUri, String pushUrl,
            String pushBranchPrefix, EventBus bus) {
        this.bus = bus;

        delegate = new GitSnippetRepository(id, new File(basedir, Urls.mangle(remoteUri)), remoteUri, pushUrl,
                pushBranchPrefix);

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    public void open() {
        writeLock.lock();
        try {
            timesOpened++;
            if (timesOpened > 1) {
                return;
            }
            if (openJob == null && !delegateOpen) {
                openJob = new Job(Messages.JOB_OPENING_SNIPPET_REPOSITORY) {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            delegate.open();
                            changeStateToOpen();
                            return Status.OK_STATUS;
                        } catch (GitUpdateException e) {
                            changeStateToOpen();
                            Status status = new Status(IStatus.WARNING, Constants.BUNDLE_ID, MessageFormat.format(
                                    Messages.WARNING_FAILURE_TO_UPDATE_REPOSITORY, delegate.getRepositoryLocation(),
                                    e.getMessage()), e);
                            Platform.getLog(Platform.getBundle(Constants.BUNDLE_ID)).log(status);
                            return Status.OK_STATUS;
                        } catch (final GitNoCurrentFormatBranchException e) {
                            changeStateToOpen();
                            Status status = new Status(IStatus.WARNING, Constants.BUNDLE_ID, MessageFormat.format(
                                    Messages.WARNING_FAILURE_TO_CHECKOUT_CURRENT_BRANCH, Snippet.FORMAT_VERSION,
                                    delegate.getRepositoryLocation(), e.getCheckoutVersion(), e.getMessage()), e);
                            Platform.getLog(Platform.getBundle(Constants.BUNDLE_ID)).log(status);

                            final Display display = Display.getDefault();
                            display.asyncExec(new Runnable() {

                                @Override
                                public void run() {

                                    BranchCheckoutFailureDialog dialog = new BranchCheckoutFailureDialog(display
                                            .getActiveShell(), delegate.getRepositoryLocation(),
                                            Snippet.FORMAT_VERSION, e.getCheckoutVersion());
                                    dialog.open();
                                }
                            });

                            return Status.OK_STATUS;
                        } catch (final GitNoFormatBranchException e) {
                            LOG.error("Exception while opening repository.", e); //$NON-NLS-1$
                            Status status = new Status(IStatus.ERROR, Constants.BUNDLE_ID, MessageFormat.format(
                                    Messages.ERROR_NO_FORMAT_BRANCH, Snippet.FORMAT_VERSION,
                                    delegate.getRepositoryLocation(), e.getMessage()), e);
                            Platform.getLog(Platform.getBundle(Constants.BUNDLE_ID)).log(status);

                            final Display display = Display.getDefault();
                            display.asyncExec(new Runnable() {

                                @Override
                                public void run() {

                                    BranchCheckoutFailureDialog dialog = new BranchCheckoutFailureDialog(display
                                            .getActiveShell(), delegate.getRepositoryLocation(), Snippet.FORMAT_VERSION);
                                    dialog.open();
                                }
                            });

                            return Status.CANCEL_STATUS;
                        } catch (IOException e) {
                            LOG.error("Exception while opening repository.", e); //$NON-NLS-1$
                            Status status = new Status(IStatus.ERROR, Constants.BUNDLE_ID, MessageFormat.format(
                                    Messages.ERROR_FAILURE_TO_CLONE_REPOSITORY, delegate.getRepositoryLocation(),
                                    timesOpened, e.getMessage()), e);
                            Platform.getLog(Platform.getBundle(Constants.BUNDLE_ID)).log(status);
                            return Status.CANCEL_STATUS;
                        } finally {
                            openJob = null;
                        }
                    }

                    private void changeStateToOpen() {
                        delegateOpen = true;
                        bus.post(new SnippetRepositoryOpenedEvent(EclipseGitSnippetRepository.this));
                    }
                };
                openJob.schedule();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
        writeLock.lock();
        try {
            if (timesOpened == 0) {
                return;
            } else if (timesOpened > 1) {
                timesOpened--;
                return;
            } else if (timesOpened == 1) {
                timesOpened = 0;
                if (openJob != null) {
                    try {
                        openJob.join();
                        openJob = null;
                    } catch (InterruptedException e) {
                        LOG.error("Failed to join open job", e); //$NON-NLS-1$
                    }
                }
                delegate.close();
                delegateOpen = false;
                bus.post(new SnippetRepositoryClosedEvent(this));
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<Recommendation<ISnippet>> search(ISearchContext context) {
        readLock.lock();
        try {
            if (!isOpen() || !delegateOpen) {
                return Collections.emptyList();
            }
            return delegate.search(context);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Recommendation<ISnippet>> search(ISearchContext context, int maxResults) {
        readLock.lock();
        try {
            if (!isOpen() || !delegateOpen) {
                return Collections.emptyList();
            }
            return delegate.search(context, maxResults);
        } finally {
            readLock.unlock();
        }
    }

    @Subscribe
    public void onEvent(SnippetRepositoryConfigurationChangedEvent e) throws IOException {
        close();
        open();
    }

    @Override
    public String getRepositoryLocation() {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            if (!delegateOpen) {
                return ""; //$NON-NLS-1$
            }
            return delegate.getRepositoryLocation();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String getId() {
        readLock.lock();
        try {
            return delegate.getId();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean hasSnippet(UUID uuid) {
        readLock.lock();
        try {
            if (!isOpen() || !delegateOpen) {
                return false;
            }
            return delegate.hasSnippet(uuid);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean delete(UUID uuid) throws IOException {
        writeLock.lock();
        try {
            if (!isOpen() || !delegateOpen) {
                return false;
            }
            boolean deleted = delegate.delete(uuid);
            if (deleted) {
                bus.post(new SnippetRepositoryContentChangedEvent(this));
            }
            return deleted;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isDeleteSupported() {
        return delegate.isDeleteSupported();
    }

    private boolean isOpen() {
        return timesOpened > 0;
    }

    @Override
    public void importSnippet(ISnippet snippet) throws IOException {
        writeLock.lock();
        try {
            Preconditions.checkState(isOpen(), Messages.ERROR_REPOSITORY_NOT_OPEN_YET);
            delegate.importSnippet(snippet);
            bus.post(new SnippetRepositoryContentChangedEvent(this));
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isImportSupported() {
        return delegate.isImportSupported();
    }

    public static ISnippetRepository createRepositoryInstance(EclipseGitSnippetRepositoryConfiguration config) {
        EventBus bus = InjectionService.getInstance().requestInstance(EventBus.class);
        File basedir = InjectionService.getInstance().requestAnnotatedInstance(File.class,
                Names.named(SnipmatchRcpModule.SNIPPET_REPOSITORY_BASEDIR));

        return new EclipseGitSnippetRepository(config.getId(), basedir, config.getUrl(), config.getPushUrl(),
                config.getPushBranchPrefix(), bus);
    }

    public static BasicEList<SnippetRepositoryConfiguration> getDefaultConfiguration() {
        BasicEList<SnippetRepositoryConfiguration> result = new BasicEList<SnippetRepositoryConfiguration>();
        result.addAll(DefaultGitSnippetRepositoryConfigurations.fetchDefaultConfigurations());
        return result;
    }

    @Override
    public boolean delete() {
        writeLock.lock();
        try {
            try {
                close();
                delegate.delete();
                return true;
            } catch (IOException e) {
                LOG.error("Exception while deleting files on disk.", e); //$NON-NLS-1$
                return false;
            }

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean share(Collection<UUID> uuids) {
        writeLock.lock();
        try {
            Collection<ISnippet> snippets = Lists.newArrayList();
            for (UUID uuid : uuids) {
                ISnippet snippet = delegate.getSnippet(uuid);
                if (snippet != null) {
                    snippets.add(snippet);
                }
            }
            return shareSnippets(snippets);
        } finally {
            writeLock.unlock();
        }
    }

    private boolean shareSnippets(Collection<ISnippet> snippets) {
        if (snippets.isEmpty()) {
            return false;
        }
        final Workspace workspace = (Workspace) ResourcesPlugin.getWorkspace();
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        List<IResource> resources = toResource(snippets, workspace);

        IResource[] selectedResources = resources.toArray(new IResource[resources.size()]);
        boolean committed = commit(delegate.getGitRepo(), selectedResources, workspace.getRoot(), snippets, shell);
        if (!committed) {
            return false;
        }

        push(delegate.getGitRepo(), shell);

        reset(workspace);

        return true;
    }

    private List<IResource> toResource(Collection<ISnippet> snippets, Workspace workspace) {
        List<IResource> resources = Lists.newArrayList();
        for (ISnippet snippet : snippets) {
            File snippetFile = delegate.getSnippetFile(snippet.getUuid());
            if (snippetFile == null) {
                continue;
            }
            IPath location = new Path(snippetFile.getAbsolutePath());
            IResource file = workspace.newResource(location, IResource.FILE);
            resources.add(file);
        }
        return resources;
    }

    @Override
    public boolean isSharingSupported() {
        return true;
    }

    private boolean commit(Repository repo, IResource[] selectedResources, IResource workspace,
            Collection<ISnippet> snippets, Shell shell) {
        files = Sets.newHashSet();
        IndexDiff indexDiff = null;
        try {
            indexDiff = buildIndexHeadDiffList(delegate.getGitRepo(), new NullProgressMonitor());
        } catch (OperationCanceledException e) {
            return false;
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_CREATING_INDEX_HEAD_DIFF, delegate.getRepositoryLocation());
            return false;
        }
        if (indexDiff == null) {
            Logs.log(LogMessages.ERROR_CREATING_INDEX_HEAD_DIFF, delegate.getRepositoryLocation());
        }
        if (files.isEmpty()) {
            MessageDialog.openInformation(shell, Messages.DIALOG_TITLE_SELECTION_NOT_SHAREABLE,
                    Messages.DIALOG_MESSAGE_NO_GIT_CHANGES_IN_SELECTION);
            return false;
        }

        final Set<String> preselectedFiles = Sets.newHashSet();

        getPreselectedFiles(selectedResources, preselectedFiles);
        if (preselectedFiles.isEmpty()) {
            MessageDialog.openInformation(shell, Messages.DIALOG_TITLE_SELECTION_NOT_SHAREABLE,
                    Messages.DIALOG_MESSAGE_NO_GIT_CHANGES_IN_SELECTION);
            return false;
        }

        Collection<ISnippet> changedSnippets = getChangedSnippets(snippets, preselectedFiles);
        String commitMessage = getCommitMessage(changedSnippets);

        CommitDialog commitDialog = getCommitDialog(shell, indexDiff, preselectedFiles, commitMessage);
        return doCommit(commitDialog);
    }

    private IndexDiff buildIndexHeadDiffList(Repository repo, IProgressMonitor monitor) throws IOException,
            OperationCanceledException {
        monitor.beginTask(Messages.MONITOR_CALCULATING_DIFF, 1000);
        try {
            WorkingTreeIterator it = IteratorService.createInitialIterator(repo);
            if (it == null) {
                throw new OperationCanceledException(); // workspace is closed
            }
            IndexDiff indexDiff = new IndexDiff(repo, org.eclipse.jgit.lib.Constants.HEAD, it);
            indexDiff.diff();

            Set<String> indexChanges = Sets.newHashSet();
            Set<String> notIndexed = Sets.newHashSet();
            notTracked = Sets.newHashSet();

            includeList(indexDiff.getAdded(), indexChanges);
            includeList(indexDiff.getChanged(), indexChanges);
            includeList(indexDiff.getRemoved(), indexChanges);
            includeList(indexDiff.getMissing(), notIndexed);
            includeList(indexDiff.getModified(), notIndexed);
            includeList(indexDiff.getUntracked(), notTracked);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            return indexDiff;
        } finally {
            monitor.done();
        }
    }

    private void includeList(Set<String> added, Set<String> category) {
        for (String filename : added) {
            if (!files.contains(filename)) {
                files.add(filename);
            }
            category.add(filename);
        }
    }

    private void getPreselectedFiles(IResource[] selectedResources, final Set<String> preselectedFiles) {
        for (String fileName : files) {
            URI uri = new File(delegate.getGitRepo().getWorkTree(), fileName).toURI();
            for (IResource resource : selectedResources) {
                if (resource.getFullPath().toFile().equals(new File(uri))) {
                    preselectedFiles.add(fileName);
                }
            }
        }
    }

    private Collection<ISnippet> getChangedSnippets(Collection<ISnippet> snippets, final Set<String> preselectedFiles) {
        return Collections2.filter(snippets, new Predicate<ISnippet>() {

            @Override
            public boolean apply(ISnippet input) {
                UUID uuid = input.getUuid();
                for (String preselectedFile : preselectedFiles) {
                    if (preselectedFile.substring(SNIPPETS_DIR.length(),
                            preselectedFile.length() - EXT_JSON.length() - 1).equals(uuid.toString())) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private String getCommitMessage(Collection<ISnippet> snippets) {
        StringBuilder sb = new StringBuilder();
        String header = ""; //$NON-NLS-1$
        if (snippets.size() == 1) {
            ISnippet snippet = snippets.iterator().next();
            header = format("Snippet contribution: {0} - {1}", snippet.getName(), snippet.getDescription()); //$NON-NLS-1$
        } else {
            header = format("This contributes {0} snippets", snippets.size()); //$NON-NLS-1$
        }
        sb.append(abbreviate(header, COMMIT_MESSAGE_FIRST_LINE_LENGTH));
        sb.append(LINE_SEPARATOR);
        sb.append(LINE_SEPARATOR);
        for (ISnippet snippet : snippets) {
            String snippetDescription = snippet.getDescription();
            String line = ""; //$NON-NLS-1$
            if (StringUtils.isEmptyOrNull(snippetDescription)) {
                line = " * " + snippet.getName();
            } else {
                line = " * " + snippet.getName() + " - " + snippet.getDescription();
            }
            sb.append(abbreviate(line, COMMIT_MESSAGE_LINE_LENGTH));
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private CommitDialog getCommitDialog(Shell shell, IndexDiff indexDiff, final Set<String> preselectedFiles,
            String commitMessage) {
        CommitHelper commitHelper = new CommitHelper(delegate.getGitRepo());

        CommitDialog commitDialog = new CommitDialog(shell);
        commitDialog.setAmendAllowed(false);
        commitDialog.setFiles(delegate.getGitRepo(), files, indexDiff);
        commitDialog.setPreselectedFiles(preselectedFiles);
        commitDialog.setAuthor(commitHelper.getAuthor());
        commitDialog.setCommitter(commitHelper.getCommitter());
        commitDialog.setCommitMessage(commitMessage);
        return commitDialog;
    }

    private boolean doCommit(CommitDialog commitDialog) {
        /*
         * TODO This is a workaround for CommitDialog shortcomings.
         *
         * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=447236
         */
        IPreferenceStore preferenceStore = org.eclipse.egit.ui.Activator.getDefault().getPreferenceStore();
        boolean includeUntrackedPreference = preferenceStore.getBoolean(UIPreferences.COMMIT_DIALOG_INCLUDE_UNTRACKED);
        try {
            preferenceStore.setValue(UIPreferences.COMMIT_DIALOG_INCLUDE_UNTRACKED, true);

            if (commitDialog.open() != IDialogConstants.OK_ID) {
                return false;
            }

        } finally {
            preferenceStore.setValue(UIPreferences.COMMIT_DIALOG_INCLUDE_UNTRACKED, includeUntrackedPreference);
        }

        final CommitOperation commitOperation;
        try {
            commitOperation = new CommitOperation(delegate.getGitRepo(), commitDialog.getSelectedFiles(), notTracked,
                    commitDialog.getAuthor(), commitDialog.getCommitter(), commitDialog.getCommitMessage());
        } catch (CoreException e) {
            Activator.handleError(Messages.ERROR_COMMIT_FAILED, e, true);
            return false;
        }
        commitOperation.setComputeChangeId(commitDialog.getCreateChangeId());
        commitOperation.setCommitAll(false);
        Job commitJob = new CommitJob(delegate.getGitRepo(), commitOperation);
        commitJob.setRule(schedulingRule);
        commitJob.schedule();
        return true;
    }

    private void push(Repository repository, final Shell shell) {
        RemoteConfig config = SimpleConfigurePushDialog.getConfiguredRemote(repository);
        int timeout = Activator.getDefault().getPreferenceStore().getInt(UIPreferences.REMOTE_CONNECTION_TIMEOUT);
        final PushOperation push = new PushOperation(repository, config.getName(), false, timeout);
        push.setCredentialsProvider(new EGitCredentialsProvider());
        Job pushJob = new Job(Messages.JOB_PUSHING_SNIPPETS_TO_REMOTE_GIT_REPO) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    push.run(monitor);
                    PushOperationResult operationResult = push.getOperationResult();
                    for (URIish uri : operationResult.getURIs()) {
                        String errorMessage = operationResult.getErrorMessage(uri);
                        if (errorMessage == null) {
                            return Status.OK_STATUS;
                        } else {
                            return new Status(IStatus.ERROR, Constants.BUNDLE_ID, MessageFormat.format(
                                    Messages.ERROR_FAILURE_TO_PUSH_SNIPPETS_TO_REMOTE_GIT_REPO, errorMessage));
                        }
                    }
                    return Status.OK_STATUS;
                } catch (InvocationTargetException e) {
                    return new Status(IStatus.ERROR, Constants.BUNDLE_ID,
                            Messages.ERROR_EXCEPTION_WHILE_PUSHING_SNIPPETS_TO_REMOTE_GIT_REPO, e);
                }
            }
        };
        pushJob.setRule(schedulingRule);
        pushJob.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                shell.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (event.getResult().isOK()) {
                            MessageDialog.openInformation(shell, Messages.DIALOG_TITLE_GIT_PUSH_SUCCESSFUL,
                                    Messages.DIALOG_MESSAGE_GIT_PUSH_SUCCESSFUL);
                        }
                    }
                });
            }
        });
        pushJob.schedule();
    }

    private void reset(final Workspace workspace) {
        ResetOperation reset = new ResetOperation(delegate.getGitRepo(), "origin/" + Snippet.FORMAT_VERSION, //$NON-NLS-1$
                ResetType.MIXED) {
            @Override
            public ISchedulingRule getSchedulingRule() {
                return schedulingRule;
            }
        };
        JobUtil.scheduleUserJob(reset, Messages.JOB_RESETTING_GIT_REPOSITORY, null);
    }
}

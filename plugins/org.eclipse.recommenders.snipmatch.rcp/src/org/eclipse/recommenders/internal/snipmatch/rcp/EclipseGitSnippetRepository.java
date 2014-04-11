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
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.SnipmatchRcpModule.SNIPMATCH_BASEDIR;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.snipmatch.GitSnippetRepository;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.snipmatch.ISnippetRepository;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Urls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.inject.name.Named;

public class EclipseGitSnippetRepository implements ISnippetRepository, IRcpService {

    private static Logger LOG = LoggerFactory.getLogger(EclipseGitSnippetRepository.class);

    private volatile int timesOpened;
    private ISnippetRepository delegate;
    private volatile boolean delegateOpen;

    private final Lock readLock;
    private final Lock writeLock;

    private volatile Job openJob = null;

    private File basedir;

    @Inject
    public EclipseGitSnippetRepository(@Named(SNIPMATCH_BASEDIR) File basedir, SnipmatchRcpPreferences prefs) {
        String remoteUri = prefs.getLocation();
        this.basedir = new File(basedir, Urls.mangle(remoteUri));
        this.delegate = new GitSnippetRepository(this.basedir, remoteUri);

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    @Override
    @PostConstruct
    public void open() throws IOException {
        writeLock.lock();
        try {
            timesOpened++;
            if (timesOpened > 1) {
                return;
            }
            if (openJob == null && !delegateOpen) {
                openJob = new Job("Opening snippets repository") {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            delegate.open();
                            delegateOpen = true;
                            openJob = null;
                            return Status.OK_STATUS;
                        } catch (IOException e) {
                            LOG.error("Exception while opening repository.", e);
                            return Status.CANCEL_STATUS;
                        }
                    }
                };
                openJob.schedule();
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    @PreDestroy
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
                    } catch (InterruptedException e) {
                        LOG.error("Failed to join open job", e);
                    }
                    delegate.close();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<Recommendation<ISnippet>> search(String query) {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            if (!delegateOpen) {
                return Collections.emptyList();
            }
            return delegate.search(query);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ImmutableSet<Recommendation<ISnippet>> getSnippets() {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            if (!delegateOpen) {
                return ImmutableSet.of();
            }
            return delegate.getSnippets();
        } finally {
            readLock.unlock();
        }
    }

    @Subscribe
    public void onEvent(SnippetRepositoryChangedEvent e) throws IOException {
        close();
        open();
    }

    public static class SnippetRepositoryChangedEvent {
    }

    @Override
    public String getRepositoryLocation() {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            if (!delegateOpen) {
                return "";
            }
            return delegate.getRepositoryLocation();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean hasSnippet(UUID uuid) {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            if (!delegateOpen) {
                return false;
            }
            return delegate.hasSnippet(uuid);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean delete(UUID uuid) throws IOException {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            if (!delegateOpen) {
                return false;
            }
            return delegate.delete(uuid);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isDeleteSupported() {
        readLock.lock();
        try {
            Preconditions.checkState(isOpen());
            return delegate.isDeleteSupported();
        } finally {
            readLock.unlock();
        }
    }

    private boolean isOpen() {
        return timesOpened > 0;
    }

    @Override
    public void importSnippet(ISnippet snippet) throws IOException {
        writeLock.lock();
        try {
            Preconditions.checkState(isOpen());
            delegate.importSnippet(snippet);
        } finally {
            writeLock.unlock();
        }
    }
}

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
package org.eclipse.recommenders.snipmatch;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class GitSnippetRepository extends FileSnippetRepository {

    private static Logger LOG = LoggerFactory.getLogger(GitSnippetRepository.class);

    private File basedir;
    private String repoUrl;
    private File gitFile;

    public GitSnippetRepository(File basedir, String repoUrl) {
        super(basedir);
        this.basedir = basedir;
        this.repoUrl = repoUrl;
        gitFile = new File(basedir.getAbsolutePath() + "/.git");
    }

    @Override
    public void open() throws IOException {
        synchronized (this) {
            boolean gitFileExists = gitFile.exists();
            try {
                if (gitFileExists) {
                    updateSnippetsRepo();
                } else {
                    cloneSnippetsRepo();
                }
            } catch (InvalidRemoteException e) {
                LOG.error("Invalid remote repository.", e);
                throw createException(gitFileExists, MessageFormat.format(
                        "Invalid remote repository \u0027{1}\u0027. Check the repository's URL.", repoUrl), e);
            } catch (TransportException e) {
                LOG.error("Transport operation failed.", e);
                throw createException(gitFileExists,
                        "Could not connect to remote repository. Your internet connection may be down.", e);
            } catch (GitAPIException e) {
                LOG.error("Exception while update/clone repository.", e);
                throw createException(gitFileExists, "Exception while updating/cloning repository.", e);
            } catch (CoreException e) {
                LOG.error("Exception while opening repository.", e);
                throw createException(gitFileExists, "Exception while opening repository.", e);
            } finally {
                if (gitFileExists) {
                    super.open();
                }
            }
        }
        super.open();
    }

    @SuppressWarnings("serial")
    public class GitUpdateException extends IOException {
        public GitUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private IOException createException(boolean gitFileExists, String message, Throwable e) {
        if (gitFileExists) {
            return new GitUpdateException(message, e);
        } else {
            return new IOException(message, e);
        }
    }

    private void cloneSnippetsRepo() throws GitAPIException, InvalidRemoteException, TransportException, IOException {
        CloneCommand clone = Git.cloneRepository();
        clone.setBare(false);
        clone.setBranch("master");
        clone.setCloneAllBranches(false);
        clone.setDirectory(basedir).setURI(getRepositoryLocation());
        Git git = clone.call();
        setPushBranch(git);

    }

    private void updateSnippetsRepo() throws IOException, InvalidRemoteException, TransportException, GitAPIException,
            CoreException {
        FileRepository localRepo = new FileRepository(gitFile);
        FileBasedConfig config = localRepo.getConfig();
        Set<String> subsections = config.getSubsections("remote");
        for (String subsection : subsections) {
            String remoteUrl = config.getString("remote", subsection, "url");
            if (!remoteUrl.equals(getRepositoryLocation())) {
                cloneSnippetsRepo();
                return;
            }
        }
        Git git = new Git(localRepo);
        PullCommand pull = git.pull();
        pull.call();
        setPushBranch(git);
    }

    private void setPushBranch(Git git) throws IOException {
        StoredConfig config = git.getRepository().getConfig();
        String push = config.getString("remote", "origin", "push");
        if (Strings.isNullOrEmpty(push)) {
            config.setString("remote", "origin", "push", "HEAD:refs/for/master");
            config.save();
        }
    }

    @Override
    public String getRepositoryLocation() {
        return repoUrl;
    }
}

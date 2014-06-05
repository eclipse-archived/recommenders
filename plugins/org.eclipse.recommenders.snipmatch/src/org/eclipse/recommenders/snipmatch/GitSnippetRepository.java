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

import static org.eclipse.recommenders.snipmatch.Snippet.FORMAT_VERSION;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class GitSnippetRepository extends FileSnippetRepository {

    private static Logger LOG = LoggerFactory.getLogger(GitSnippetRepository.class);

    private final File basedir;
    private final String repoUrl;
    private final File gitFile;

    public GitSnippetRepository(File basedir, String repoUrl) {
        super(basedir);
        this.basedir = basedir;
        this.repoUrl = repoUrl;
        gitFile = new File(basedir.getAbsolutePath() + "/.git");
    }

    @Override
    public void open() throws IOException {
        synchronized (this) {
            boolean updatePossible = false;
            boolean gitFileExists = gitFile.exists();
            if (gitFileExists) {
                updatePossible = isUpdatePossible();
            }
            try {
                if (!updatePossible) {
                    if (gitFileExists) {
                        FileUtils.deleteDirectory(gitFile);
                    }
                    initializeSnippetsRepo();
                }
                pullSnippets();
            } catch (InvalidRemoteException e) {
                LOG.error("Invalid remote repository.", e);
                throw createException(updatePossible, MessageFormat.format(
                        "Invalid remote repository \u0027{1}\u0027. Check the repository's URL.", repoUrl), e);
            } catch (TransportException e) {
                LOG.error("Transport operation failed.", e);
                throw createException(updatePossible,
                        "Could not connect to remote repository. Your internet connection may be down.", e);
            } catch (GitAPIException e) {
                LOG.error("Exception while update/clone repository.", e);
                throw createException(updatePossible, "Exception while updating/cloning repository.", e);
            } catch (CoreException e) {
                LOG.error("Exception while opening repository.", e);
                throw createException(updatePossible, "Exception while opening repository.", e);
            } finally {
                super.open();
            }
        }
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

    private boolean isUpdatePossible() throws IOException {
        if (RepositoryCache.FileKey.isGitRepository(gitFile, FS.DETECTED)) {
            FileRepository localRepo = new FileRepository(gitFile);
            for (Ref ref : localRepo.getAllRefs().values()) {
                if (ref.getObjectId() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initializeSnippetsRepo() throws GitAPIException, InvalidRemoteException, TransportException,
    IOException {
        InitCommand init = Git.init();
        init.setBare(false);
        init.setDirectory(basedir);
        Git git = init.call();
        configureGit(git);
    }

    private void configureGit(Git git) throws IOException {
        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", "origin", "url", getRepositoryLocation());
        config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
        String pushBranch = "HEAD:refs/for/" + FORMAT_VERSION;
        config.setString("remote", "origin", "push", pushBranch);

        // prevents trust anchor errors when pulling from eclipse.org
        config.setBoolean("http", null, "sslVerify", false);

        config.setString("branch", FORMAT_VERSION, "remote", "origin");
        String branch = "refs/heads/" + FORMAT_VERSION;
        config.setString("branch", FORMAT_VERSION, "merge", branch);
        config.save();
    }

    private void pullSnippets() throws IOException, InvalidRemoteException, TransportException, GitAPIException,
    CoreException {
        String remoteBranch = "origin/" + FORMAT_VERSION;
        FileRepository localRepo = new FileRepository(gitFile);
        Git git = new Git(localRepo);

        git.fetch().call();
        CheckoutCommand checkout = git.checkout();
        checkout.setName(FORMAT_VERSION);
        checkout.setStartPoint(remoteBranch);
        checkout.setCreateBranch(!branchExistsLocally(git, "refs/heads/" + FORMAT_VERSION));
        checkout.call();

        PullCommand pull = git.pull();
        pull.call();
    }

    private boolean branchExistsLocally(Git git, String remoteBranch) throws GitAPIException {
        List<Ref> branches = git.branchList().call();
        Collection<String> branchNames = Collections2.transform(branches, new Function<Ref, String>() {

            @Override
            public String apply(Ref input) {
                return input.getName();
            }

        });
        return branchNames.contains(remoteBranch);
    }

    @Override
    public String getRepositoryLocation() {
        return repoUrl;
    }
}

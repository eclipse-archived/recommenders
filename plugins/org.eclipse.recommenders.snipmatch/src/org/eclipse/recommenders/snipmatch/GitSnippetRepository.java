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

import static com.google.common.base.Strings.isNullOrEmpty;
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
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class GitSnippetRepository extends FileSnippetRepository {

    private static final String FORMAT = "format-";

    private static Logger LOG = LoggerFactory.getLogger(GitSnippetRepository.class);

    private final File basedir;
    private final String fetchUrl;
    private final File gitFile;
    private final String pushUrl;
    private final String pushBranchPrefix;

    private Repository localRepo;

    public GitSnippetRepository(int id, File basedir, String fetchUrl, String pushUrl, String pushBranchPrefix) {
        super(id, basedir);
        this.basedir = basedir;
        this.fetchUrl = fetchUrl;
        this.pushUrl = pushUrl;
        this.pushBranchPrefix = pushBranchPrefix;
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
                configureGit();
                Git git = fetch();
                String checkoutBranch = getCheckoutBranch(git);
                if (isNullOrEmpty(checkoutBranch)) {
                    throw new GitNoFormatBranchException(MessageFormat.format("Could not locate branch \"{0}\"",
                            FORMAT_VERSION), null);
                }
                configureGitBranch(checkoutBranch);
                pullSnippets(git, checkoutBranch);
                if (!checkoutBranch.equals(FORMAT_VERSION)) {
                    throw new GitNoCurrentFormatBranchException(checkoutBranch, MessageFormat.format(
                            "Could not locate branch \"{0}\", working with older branch \"{1}\".", FORMAT_VERSION,
                            checkoutBranch), null);
                }
            } catch (InvalidRemoteException e) {
                LOG.error("Invalid remote repository.", e);
                throw createException(updatePossible, MessageFormat.format(
                        "Invalid remote repository \"{0}\". Check the repository's URL.", fetchUrl), e);
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
    public static class GitUpdateException extends IOException {

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

    @SuppressWarnings("serial")
    public static final class GitNoCurrentFormatBranchException extends IOException {

        private final String checkoutVersion;

        public GitNoCurrentFormatBranchException(String checkoutVersion, String message, Throwable cause) {
            super(message, cause);
            this.checkoutVersion = checkoutVersion;
        }

        public String getCheckoutVersion() {
            return checkoutVersion;
        }
    }

    @SuppressWarnings("serial")
    public static final class GitNoFormatBranchException extends IOException {

        public GitNoFormatBranchException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private boolean isUpdatePossible() throws IOException {
        if (RepositoryCache.FileKey.isGitRepository(gitFile, FS.DETECTED)) {
            Repository localRepo = new FileRepositoryBuilder().setGitDir(gitFile).build();
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
        init.call();
    }

    private void configureGit() throws IOException {
        Git git = Git.open(gitFile);
        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", "origin", "url", getRepositoryLocation());
        config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
        config.setString("remote", "origin", "pushUrl", pushUrl);
        // prevents trust anchor errors when pulling from eclipse.org
        config.setBoolean("http", null, "sslVerify", false);
        config.save();
    }

    private Git fetch() throws GitAPIException, IOException {
        localRepo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        Git git = new Git(localRepo);
        git.fetch().call();
        return git;
    }

    private String getCheckoutBranch(Git git) throws IOException, GitAPIException {
        ListBranchCommand branchList = git.branchList();
        branchList.setListMode(ListMode.REMOTE);
        List<Ref> branches = branchList.call();

        String formatVersion = FORMAT_VERSION.substring(FORMAT.length());
        int version = Integer.parseInt(formatVersion);

        return getCheckoutBranch(branches, version);
    }

    private String getCheckoutBranch(List<Ref> branches, int version) {
        String remoteBranch = "refs/remotes/origin/format-" + version;
        for (Ref branch : branches) {
            if (branch.getName().equals(remoteBranch)) {
                return FORMAT + version;
            }
        }
        if (version > 2) { // 2 == lowest, publicly available version
            return getCheckoutBranch(branches, version - 1);
        }
        return "";
    }

    private void configureGitBranch(String remoteBranch) throws IOException {
        Git git = Git.open(gitFile);
        StoredConfig config = git.getRepository().getConfig();
        String pushBranch = "HEAD:" + pushBranchPrefix + "/" + remoteBranch;
        config.setString("remote", "origin", "push", pushBranch);

        config.setString("branch", remoteBranch, "remote", "origin");
        String branch = "refs/heads/" + remoteBranch;
        config.setString("branch", remoteBranch, "merge", branch);
        config.save();
    }

    private void pullSnippets(Git git, String checkoutBranch) throws IOException, InvalidRemoteException,
    TransportException, GitAPIException, CoreException {
        CheckoutCommand checkout = git.checkout();
        checkout.setName(checkoutBranch);
        checkout.setStartPoint("origin/" + checkoutBranch);
        checkout.setCreateBranch(!branchExistsLocally(git, "refs/heads/" + checkoutBranch));
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
        return fetchUrl;
    }

    @Override
    public void close() {
        localRepo.close();
        super.close();
    };

    @Override
    public boolean delete() {
        close();
        try {
            FileUtils.deleteDirectory(basedir);
            return true;
        } catch (IOException e) {
            LOG.error("Exception while deleting files on disk.", e);
            return false;
        }
    }

    public Repository getGitRepo() {
        return localRepo;
    }

    public File getBasedir() {
        return basedir;
    }
}

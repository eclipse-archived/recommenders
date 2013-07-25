/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.rcp.repo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepository;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex;
import org.eclipse.recommenders.internal.rcp.repo.UpdateModelIndexJob;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.base.Optional;

public class RepositoryConsistencyManualTests {

    private static ModelRepository repo;
    private static ModelRepositoryIndex index;
    public static Artifact SWT_30_CALL = new DefaultArtifact("org.eclipse.swt", "org.eclipse.swt.cocoa.macosx.x86_64",
            "call", "zip", "3.0.0");

    @BeforeClass
    public static void beforeClass() throws Exception {
        repo = (ModelRepository) InjectionService.getInstance().getInjector().getInstance(IModelRepository.class);
        index = (ModelRepositoryIndex) InjectionService.getInstance().getInjector()
                .getInstance(IModelRepositoryIndex.class);

        UpdateModelIndexJob job = new UpdateModelIndexJob(index, repo);
        job.schedule();
        job.join();

    }

    @Test
    public void testModelExistOnServer() throws Exception {

        List<Artifact> call = index.searchByClassifier("call");
        assertFalse(call.isEmpty());
        for (Artifact model : call) {
            Optional<String> remoteEtag = repo.remoteEtag(model);
            assertTrue("no etag for " + model + " in " + repo.getRemoteUrl(), remoteEtag.isPresent());
        }
        System.out.println();
    }

    @Test
    public void testSwtPlatformModelsFound() throws Exception {
        String[] bundleIds = { "org.eclipse.swt.cocoa.macosx.x86_64", "org.eclipse.swt.cocoa.macosx.x86",
                "org.eclipse.swt.win32.win32.x86_64", "org.eclipse.swt.win32.win32.x86",
                "org.eclipse.swt.gtk.linux.x86_64", "org.eclipse.swt.gtk.linux.x86", };

        String[] classifierIds = { "call", "ovrd", "ovrp", "selfc", "selfm" };

        for (String cid : classifierIds) {
            for (String bid : bundleIds) {
                Optional<Artifact> mac = index.searchByArtifactId(bid, cid);
                String msg = String.format("couldn't find model '%s' for '%s'", cid, bid);
                assertTrue(msg, mac.isPresent());
            }
        }
    }

    @Test
    public void testFindHigestVersion() {
        Optional<Artifact> opt = repo.findHigestVersion(SWT_30_CALL);
        assertTrue(opt.isPresent());
    }

    @Test
    public void repoPhases() throws Exception {
        // check not available on startup
        File location = repo.location(SWT_30_CALL);
        assertFalse(location.exists());

        // no model - it can't be the latest
        assertFalse(repo.isLatest(SWT_30_CALL));

        // check resolving works
        File file = repo.resolve(SWT_30_CALL, new NullProgressMonitor());
        assertTrue(file.exists());

        // is this one the latest version we have?
        // we just downloaded it, right?
        assertTrue(repo.isLatest(SWT_30_CALL));

        // prepare the stage for install
        File move = File.createTempFile(file.getName(), ".zip");
        location.renameTo(move);
        assertFalse(location.exists());

        // how do we deal with non-existent model file? we ignore that fact:
        assertTrue(repo.isLatest(SWT_30_CALL));

        // check install works
        repo.install(SWT_30_CALL.setFile(move));
        assertTrue(location.exists());

        // check delete works
        repo.delete(SWT_30_CALL.setFile(move));
        assertFalse(location.exists());

        // is 'null' the latest:
        assertFalse(repo.isLatest(SWT_30_CALL));

    }

    @Test
    public void repoSmoketest() {
        repo.toString();
        repo.findHigestVersion(SWT_30_CALL);
        repo.findLowestVersion(SWT_30_CALL);
    }
}

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

import java.util.List;

import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepository;
import org.eclipse.recommenders.internal.rcp.repo.ModelRepositoryIndex;
import org.eclipse.recommenders.internal.rcp.repo.UpdateModelIndexJob;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.rcp.repo.IModelRepositoryIndex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;

public class RepositoryConsistencyTest {

    private static ModelRepository repo;
    private static ModelRepositoryIndex index;

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
            assertTrue("no etag for " + model + " in " + repo.getRemote(), remoteEtag.isPresent());
        }
        System.out.println();
    }
}

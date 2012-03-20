/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.overrides.model;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.recommenders.internal.completion.rcp.overrides.net.ClassOverridesNetwork;
import org.eclipse.recommenders.internal.rcp.models.IModelArchive;
import org.eclipse.recommenders.internal.rcp.models.archive.NullModelArchive;
import org.eclipse.recommenders.internal.rcp.models.archive.PoolingModelArchive;
import org.eclipse.recommenders.internal.utils.codestructs.MethodDeclaration;
import org.eclipse.recommenders.internal.utils.codestructs.TypeDeclaration;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class InstantOverridesRecommender {
    private IModelArchive<ITypeName, ClassOverridesNetwork> archive = NullModelArchive.empty();

    private final class OverridesModelResolverJob extends Job {
        private final IModelRepository repository;

        private OverridesModelResolverJob(String name, IModelRepository repository) {
            super(name);
            this.repository = repository;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                File model = repository.resolve(
                        new DefaultArtifact("org.eclipse.recommenders:overrides:zip:ovr:0.0.1"),
                        new NullProgressMonitor());
                archive = new PoolingModelArchive<ITypeName, ClassOverridesNetwork>(new OverridesZipModelFactory(model));

            } catch (Exception e) {
                System.out.println(e);
            }
            return Status.OK_STATUS;
        }
    }

    private final double MIN_PROBABILITY_THRESHOLD = 0.1d;

    private ClassOverridesNetwork model;

    private TypeDeclaration type;

    private ITypeName superclass;

    @Inject
    public InstantOverridesRecommender(IModelRepository repo) {
        new OverridesModelResolverJob("", repo).schedule();
    }

    public InstantOverridesRecommender(IModelArchive<ITypeName, ClassOverridesNetwork> models) {
        this.archive = models;
    }

    public synchronized List<OverridesRecommendation> createRecommendations(final TypeDeclaration type) {
        this.type = type;
        this.superclass = type.superclass;
        if (!hasModel(superclass)) {
            return Collections.emptyList();
        }
        aquireModel();
        computeRecommendations();
        List<OverridesRecommendation> res = readRecommendations();
        releaseModel();
        return res;
    }

    private void releaseModel() {
        archive.releaseModel(model);
    }

    public boolean hasModel(final ITypeName name) {
        return archive.hasModel(name);
    }

    private void aquireModel() {
        model = archive.acquireModel(superclass).orNull();
    }

    private void computeRecommendations() {
        for (final MethodDeclaration method : type.methods) {
            model.observeMethodNode(method.superDeclaration);
        }
    };

    private List<OverridesRecommendation> readRecommendations() {
        final List<OverridesRecommendation> res = Lists.newLinkedList();
        for (final Tuple<IMethodName, Double> item : model.getRecommendedMethodOverrides(MIN_PROBABILITY_THRESHOLD, 5)) {
            final IMethodName method = item.getFirst();
            final Double probability = item.getSecond();
            final OverridesRecommendation recommendation = new OverridesRecommendation(method, probability);
            res.add(recommendation);
        }
        return res;
    }
}
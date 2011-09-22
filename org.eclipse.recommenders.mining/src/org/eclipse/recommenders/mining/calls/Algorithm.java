/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.calls;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.mining.calls.generation.ModelArchiveFileWriter;
import org.eclipse.recommenders.mining.calls.generation.ModelGenerator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class Algorithm {

    private final IModelSpecificationProvider specProvider;
    private final IObjectUsageProvider usageProvider;
    private final Set<IModelGenerationListener> generationListeners;
    private final File outdir;

    @Inject
    public Algorithm(final IModelSpecificationProvider specProvider, final IObjectUsageProvider usageProvider,
            final Set<IModelGenerationListener> generationListener, final AlgorithmParameters config) {
        this.specProvider = specProvider;
        this.usageProvider = usageProvider;
        this.generationListeners = generationListener;
        this.outdir = config.getOut();
    }

    public void run() {

        for (final ModelSpecification spec : specProvider.findSpecifications()) {
            try {
                for (final IModelGenerationListener l : generationListeners) {
                    l.started(spec);
                }
                doGenerate(spec);
                for (final IModelGenerationListener l : generationListeners) {
                    l.finished(spec);
                }
            } catch (final Exception e) {
                for (final IModelGenerationListener l : generationListeners) {
                    l.failed(spec, e);
                }
            }
        }
    }

    private void doGenerate(final ModelSpecification spec) throws IOException {
        final Multimap<ITypeName, ObjectUsage> index = buildIndex(spec);
        if (index.isEmpty()) {
            for (final IModelGenerationListener l : generationListeners) {
                l.skip(spec, "no (new) data.");
            }
        } else {
            for (final IModelGenerationListener l : generationListeners) {
                l.generate(spec);
            }
            generateModelZipAndUpdateSpec(spec, index);
        }

    }

    private Multimap<ITypeName, ObjectUsage> buildIndex(final ModelSpecification spec) {
        final Iterable<ObjectUsage> usages = usageProvider.findObjectUsages(spec);
        final HashMultimap<ITypeName, ObjectUsage> res = HashMultimap.create();
        for (final ObjectUsage objectUsage : usages) {
            res.put(objectUsage.type, objectUsage);
        }
        return res;
    }

    private void generateModelZipAndUpdateSpec(final ModelSpecification spec,
            final Multimap<ITypeName, ObjectUsage> index) throws IOException {

        final Date timestamp = new Date();
        final Manifest manifest = new Manifest(spec.getSymbolicName(), spec.getVersionRange(), timestamp);
        final ModelGenerator modelGenerator = new ModelGenerator(index);
        final File dest = new File(outdir, manifest.getIdentifier() + ".zip");
        final ModelArchiveFileWriter writer = new ModelArchiveFileWriter(dest);
        writer.consume(manifest);
        modelGenerator.generateModel(writer);
        writer.close();
        spec.setLatestBuilt(timestamp);
    }
}

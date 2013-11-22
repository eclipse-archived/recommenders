/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models.advisors;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.Pair;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class SharedManualMappingsAdvisor extends AbstractProjectCoordinateAdvisor {

    public static final ModelCoordinate MAPPINGS = new ModelCoordinate("org.eclipse.recommenders", "mappings", null,
            "properties", "1.0.0");

    private final IModelRepository repository;

    private List<Pair<String, ProjectCoordinate>> mappings;

    public SharedManualMappingsAdvisor(IModelRepository repository) {
        this.repository = repository;
    }

    @Override
    protected boolean isApplicable(DependencyType any) {
        return true;
    }

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        initializeMappings();

        String path = dependencyInfo.getFile().getAbsolutePath().replace(File.separatorChar, '/');
        for (Pair<String, ProjectCoordinate> mapping : mappings) {
            String suffixPattern = mapping.getFirst();
            if (PathUtils.matchesSuffixPattern(path, suffixPattern)) {
                return Optional.of(mapping.getSecond());
            }
        }

        return Optional.absent();
    }

    private synchronized void initializeMappings() {
        if (mappings == null) {
            // Look for new shared mappings after each restart.
            Optional<File> mappingFile = repository.resolve(MAPPINGS, true);
            if (mappingFile.isPresent()) {
                mappings = readMappingFile(mappingFile.get());
            } else {
                mappings = Collections.emptyList();
            }
        }
    }

    private List<Pair<String, ProjectCoordinate>> readMappingFile(File mappingFile) {
        try {
            List<Pair<String, ProjectCoordinate>> result = Lists.newLinkedList();
            List<String> lines = Files.readLines(mappingFile, Charsets.UTF_8);
            for (String line : lines) {
                String[] split = StringUtils.split(line, "=");
                if (split.length != 2) {
                    throw new IllegalArgumentException();
                }
                result.add(Pair.newPair(split[0], ProjectCoordinate.valueOf(split[1])));
            }
            return result;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

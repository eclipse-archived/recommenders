/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch- initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.File;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.rcp.repo.IModelRepository;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.swt.graphics.Image;
import org.sonatype.aether.artifact.Artifact;

import com.google.common.base.Optional;

public class ModelLabelProvider extends ColumnLabelProvider {
    public static final String NLS_UNKNOWN = "No model for this dependency";
    public static final String NLS_KNOWN = "Model for this dependency is available";
    private final Image modelImage;
    private final Image modelUnknownImage;
    private final IModelRepository repository;

    public ModelLabelProvider(IModelRepository repository, Image modelImage, Image modelUnknownImage) {
        this.repository = repository;
        this.modelImage = modelImage;
        this.modelUnknownImage = modelUnknownImage;
    }

    @Override
    public Image getImage(final Object element) {
        return hasModel(element) ? modelImage : modelUnknownImage;
    }

    @Override
    public String getToolTipText(final Object element) {
        return hasModel(element) ? NLS_KNOWN : NLS_UNKNOWN;
    }

    private boolean hasModel(final Object element) {
        Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> e = cast(element);
        Optional<Artifact> opt = e.getSecond().getArtifact();
        if (!opt.isPresent()) {
            return false;
        }
        File location = repository.location(opt.get());
        return location.exists();
    }
}
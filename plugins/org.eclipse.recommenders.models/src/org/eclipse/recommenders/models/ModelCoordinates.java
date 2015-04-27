/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.models;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;

public final class ModelCoordinates {

    public static ModelCoordinate toModelCoordinate(ProjectCoordinate pc, String classifier, String extension) {
        return new ModelCoordinate(pc.getGroupId(), pc.getArtifactId(), classifier, extension, pc.getVersion());
    }

    public static ProjectCoordinate toProjectCoordinate(ModelCoordinate mc) {
        return new ProjectCoordinate(mc.getGroupId(), mc.getArtifactId(), mc.getVersion());
    }

    private ModelCoordinates() {
    }
}

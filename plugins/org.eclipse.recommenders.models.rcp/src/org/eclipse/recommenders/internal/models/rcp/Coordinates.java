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
package org.eclipse.recommenders.internal.models.rcp;

import org.eclipse.recommenders.models.ModelCoordinate;
import org.eclipse.recommenders.models.ProjectCoordinate;

public class Coordinates {

    public static ModelCoordinate toModelCoordinate(ProjectCoordinate pc, String classifier, String extension) {
        return new ModelCoordinate(pc.getGroupId(), pc.getArtifactId(), classifier, extension, pc.getVersion());
    }

    public static ProjectCoordinate toProjectCoordinate(ModelCoordinate mc) {
        return new ProjectCoordinate(mc.getGroupId(), mc.getArtifactId(), mc.getVersion());
    }

    private Coordinates() {
    }
}

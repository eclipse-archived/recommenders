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
package org.eclipse.recommenders.internal.rcp.repo;

import java.util.Set;

import org.sonatype.aether.version.VersionRange;

public class ModelArtifact {

    /**
     * The finger-prints of all jar's this model is supporting
     */
    private Set<String> matchFingerprints;
    private Set<String> matchArtifactIds;

    /**
     * the model's coordinate in the (remote or local) repository
     */
    private String coordinate;

    private Long generatedOn;

    private VersionRange range;

}

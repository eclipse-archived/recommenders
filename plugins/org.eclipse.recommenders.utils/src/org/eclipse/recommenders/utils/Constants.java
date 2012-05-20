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
package org.eclipse.recommenders.utils;

public interface Constants {
    String COORD_INDEX = "org.eclipse.recommenders:index:zip:0.0.0";
    String F_FINGERPRINTS = "fingerprints";
    String F_COORDINATE = "coordinate";
    String F_CLASSIFIER = "classifier";
    String F_ARTIFACT_ID = "artifactId";
    String SYMBOLIC_NAMES = "symbolic-names";

    /**
     * group id org.eclipse.recommenders
     */
    String GID_CR = "org.eclipse.recommenders";
    String AID_OVRS = "overrides";
    String AID_INDEX = "index";

    String VERSION = "0.0.1";

    String EXT_POM = "pom";
    String EXT_ZIP = "zip";
    String EXT_JAR = "jar";

    String CLASS_CALL_MODELS = "call";
    String CLASS_OVERRIDES_DIRECTIVES = "ovrd";
    String CLASS_OVERRIDES_PATTERNS = "ovrp";
    String CLASS_OVR_DATA = "ovr";
    String CLASS_OUS_DATA = "ous";
}

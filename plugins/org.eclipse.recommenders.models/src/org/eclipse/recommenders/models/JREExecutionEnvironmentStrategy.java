/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models;

import static com.google.common.base.Optional.*;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class JREExecutionEnvironmentStrategy extends AbstractStrategy {

    private static Map<String, ProjectCoordinate> lookUpTable = createLookUpTable();

    @Override
    protected Optional<ProjectCoordinate> extractProjectCoordinateInternal(DependencyInfo dependencyInfo) {
        Optional<String> optionalExecutionEnvironment = dependencyInfo
                .getAttribute(DependencyInfo.EXECUTION_ENVIRONMENT);
        if (optionalExecutionEnvironment.isPresent()) {
            ProjectCoordinate projectCoordinate = lookUpTable.get(optionalExecutionEnvironment.get());
            if (projectCoordinate != null) {
                return fromNullable(projectCoordinate);
            }
        }
        return absent();
    }

    private static Map<String, ProjectCoordinate> createLookUpTable() {
        Map<String, ProjectCoordinate> result = Maps.newHashMap();

        result.put("JRE-1.1", new ProjectCoordinate("jre", "jre", "1.1.0"));
        result.put("J2SE-1.2", new ProjectCoordinate("jre", "jre", "1.2.0"));
        result.put("J2SE-1.3", new ProjectCoordinate("jre", "jre", "1.3.0"));
        result.put("J2SE-1.4", new ProjectCoordinate("jre", "jre", "1.4.0"));
        result.put("J2SE-1.5", new ProjectCoordinate("jre", "jre", "1.5.0"));
        result.put("JavaSE-1.6", new ProjectCoordinate("jre", "jre", "1.6.0"));
        result.put("JavaSE-1.7", new ProjectCoordinate("jre", "jre", "1.7.0"));

        return result;
    }

    @Override
    public boolean isApplicable(DependencyType dependencyType) {
        return dependencyType == DependencyType.JRE;
    }

}

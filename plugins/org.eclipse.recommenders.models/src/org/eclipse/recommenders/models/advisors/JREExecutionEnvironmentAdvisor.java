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
package org.eclipse.recommenders.models.advisors;

import static com.google.common.base.Optional.*;

import java.util.Map;

import org.eclipse.recommenders.models.DependencyInfo;
import org.eclipse.recommenders.models.DependencyType;
import org.eclipse.recommenders.models.ProjectCoordinate;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class JREExecutionEnvironmentAdvisor extends AbstractProjectCoordinateAdvisor {

    private static Map<String, ProjectCoordinate> lookUpTable = createLookUpTable();

    @Override
    protected Optional<ProjectCoordinate> doSuggest(DependencyInfo dependencyInfo) {
        String optionalExecutionEnvironment = dependencyInfo.getHint(DependencyInfo.EXECUTION_ENVIRONMENT).orNull();
        if (optionalExecutionEnvironment != null) {
            ProjectCoordinate pc = lookUpTable.get(optionalExecutionEnvironment);
            return fromNullable(pc);
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

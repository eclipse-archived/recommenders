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
package org.eclipse.recommenders.rdk.utils

class Poms {

    def static newPom(String groupId, String artifactId, String version) {
        '''
            <?xml version="1.0" encoding="UTF-8"?>
            <project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            	<modelVersion>4.0.0</modelVersion>
            	<groupId>«groupId»</groupId>
            	<artifactId>«artifactId»</artifactId>
            	<version>«version»</version>
            </project>
        '''
    }
}

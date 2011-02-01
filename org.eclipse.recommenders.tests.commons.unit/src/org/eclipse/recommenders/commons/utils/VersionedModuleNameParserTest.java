/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.utils;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.recommenders.commons.utils.VersionedModuleName;
import org.eclipse.recommenders.commons.utils.VersionedModuleNameParser;
import org.junit.Test;

public class VersionedModuleNameParserTest {

    @Test
    public void testParseFromFileName() {
        // setup:
        String symbolicName = "org.eclipse.test";
        String version = "0.2.3";
        File file = new File(format("%s_%s.jar", symbolicName, version));
        // exercise:
        VersionedModuleName res = VersionedModuleNameParser.parseFromFile(file);
        // verify:
        assertEquals(symbolicName, res.name);
        assertEquals(version, res.version.toString());
    }
}

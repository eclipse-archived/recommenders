/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import org.eclipse.recommenders.server.extdoc.CodeExamplesServer;
import org.eclipse.recommenders.server.extdoc.types.CodeExamples;
import org.eclipse.recommenders.tests.commons.extdoc.ServerUtils;
import org.eclipse.recommenders.tests.commons.extdoc.TestUtils;
import org.junit.Test;

public class CodeExamplesServerTest {

    private final CodeExamplesServer server = new CodeExamplesServer(ServerUtils.getServer(),
            ServerUtils.getUsernameListener());

    @Test
    public void testGetOverridenMethodCodeExamples() {
        final CodeExamples examples = server.getOverridenMethodCodeExamples(TestUtils.getDefaultMethod());
        // examples.getExamples();
    }

    @Test
    public void testGetTypeCodeExamples() {
        final CodeExamples examples = server.getTypeCodeExamples(TestUtils.getDefaultType());
        // examples.getExamples();
    }
}

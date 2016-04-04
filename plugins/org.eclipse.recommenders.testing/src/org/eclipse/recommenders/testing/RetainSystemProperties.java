/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.testing;

import java.util.HashMap;
import java.util.Properties;

import org.junit.rules.ExternalResource;

public class RetainSystemProperties extends ExternalResource {

    private HashMap<Object, Object> backup;

    @Override
    protected void before() {
        // Back up system properties (but not their default values).
        backup = new HashMap<>(System.getProperties());
    }

    @Override
    protected void after() {
        // Re-insert backed up systme properties (but leave default values as is).
        Properties properties = System.getProperties();
        properties.clear();
        properties.putAll(backup);
    }
}

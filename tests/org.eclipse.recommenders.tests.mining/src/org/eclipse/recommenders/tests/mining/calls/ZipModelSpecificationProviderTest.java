/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.mining.calls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.zip.ZipModelSpecificationProvider;
import org.junit.Test;

import com.google.common.collect.Iterables;

public class ZipModelSpecificationProviderTest {

    @Test
    public void testWithDefaults() {
        final AlgorithmParameters config = new AlgorithmParameters();
        final ZipModelSpecificationProvider sut = new ZipModelSpecificationProvider(config);
        final Iterable<ModelSpecification> specs = sut.findSpecifications();
        final ModelSpecification spec = Iterables.getFirst(specs, null);
        assertNotNull(spec);
        assertEquals(config.getSymbolicName(), spec.getSymbolicName());
        assertEquals(config.getVersionRange(), spec.getVersionRange());
        assertFalse(spec.getLastBuilt().hasValue());
    }
}

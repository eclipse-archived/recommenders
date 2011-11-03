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
package org.eclipse.recommenders.mining.calls.data.zip;

import java.util.Collections;
import java.util.HashSet;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.IModelSpecificationProvider;

import com.google.inject.Inject;

public class ZipModelSpecificationProvider implements IModelSpecificationProvider {

    private final ModelSpecification spec;

    @Inject
    public ZipModelSpecificationProvider(final AlgorithmParameters config) {
        spec = new ModelSpecification(config.getSymbolicName(), new String[0], config.getVersionRange(), null,
                new HashSet<String>());

    }

    @Override
    public Iterable<ModelSpecification> findSpecifications() {
        return Collections.singleton(spec);
    }
}

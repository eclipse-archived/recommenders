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
package org.eclipse.recommenders.mining.calls.data.couch;

import java.util.Collection;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.mining.calls.AlgorithmParameters;
import org.eclipse.recommenders.mining.calls.data.IModelSpecificationProvider;

import com.google.common.collect.Collections2;
import com.google.inject.Inject;

public class CouchModelSpecificationProvider implements IModelSpecificationProvider {

    private final CouchDbDataAccess db;
    private final boolean forceModelGeneration;

    @Inject
    public CouchModelSpecificationProvider(final CouchDbDataAccess db, final AlgorithmParameters conf) {
        this.db = db;
        forceModelGeneration = conf.isForce();
    }

    @Override
    public Iterable<ModelSpecification> findSpecifications() {

        final Collection<ModelSpecification> specs = db.lookupAllModelSpecifications();
        return Collections2.filter(specs, new NewObjectUsagesAvailablePredicate(db, forceModelGeneration));
    }

}

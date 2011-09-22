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
package org.eclipse.recommenders.mining.calls.couch;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.eclipse.recommenders.mining.calls.IModelGenerationListener;

import com.google.inject.Inject;

public class CouchModelSpecUpdater implements IModelGenerationListener {

    private final CouchDbDataAccess db;

    @Inject
    public CouchModelSpecUpdater(final CouchDbDataAccess db) {
        this.db = db;
    }

    @Override
    public void started(final ModelSpecification spec) {
    }

    @Override
    public void finished(final ModelSpecification spec) {
        db.save(spec);
    }

    @Override
    public void failed(final ModelSpecification spec, final Exception e) {
    }

    @Override
    public void skip(final ModelSpecification spec, final String reason) {

    }

    @Override
    public void generate(final ModelSpecification spec) {
    }

}

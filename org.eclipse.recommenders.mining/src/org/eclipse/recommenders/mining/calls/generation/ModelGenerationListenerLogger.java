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
package org.eclipse.recommenders.mining.calls.generation;

import static java.lang.String.format;

import org.eclipse.recommenders.commons.udc.ModelSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelGenerationListenerLogger implements IModelGenerationListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void started(final ModelSpecification spec) {
        log.debug("Checking for new update for '{}'", spec.getIdentifier());
    }

    @Override
    public void finished(final ModelSpecification spec) {
        log.debug("Checked call model generation for '{}'", spec.getIdentifier());
    }

    @Override
    public void failed(final ModelSpecification spec, final Exception e) {
        final String msg = format("Faild to generate call model generation for '%s'", spec.getIdentifier());
        log.error(msg, e);
    }

    @Override
    public void skip(final ModelSpecification spec, final String reason) {
        log.debug("Skipped model generation for '{}': {}", spec.getIdentifier(), reason);
    }

    @Override
    public void generate(final ModelSpecification spec) {
        log.info("Generating new calls model for '{}'.", spec.getIdentifier());
    }
}

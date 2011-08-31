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
package org.eclipse.recommenders.mining.calls;

import org.eclipse.recommenders.commons.udc.ModelSpecification;

public class ModelGenerationListenerLogger implements IModelGenerationListener {

    @Override
    public void started(final ModelSpecification spec) {
        System.out.println("begin:" + spec.getIdentifier());

    }

    @Override
    public void finished(final ModelSpecification spec) {
        System.out.println("finished:" + spec.getIdentifier());
    }

    @Override
    public void failed(final ModelSpecification spec, final Exception e) {
        System.err.println("failed:" + spec.getIdentifier());
        e.printStackTrace();
    }

    @Override
    public void skip(final ModelSpecification spec, final String reason) {
        System.out.println("Skipped " + spec.getIdentifier() + ": reason: " + reason);
    }

}

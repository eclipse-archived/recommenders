/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.store2;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.BayesNetWrapper;
import org.eclipse.recommenders.internal.completion.rcp.calls.net.IObjectMethodCallsNet;
import org.eclipse.recommenders.internal.completion.rcp.calls.store2.models.IModelLoader;
import org.eclipse.recommenders.utils.names.ITypeName;

public class CallsModelLoader implements IModelLoader<IObjectMethodCallsNet> {

    @Override
    public IObjectMethodCallsNet load(final ITypeName name, final InputStream inputStream) {
        try {
            final ObjectInputStream objectStream = new ObjectInputStream(inputStream);
            final BayesianNetwork network = (BayesianNetwork) objectStream.readObject();
            return new BayesNetWrapper(name, network);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}

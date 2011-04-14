/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayes;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import java.io.IOException;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.CallsModelStore;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

public class BayesianNetworkCallsModelStore extends CallsModelStore {

    @Override
    protected IObjectMethodCallsNet loadNetwork(final ITypeName name) {
        try {
            final BayesianNetwork network = loader.loadObjectForTypeName(name, BayesianNetwork.class);
            return new SmileNetWrapper(name, network);
        } catch (final IOException x) {
            throw throwUnhandledException(x);
        }
    }
}

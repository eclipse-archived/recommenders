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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.db;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.bayes.SmileNetWrapper;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.net.IObjectMethodCallsNet;

public class BinaryModelLoader {

    public IObjectMethodCallsNet load(final ITypeName name, final InputStream inputStream) {
        try {
            final ObjectInputStream objectStream = new ObjectInputStream(inputStream);
            final BayesianNetwork network = (BayesianNetwork) objectStream.readObject();
            return new SmileNetWrapper(name, network);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

    }
}

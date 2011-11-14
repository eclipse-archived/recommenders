/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API.
 *    Johannes Lerch - implementation.
 */
package org.eclipse.recommenders.mining.calls.data;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.udc.Manifest;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IModelArchiveWriter extends Closeable {

    void consume(Manifest manifest) throws IOException;

    void consume(ITypeName typeName, BayesianNetwork bayesNet) throws IOException;
}
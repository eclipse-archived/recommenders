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
package org.eclipse.recommenders.internal.analysis.analyzers;

import com.ibm.wala.classLoader.IClass;

public interface IDependencyFingerprintComputer {

    public String NULL = "null";

    /**
     * Computes the fingerprint of the enclosing container. This is typically a
     * jar file.
     */
    String computeContainerFingerprint(IClass clazz);

}

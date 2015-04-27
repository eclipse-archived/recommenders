/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.models;

import org.eclipse.recommenders.coordinates.ProjectCoordinate;
import org.eclipse.recommenders.utils.names.IMethodName;

public class UniqueMethodName extends AbstractUniqueName<IMethodName> {

    public UniqueMethodName(ProjectCoordinate pc, IMethodName name) {
        super(pc, name);
    }
}

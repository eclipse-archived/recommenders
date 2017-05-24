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
package org.eclipse.recommenders.overrides;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.recommenders.utils.names.IMethodName;

public class OverrideObservation {

    public int frequency;

    public Set<IMethodName> overriddenMethods = new TreeSet<>();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

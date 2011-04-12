/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class InstanceUsage {
    public static InstanceUsage create(final String name) {
        final InstanceUsage res = new InstanceUsage();
        res.name = name;
        return res;
    }

    public String name;

    public String description;

    public Set<IMethodName> invokedMethods = Sets.newHashSet();

    public Map<IMethodName, Integer> observedContexts = Maps.newHashMap();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}

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
package org.eclipse.recommenders.mining.calls.generation;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

import com.google.common.collect.Sets;

public class ReceiverCallGroupsContainer {
    public Set<IMethodName> invokedMethods = Sets.newHashSet();

    public Bag<IMethodName> observedContexts = HashBag.newHashBag();

    public Bag<String> definitionSites = HashBag.newHashBag();

    public static ReceiverCallGroupsContainer create() {
        final ReceiverCallGroupsContainer res = new ReceiverCallGroupsContainer();
        return res;
    }

    public static ReceiverCallGroupsContainer newGroup(final Set<IMethodName> callGroup,
            final Tuple<IMethodName, Integer>[] callingContextsWithFrequency,
            final Tuple<String, Integer>[] definitionContextsWithFrequency) {
        final ReceiverCallGroupsContainer res = new ReceiverCallGroupsContainer();
        res.invokedMethods = callGroup;

        for (final Tuple<IMethodName, Integer> c : callingContextsWithFrequency) {
            res.observedContexts.add(c.getFirst(), c.getSecond());
        }

        for (final Tuple<String, Integer> d : definitionContextsWithFrequency) {
            res.definitionSites.add(d.getFirst(), d.getSecond());
        }
        return res;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    public int numberOfUsagesInContext(final IMethodName curCallingContext) {
        return observedContexts.count(curCallingContext);
    }

    public int numberOfUsagesInContextAndDefinitionSite(final IMethodName curCallingContext, final String definitionSite) {
        return observedContexts.count(curCallingContext);
    }
}

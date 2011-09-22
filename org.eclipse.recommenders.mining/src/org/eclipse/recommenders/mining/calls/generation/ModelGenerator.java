/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API.
 *    Marcel Burch - initial API.
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.calls.generation;import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.commons.udc.ObjectUsage;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
;

public class ModelGenerator {

    private Multimap<ITypeName, ObjectUsage> types2usages = HashMultimap.create();
    private IModelArchiveWriter writer;

    public ModelGenerator(final Multimap<ITypeName, ObjectUsage> types2usages) {
        Checks.ensureIsNotNull(types2usages);
        Checks.ensureIsNotEmpty(types2usages.keys(), "There must be at least one type available");
        this.types2usages = types2usages;
    }

    public synchronized void generateModel(final IModelArchiveWriter writer) throws IOException {
        this.writer = writer;
        buildReceiverCallGroupsPerType();
    }

    private void buildReceiverCallGroupsPerType() throws IOException {
        for (final ITypeName type : types2usages.keySet()) {
            final Collection<ObjectUsage> usages = types2usages.get(type);
            Checks.ensureIsNotEmpty(usages, "There must be at least one object usage for type " + type.toString());
            final Map<Set<IMethodName>, ReceiverCallGroupsContainer> calls2GroupsContainer = Maps.newHashMap();

            for (final ObjectUsage usage : usages) {
                collectReceiverCallGroups(calls2GroupsContainer, usage);
            }
            final BayesianNetwork net = TypeModelsWithContextBuilder
                    .createNetwork(type, calls2GroupsContainer.values());
            writer.consume(type, net);
        }
    }

    private void collectReceiverCallGroups(
            final Map<Set<IMethodName>, ReceiverCallGroupsContainer> calls2GroupsContainer, final ObjectUsage usage) {
        final IMethodName firstMethodDeclarationContext = getFirstMethodDeclarationContext(usage);
        final Set<IMethodName> key = rebaseMethodCalls(usage);
        final ReceiverCallGroupsContainer group = findOrCreateReceiverCallsGroupContainer(calls2GroupsContainer, key);
        group.observedContexts.add(firstMethodDeclarationContext);
    }

    private IMethodName getFirstMethodDeclarationContext(final ObjectUsage usage) {
        if (usage.contextFirst == null) {
            return NetworkUtils.CTX_NULL;
        }
        return usage.contextFirst;
    }

    private Set<IMethodName> rebaseMethodCalls(final ObjectUsage obj) {
        final Set<IMethodName> rebasedMethodCalls = Sets.newTreeSet();
        for (final IMethodName method : obj.calls) {
            final VmMethodName rebased = VmMethodName.rebase(obj.type, method);
            rebasedMethodCalls.add(rebased);
        }
        return rebasedMethodCalls;
    }

    private ReceiverCallGroupsContainer findOrCreateReceiverCallsGroupContainer(
            final Map<Set<IMethodName>, ReceiverCallGroupsContainer> calls2GroupsContainer, final Set<IMethodName> key) {
        ReceiverCallGroupsContainer group = calls2GroupsContainer.get(key);
        if (group == null) {
            group = new ReceiverCallGroupsContainer();
            group.invokedMethods = key;
            calls2GroupsContainer.put(key, group);
        }
        return group;
    }

}

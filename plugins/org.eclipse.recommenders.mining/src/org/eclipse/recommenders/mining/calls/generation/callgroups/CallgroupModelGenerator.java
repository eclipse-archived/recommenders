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
package org.eclipse.recommenders.mining.calls.generation.callgroups;

import static org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage.UNKNOWN_METHOD;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.commons.bayesnet.BayesianNetwork;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectUsage;
import org.eclipse.recommenders.mining.calls.data.IModelArchiveWriter;
import org.eclipse.recommenders.mining.calls.generation.IModelGenerator;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

// XXX delete this class
public class CallgroupModelGenerator implements IModelGenerator {

    private Multimap<ITypeName, ObjectUsage> types2usages = HashMultimap.create();
    private IModelArchiveWriter writer;

    @Override
    public BayesianNetwork generate(ITypeName type, Collection<ObjectUsage> usages) {

        throw new RuntimeException("obsolete");

        // Checks.ensureIsNotNull(types2usages);
        // Checks.ensureIsNotEmpty(types2usages.keys(), "There must be at least one type available");
        // this.types2usages = types2usages;
        //
        // this.writer = writer;
        // buildReceiverCallGroupsPerType();
    }

    private void buildReceiverCallGroupsPerType() throws IOException {
        for (final ITypeName type : types2usages.keySet()) {

            System.out.println("learning for " + type + " (callgroups)");

            final Collection<ObjectUsage> usages = types2usages.get(type);
            Checks.ensureIsNotEmpty(usages, "There must be at least one object usage for type " + type.toString());
            final Map<Set<IMethodName>, ReceiverCallGroupsContainer> calls2GroupsContainer = Maps.newHashMap();

            for (final ObjectUsage usage : usages) {
                collectReceiverCallGroups(calls2GroupsContainer, usage);
            }

            System.out.println(String.format("... found %d patterns", calls2GroupsContainer.size()));

            final BayesianNetwork net = TypeModelsWithContextBuilder
                    .createNetwork(type, calls2GroupsContainer.values());

            System.out.println("... network built");

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
            return UNKNOWN_METHOD;
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
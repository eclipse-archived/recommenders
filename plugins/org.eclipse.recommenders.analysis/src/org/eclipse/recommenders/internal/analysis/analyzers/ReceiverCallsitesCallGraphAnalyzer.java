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
package org.eclipse.recommenders.internal.analysis.analyzers;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.analysis.utils.HeapHelper;
import org.eclipse.recommenders.internal.analysis.utils.ReceiverCallsitesCallGraphVisitor;
import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.eclipse.recommenders.internal.analysis.utils.WalaReceiverCallsite;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.collect.Multimap;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class ReceiverCallsitesCallGraphAnalyzer implements ICallGraphAnalyzer {
    private HeapHelper heapHelper;

    private CallGraph callGraph;

    private Multimap<InstanceKey, WalaReceiverCallsite> instances;

    private Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping;

    @Override
    public void setMethodDeclaration(final MethodDeclaration method) {
        // only uses local values as input
    }

    @Override
    public void analyzeCallgraph(final CallGraph callGraph, final HeapHelper heapHelper,
            final Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping) {
        this.callGraph = callGraph;
        this.heapHelper = heapHelper;
        this.instanceKeyMapping = instanceKeyMapping;
        collectInstanceUsages();
        collectInstanceSequences();
        populateMethodDeclaration();
    }

    private void collectInstanceUsages() {
        final ReceiverCallsitesCallGraphVisitor visitor = new ReceiverCallsitesCallGraphVisitor(callGraph, heapHelper);
        visitor.run();
        instances = visitor.getInstances();
    }

    private void collectInstanceSequences() {
        // final InterproceduralCallGraphAnalyzer visitor = new
        // InterproceduralCallGraphAnalyzer(callGraph, heapHelper);
        // visitor.run();
        // sequences = visitor.getInstances();
    }

    private void populateMethodDeclaration() {
        for (final InstanceKey instance : instances.keySet()) {
            final ObjectInstanceKey value = instanceKeyMapping.get(instance);
            ensureIsNotNull(value);
            for (final WalaReceiverCallsite callsite : instances.get(instance)) {
                value.receiverCallSites.add(mapWala2recCallsite(callsite));
            }
        }
    }

    private ReceiverCallSite mapWala2recCallsite(final WalaReceiverCallsite callsite) {
        final IMethodName source = WalaNameUtils.wala2recMethodName(callsite.source);
        final IMethodName target = WalaNameUtils.wala2recMethodName(callsite.target);
        final ReceiverCallSite res = ReceiverCallSite.create(callsite.receiver, target, source, callsite.line);
        return res;
    }
}

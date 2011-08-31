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
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.utils.HeapHelper;
import org.eclipse.recommenders.internal.commons.analysis.utils.LocalNamesCollector;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ssa.IR;

public class LocalNamesCollectingCallGraphAnalyzer implements ICallGraphAnalyzer {
    private HeapHelper heapHelper;

    private CallGraph callGraph;

    private Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping;

    @Override
    public void setMethodDeclaration(final MethodDeclaration method) {
    }

    @Override
    public void analyzeCallgraph(final CallGraph callGraph, final HeapHelper heapHelper,
            final Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping) {
        this.callGraph = callGraph;
        this.heapHelper = heapHelper;
        this.instanceKeyMapping = instanceKeyMapping;
        collectPointers();
    }

    private void collectPointers() {
        for (final CGNode entryNode : callGraph.getEntrypointNodes()) {
            final IR ir = entryNode.getIR();
            if (ir == null) {
                continue;
            }
            final LocalNamesCollector t = new LocalNamesCollector(ir);
            for (final int valueNumber : t.getValues()) {
                final String valueName = t.getName(valueNumber);
                if (valueName != null) {
                    final Set<InstanceKey> keys = heapHelper.getInstanceKeys(entryNode, valueNumber);
                    for (final InstanceKey key : keys) {
                        final ObjectInstanceKey objKey = instanceKeyMapping.get(key);
                        objKey.names.add(valueName);
                    }
                }
            }
        }
    }
}

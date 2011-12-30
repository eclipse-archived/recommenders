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

import java.util.Map;

import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.ParameterCallSite;
import org.eclipse.recommenders.internal.analysis.utils.HeapHelper;
import org.eclipse.recommenders.internal.analysis.utils.ParameterCallsitesCallGraphVisitor;
import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.eclipse.recommenders.internal.analysis.utils.WalaParameterCallsite;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.collect.Multimap;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

@SuppressWarnings("unused")
public class ParameterCallsitesCallGraphAnalyzer implements ICallGraphAnalyzer {
    private MethodDeclaration methodDecl;

    private CallGraph callGraph;

    private HeapHelper heapHelper;

    private Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping;

    private Multimap<InstanceKey, WalaParameterCallsite> instances;

    @Override
    public void setMethodDeclaration(final MethodDeclaration methodDecl) {
        this.methodDecl = methodDecl;
    }

    @Override
    public void analyzeCallgraph(final CallGraph callGraph, final HeapHelper heapHelper,
            final Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping) {
        this.callGraph = callGraph;
        this.heapHelper = heapHelper;
        this.instanceKeyMapping = instanceKeyMapping;
        findParameterCallSites();
        addParameterCallSitesToLocals();
    }

    private void findParameterCallSites() {
        final ParameterCallsitesCallGraphVisitor visitor = new ParameterCallsitesCallGraphVisitor(callGraph, heapHelper);
        visitor.run();
        instances = visitor.getInstances();
    }

    private void addParameterCallSitesToLocals() {
        for (final InstanceKey key : instances.keySet()) {
            final ObjectInstanceKey objectInstanceKey = instanceKeyMapping.get(key);
            for (final WalaParameterCallsite cs : instances.get(key)) {
                objectInstanceKey.parameterCallSites.add(mapWala2recCallsite(cs));
            }
        }
    }

    private ParameterCallSite mapWala2recCallsite(final WalaParameterCallsite callsite) {
        final IMethodName source = WalaNameUtils.wala2recMethodName(callsite.source);
        final IMethodName target = WalaNameUtils.wala2recMethodName(callsite.target);
        final ParameterCallSite res = ParameterCallSite.create(callsite.argumentName, target, callsite.argumentIndex,
                source, callsite.line);
        return res;
    }
}

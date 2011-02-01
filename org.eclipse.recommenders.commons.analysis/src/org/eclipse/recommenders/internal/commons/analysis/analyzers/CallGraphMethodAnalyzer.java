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

import static java.util.Collections.singleton;

import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey.Kind;
import org.eclipse.recommenders.internal.commons.analysis.newsites.NewSiteReferenceForField;
import org.eclipse.recommenders.internal.commons.analysis.newsites.NewSiteReferenceForMethodReturn;
import org.eclipse.recommenders.internal.commons.analysis.utils.HeapHelper;
import org.eclipse.recommenders.internal.commons.analysis.utils.InstanceCallGraphBuilder;
import org.eclipse.recommenders.internal.commons.analysis.utils.MethodUtils;
import org.eclipse.recommenders.internal.commons.analysis.utils.WalaNameUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

public class CallGraphMethodAnalyzer implements IMethodAnalyzer {
    private final Provider<InstanceCallGraphBuilder> cgBuilderProvider;

    private IMethod entrypointMethod;

    private CallGraph callGraph;

    private HeapHelper heapHelper;

    private MethodDeclaration methodDecl;

    private final Set<? extends ICallGraphAnalyzer> callgraphAnalyzers;

    private Map<InstanceKey, ObjectInstanceKey> wala2recommendersValues;

    private InstanceCallGraphBuilder cgBuilder;

    @Inject
    public CallGraphMethodAnalyzer(final Provider<InstanceCallGraphBuilder> cgBuilder,
            final Set<ICallGraphAnalyzer> callgraphAnalyzers) {
        this.cgBuilderProvider = cgBuilder;
        this.callgraphAnalyzers = callgraphAnalyzers;
    }

    @Override
    public void analyzeMethod(final Entrypoint entrypoint, final MethodDeclaration method) {
        this.methodDecl = method;
        setEntrypointMethod(entrypoint);
        buildCallGraph(entrypoint);
        createHeapHelper();
        createInstanceKeyMappings();
        invokeCallgraphAnalyzers();
    }

    private void createInstanceKeyMappings() {
        wala2recommendersValues = Maps.newHashMap();
        for (final InstanceKey walaInstanceKey : cgBuilder.getPointerAnalysis().getInstanceKeys()) {
            final ITypeName varType = WalaNameUtils.wala2recTypeName(walaInstanceKey.getConcreteType());
            final ObjectInstanceKey recInstanceKey = ObjectInstanceKey.create(varType, Kind.LOCAL);
            wala2recommendersValues.put(walaInstanceKey, recInstanceKey);
            methodDecl.objects.add(recInstanceKey);
            if (walaInstanceKey instanceof NormalAllocationInNode) {
                final NormalAllocationInNode alloc = (NormalAllocationInNode) walaInstanceKey;
                // alloc.getNode().getDU().getDef();
                final NewSiteReference site = alloc.getSite();
                if (site instanceof NewSiteReferenceForField) {
                    final NewSiteReferenceForField newsiteForField = (NewSiteReferenceForField) site;
                    final String fieldName = newsiteForField.field.getName().toString();
                    recInstanceKey.kind = Kind.FIELD;
                    recInstanceKey.names.add(fieldName);
                    recInstanceKey.definitionSite = DefinitionSite.create(WalaNameUtils
                            .wala2recFieldName(newsiteForField.field));
                } else if (site instanceof NewSiteReferenceForMethodReturn) {
                    final NewSiteReferenceForMethodReturn newsiteForField = (NewSiteReferenceForMethodReturn) site;
                    recInstanceKey.kind = Kind.LOCAL;
                    recInstanceKey.definitionSite = DefinitionSite
                            .create(org.eclipse.recommenders.internal.commons.analysis.codeelements.DefinitionSite.Kind.METHOD_RETURN,
                                    null, 0, WalaNameUtils.wala2recMethodName(newsiteForField.def));
                } else if (MethodUtils.isFakeRoot(alloc.getNode())) {
                    // we assume that it is parameter when it is initialized in fakeroot
                    // bogus but okay for now.
                    recInstanceKey.kind = Kind.PARAMETER;
                }
            }
        }
    }

    private void invokeCallgraphAnalyzers() {
        for (final ICallGraphAnalyzer analyzer : callgraphAnalyzers) {
            analyzer.setMethodDeclaration(methodDecl);
            analyzer.analyzeCallgraph(callGraph, heapHelper, wala2recommendersValues);
        }
    }

    private void setEntrypointMethod(final Entrypoint entrypoint) {
        entrypointMethod = entrypoint.getMethod();
    }

    private void buildCallGraph(final Entrypoint entrypoint) {
        cgBuilder = cgBuilderProvider.get();
        cgBuilder.setEntryPoints(singleton(entrypoint));
        cgBuilder.setThisType(entrypointMethod.getDeclaringClass());
        cgBuilder.buildClassTargetSelector();
        cgBuilder.buildMethodTargetSelector();
        cgBuilder.buildContextSelector();
        cgBuilder.buildContextInterpretor();
        cgBuilder.buildCallGraph();
        callGraph = cgBuilder.getCallGraph();
    }

    private void createHeapHelper() {
        final PointerAnalysis pointerAnalysis = cgBuilder.getPointerAnalysis();
        heapHelper = new HeapHelper(pointerAnalysis.getHeapGraph(), callGraph);
    }
}

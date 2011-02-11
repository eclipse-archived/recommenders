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
package org.eclipse.recommenders.internal.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Collection;

import org.eclipse.recommenders.commons.utils.Throws;
import org.eclipse.recommenders.internal.commons.analysis.selectors.BypassingAbstractClassesClassTargetSelector;
import org.eclipse.recommenders.internal.commons.analysis.selectors.RestrictedDeclaringClassMethodTargetSelector;

import com.google.inject.Inject;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class InstanceCallGraphBuilder implements ICallGraphBuilder {
    public static void build(final InstanceCallGraphBuilder cgBuilder, final Collection<Entrypoint> entryPoints,
            final IClass thisType) {
        cgBuilder.setEntryPoints(entryPoints);
        cgBuilder.setThisType(thisType);
        cgBuilder.buildClassTargetSelector();
        cgBuilder.buildMethodTargetSelector();
        cgBuilder.buildContextSelector();
        cgBuilder.buildContextInterpretor();
        cgBuilder.buildCallGraph();
    }

    private final AnalysisOptions options;

    private final SSAPropagationCallGraphBuilder callGraphBuilder;

    private CallGraph callGraph;

    private IClass thisType;

    @Inject
    public InstanceCallGraphBuilder(final AnalysisOptions options, final AnalysisCache cache, final IClassHierarchy cha) {
        this.options = ensureIsNotNull(options);
        callGraphBuilder = Util.makeVanillaZeroOneCFABuilder(options, cache, cha, cha.getScope());
    }

    public void setEntryPoints(final Collection<Entrypoint> entrypoints) {
        callGraphBuilder.getOptions().setEntrypoints(entrypoints);
    }

    @Override
    public void buildClassTargetSelector() {
        final BypassingAbstractClassesClassTargetSelector myClassTargetSelector = new BypassingAbstractClassesClassTargetSelector();
        options.setSelector(myClassTargetSelector);
    }

    @Override
    public void buildMethodTargetSelector() {
        final MethodTargetSelector delegate = options.getMethodTargetSelector();
        final RestrictedDeclaringClassMethodTargetSelector mySelector = new RestrictedDeclaringClassMethodTargetSelector(
                delegate, thisType, callGraphBuilder);
        options.setSelector(mySelector);
    }

    @Override
    public void buildContextSelector() {
        // final ContextSelector delegate =
        // callGraphBuilder.getContextSelector();
        // final OneLevelSiteContextSelector selector = new
        // OneLevelSiteContextSelector(delegate);
        // callGraphBuilder.setContextSelector(selector);
        final ContextSelector delegate = callGraphBuilder.getContextSelector();
        final nCFAContextSelector newContextSelector = new nCFAContextSelector(5, delegate);
        callGraphBuilder.setContextSelector(newContextSelector);
    }

    @Override
    public void buildContextInterpretor() {
        // stick with the default one.
        // final RTAContextInterpreter contextInterpreter =
        // callGraphBuilder.getContextInterpreter();
        // System.out.println();
    }

    @Override
    public void buildCallGraph() {
        try {
            callGraph = callGraphBuilder.makeCallGraph(options);
        } catch (final Exception e) {
            throw Throws.throwUnhandledException(e);
        }
    }

    @Override
    public CallGraph getCallGraph() {
        return callGraph;
    }

    @Override
    public PointerAnalysis getPointerAnalysis() {
        return callGraphBuilder.getPointerAnalysis();
    }

    public void setThisType(final IClass clazz) {
        this.thisType = clazz;
    }
}

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
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.ClassTargetSelector;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.BypassClassTargetSelector;
import com.ibm.wala.ipa.summaries.BypassMethodTargetSelector;
import com.ibm.wala.ipa.summaries.XMLMethodSummaryReader;
import com.ibm.wala.util.strings.Atom;

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
    public InstanceCallGraphBuilder(final AnalysisOptions options, final AnalysisCache cache,
            final IClassHierarchy cha, final XMLMethodSummaryReader summary) {
        this.options = ensureIsNotNull(options);
        callGraphBuilder = copyit(options, cache, cha, summary);
    }

    private ZeroXCFABuilder copyit(final AnalysisOptions options, final AnalysisCache cache, final IClassHierarchy cha,
            final XMLMethodSummaryReader summary) {
        options.setSelector(new ClassHierarchyMethodTargetSelector(cha));
        options.setSelector(new ClassHierarchyClassTargetSelector(cha));

        final MethodTargetSelector ms = new BypassMethodTargetSelector(options.getMethodTargetSelector(),
                summary.getSummaries(), summary.getIgnoredPackages(), cha);
        options.setSelector(ms);
        final AnalysisScope scope = cha.getScope();
        final ClassTargetSelector cs = new BypassClassTargetSelector(options.getClassTargetSelector(),
                summary.getAllocatableClasses(), cha, cha.getLoader(scope.getLoader(Atom
                        .findOrCreateUnicodeAtom("Synthetic"))));
        options.setSelector(cs);
        final SSAContextInterpreter appContextInterpreter = null;
        final ContextSelector appContextSelector = null;
        final ZeroXCFABuilder builder = new ZeroXCFABuilder(cha, options, cache, appContextSelector,
                appContextInterpreter, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.CONSTANT_SPECIFIC);
        return builder;
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

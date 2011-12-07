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
package org.eclipse.recommenders.internal.analysis.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ParameterCallsitesCallGraphVisitor extends SSAInstruction.Visitor implements Runnable {
    private final CallGraph callGraph;

    private final HeapHelper heap;

    private final Multimap<InstanceKey, WalaParameterCallsite> instances;

    final Set<CGNode> visitedCallGraphNodes = Sets.newHashSet();

    private final IClassHierarchy cha;

    // private MethodVariableTable curVariableNameHelper;
    private LocalNamesCollector curVariableNameHelper;

    private CGNode curNode;

    private IMethod curMethod;

    public ParameterCallsitesCallGraphVisitor(final CallGraph callGraph, final HeapHelper heap) {
        this.callGraph = callGraph;
        this.heap = heap;
        this.instances = HashMultimap.create();
        cha = callGraph.getClassHierarchy();
    }

    @Override
    public void run() {
        traverseCallGraph();
    }

    private void traverseCallGraph() {
        final Collection<CGNode> entrypointNodes = callGraph.getEntrypointNodes();
        for (final CGNode node : entrypointNodes) {
            traverseCallGraphNode(node);
        }
    }

    private void traverseCallGraphNode(final CGNode node) {
        if (!checkNodeIsTraversable(node)) {
            return;
        }
        this.curNode = node;
        this.curMethod = node.getMethod();
        final IR ir = node.getIR();
        curVariableNameHelper = new LocalNamesCollector(ir);
        ir.visitNormalInstructions(this);
        for (final Iterator<CGNode> it = callGraph.getSuccNodes(node); it.hasNext();) {
            final CGNode succNode = it.next();
            traverseCallGraphNode(succNode);
        }
    }

    private boolean checkNodeIsTraversable(final CGNode node) {
        final boolean additionOfNodeModfiedSet = visitedCallGraphNodes.add(node);
        return additionOfNodeModfiedSet && hasIR(node);
    }

    private boolean hasIR(final CGNode node) {
        return node.getIR() != null;
    }

    @Override
    public void visitInvoke(final SSAInvokeInstruction instruction) {
        final IMethod targetMethod = findTargetMethod(instruction);
        if (targetMethod == null) {
            // logUnableToResolveTargetMethod(instruction);
            return;
        }
        final int lineNumber = WalaAnalysisUtils.getLineNumber(curMethod, instruction);
        // ignore this for non-static calls since they are not classic parameter
        // call sites... but maybe...
        final int startIndex = instruction.isStatic() ? 0 : 1;
        final int usesCount = instruction.getNumberOfParameters();
        //
        for (int argIndex = startIndex; argIndex < usesCount; argIndex++) {
            final int use = instruction.getUse(argIndex);
            final String argumentName = curVariableNameHelper.getName(use);
            final Set<InstanceKey> usesForArgAtIndexI = getFilteredArguments(instruction, targetMethod, use);
            /*
             * wala uses 'this' as first argument to a method call. I would like
             * to keep method arguments starting with '0'. That's why we
             * decrease the argumentIndex by one here.
             */
            final int realArgumentIndex = argIndex - 1;
            final WalaParameterCallsite parameterCallSite = WalaParameterCallsite.create(curMethod, argumentName,
                    targetMethod, realArgumentIndex, lineNumber);
            addCallSiteToReceivers(usesForArgAtIndexI, parameterCallSite);
        }
    }

    private Set<InstanceKey> getFilteredArguments(final SSAInvokeInstruction instruction, final IMethod targetMethod,
            final int valuePointer) {
        final Set<InstanceKey> instanceKeys = heap.getInstanceKeys(curNode, valuePointer);
        return instanceKeys;
    }

    private void addCallSiteToReceivers(final Set<InstanceKey> receivers, final WalaParameterCallsite callSite) {
        for (final InstanceKey receiver : receivers) {
            instances.put(receiver, callSite);
        }
    }

    private IMethod findTargetMethod(final SSAAbstractInvokeInstruction instruction) {
        return cha.resolveMethod(instruction.getDeclaredTarget());
    }

    public Multimap<InstanceKey, WalaParameterCallsite> getInstances() {
        return instances;
    }
}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;

public class ReceiverCallsitesCallGraphVisitor extends SSAInstruction.Visitor implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CallGraph callGraph;

    private final HeapHelper heap;

    private final Multimap<InstanceKey, WalaReceiverCallsite> instances;

    final Set<CGNode> visitedCallGraphNodes = Sets.newHashSet();

    private IMethod entrypointMethod;

    private final IClassHierarchy cha;

    // private MethodVariableTable curVariableNameHelper;
    private LocalNamesCollector curVariableNameHelper;

    private CGNode curNode;

    private IMethod curMethod;

    public ReceiverCallsitesCallGraphVisitor(final CallGraph callGraph, final HeapHelper heap) {
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
            entrypointMethod = node.getMethod();
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
        if (instruction.isStatic()) {
            // static calls are ignored/omitted right now
            return;
        }
        final IMethod targetMethod = findTargetMethod(instruction);
        if (targetMethod == null) {
            // logUnableToResolveTargetMethod(instruction);
            return;
        }
        final int lineNumber = WalaAnalysisUtils.getLineNumber(curMethod, instruction);
        final Set<InstanceKey> receivers = getFilteredReceivers(instruction, targetMethod, lineNumber);
        final String receiver = curVariableNameHelper.getName(instruction.getReceiver());
        final WalaReceiverCallsite receiverCallSite = WalaReceiverCallsite.create(curMethod, receiver, targetMethod,
                lineNumber);
        addCallSiteToReceivers(receivers, receiverCallSite);
    }

    private Set<InstanceKey> getFilteredReceivers(final SSAInvokeInstruction instruction, final IMethod targetMethod,
            final int lineNumber) {
        final Set<InstanceKey> receivers = getInstanceKeysForReceiver(instruction);
        filterBuggyReceivers(receivers, targetMethod, lineNumber);
        return receivers;
    }

    private void addCallSiteToReceivers(final Set<InstanceKey> receivers, final WalaReceiverCallsite callSite) {
        for (final InstanceKey receiver : receivers) {
            instances.put(receiver, callSite);
        }
    }

    private IMethod findTargetMethod(final SSAAbstractInvokeInstruction instruction) {
        return cha.resolveMethod(instruction.getDeclaredTarget());
    }

    private Set<InstanceKey> getInstanceKeysForReceiver(final SSAAbstractInvokeInstruction instruction) {
        return heap.getInstanceKeys(curNode, instruction.getReceiver());
    }

    private void filterBuggyReceivers(final Set<InstanceKey> receivers, final IMethod method, final int lineNumber) {
        final Iterator<InstanceKey> it = receivers.iterator();
        while (it.hasNext()) {
            final InstanceKey receiver = it.next();
            if (!hasDeclaredMethod(receiver, method)) {
                it.remove();
                logAnalysisBugReceiverDoesNotHaveMethod(receiver, method, lineNumber);
            }
        }
    }

    private boolean hasDeclaredMethod(final InstanceKey receiver, final IMethod targetMethod) {
        if (targetMethod.isInit()) {
            // super constructor calls on this are not resolved by cha!
            return true;
        }
        final IClass type = receiver.getConcreteType();
        final IMethod resolved = cha.resolveMethod(type, targetMethod.getSelector());
        return resolved != null;
    }

    private void logAnalysisBugReceiverDoesNotHaveMethod(final InstanceKey receiver, final IMethod targetMethod,
            final int lineNumber) {
        final String msg = String
                .format("Analysis Bug: Type '%s' does not declare a method '%s' as the static analysis makes us believe. This illegal call has been observed in source method '%s', line %d",
                        receiver.getConcreteType().getName(), targetMethod.getSignature(),
                        entrypointMethod.getSignature(), lineNumber);
        log.error(msg);
    }

    public Multimap<InstanceKey, WalaReceiverCallsite> getInstances() {
        return instances;
    }
}

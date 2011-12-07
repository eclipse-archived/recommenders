/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.chain.jdt;

import static org.eclipse.recommenders.completion.rcp.chain.jdt.InternalAPIsHelper.findAllPublicInstanceFieldsAndNonVoidNonPrimitiveMethods;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Checks.cast;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Throws.throwCancelationException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Optional;

public class CallChainGraphBuilder {

    /**
     * @see VisitEdgeJob#call() decrements the counter when done.
     * @see #scheduleNextIteration(List) increments the counter for a bunch of jobs. NOTE, if the counter is increased
     *      of for a single job one-by-one and not as a bulk operation, it might happen that the pool is shutdown before
     *      even the initial jobs were scheduled!
     */
    private final AtomicInteger scheduledJobs = new AtomicInteger();

    private final class VisitEdgeJob implements Callable<Void> {
        private final CallChainEdge newEdge;

        private VisitEdgeJob(final CallChainEdge newEdge) {
            this.newEdge = newEdge;
        }

        @Override
        public Void call() throws Exception {
            visitEdge(newEdge);
            final int numberOfScheduledJobs = scheduledJobs.decrementAndGet();
            if (numberOfScheduledJobs == 0) {
                pool.shutdown();
            }
            return null;
        }

        @Override
        public String toString() {
            return newEdge.toString();
        }
    }

    // private final ScheduledExecutorService timeout = Executors.newScheduledThreadPool(1);
    private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime()
            .availableProcessors());

    private final int maxdepth = 5;

    private final Map<IType, CallChainTypeNode> nodes = Collections
            .synchronizedMap(new HashMap<IType, CallChainTypeNode>());
    private final List<List<CallChainEdge>> chains = new CopyOnWriteArrayList<List<CallChainEdge>>();

    public void build(final List<CallChainEdge> entrypoints) {
        scheduleEntrypoints(entrypoints);
        awaitPoolTermination();
    }

    private void scheduleEntrypoints(final List<CallChainEdge> entrypoints) {
        final LinkedList<VisitEdgeJob> iteration = new LinkedList<VisitEdgeJob>();
        for (final CallChainEdge entrypoint : entrypoints) {
            iteration.add(new VisitEdgeJob(entrypoint));
        }
        scheduleNextIteration(iteration);

    }

    private void visitEdge(final CallChainEdge edge) throws JavaModelException {
        terminateIfInterrupted();
        final Optional<IType> opt = edge.getReturnType();
        if (opt.isPresent()) {
            inspectReturnTypeAndRegisterNewNodesAndEdges(edge, opt.get());
        }
    }

    private synchronized void inspectReturnTypeAndRegisterNewNodesAndEdges(final CallChainEdge edge,
            final IType returnType) throws JavaModelException {
        if (nodes.containsKey(returnType)) {
            registerIncomingEdge(edge, returnType);
        } else {
            registerNewNode(returnType);
            registerIncomingEdge(edge, returnType);
            addNewEdgesIntoWorklist(edge, returnType);
        }
    }

    private void addNewEdgesIntoWorklist(final CallChainEdge edge, final IType returnType) throws JavaModelException {
        final List<VisitEdgeJob> nextIteration = new LinkedList<VisitEdgeJob>();

        final Collection<IMember> allMethodsAndFields = findAllPublicInstanceFieldsAndNonVoidNonPrimitiveMethods(returnType);
        for (final IJavaElement element : allMethodsAndFields) {
            CallChainEdge newEdge = null;
            switch (element.getElementType()) {
            case IJavaElement.METHOD:
                final IMethod m = (IMethod) element;
                newEdge = new CallChainEdge(returnType, m);
                nextIteration.add(new VisitEdgeJob(newEdge));
                break;
            case IJavaElement.FIELD:
                final IField f = (IField) element;
                newEdge = new CallChainEdge(returnType, f);
                nextIteration.add(new VisitEdgeJob(newEdge));
                break;
            default:
                break;
            }
        }
        scheduleNextIteration(nextIteration);
    }

    private void scheduleNextIteration(final List<VisitEdgeJob> iteration) {

        // ATTENTION: increment all at once! Otherwise, the pool may be shutdown before the last job has been executed!
        scheduledJobs.addAndGet(iteration.size());
        for (final VisitEdgeJob job : iteration) {
            pool.submit(job);
        }
    }

    private void awaitPoolTermination() {
        try {
            pool.awaitTermination(4, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            // no logging intended
        }
    }

    public List<List<CallChainEdge>> findChains(final IType expectedType) {
        for (final CallChainTypeNode node : nodes.values()) {
            if (node.isAssignable(expectedType)) {
                for (final CallChainEdge edge : node.incomingEdges) {
                    final LinkedHashSet<CallChainEdge> anchor = new LinkedHashSet<CallChainEdge>();
                    dsfTraverse(anchor, edge);
                }
            }
        }
        return chains;

    }

    // TODO should we use bsf instead?
    private void dsfTraverse(final LinkedHashSet<CallChainEdge> incompleteChain, final CallChainEdge edgeToTest) {
        terminateIfInterrupted();

        if (incompleteChain.contains(edgeToTest)) {
            return;
        }

        final LinkedHashSet<CallChainEdge> workingCopy = createWorkingCopyWithNewEdge(incompleteChain, edgeToTest);

        if (reachedCallChainMaxLengthLimit(workingCopy)) {
            return;
        }

        if (edgeToTest.isChainAnchor()) {
            registerCopyOfSuccessfullyCompletedChain(workingCopy);
            return;
        }

        final IType accessedFrom = edgeToTest.getSourceType().get();
        final CallChainTypeNode typeNode = nodes.get(accessedFrom);

        for (final CallChainEdge nextEdgeToTest : typeNode.incomingEdges) {
            // round n+1
            dsfTraverse(workingCopy, nextEdgeToTest);
        }
    }

    private void terminateIfInterrupted() {
        if (Thread.interrupted()) {
            throwCancelationException();
        }
    }

    private LinkedHashSet<CallChainEdge> createWorkingCopyWithNewEdge(
            final LinkedHashSet<CallChainEdge> incompleteChain, final CallChainEdge edgeToTest) {
        final LinkedHashSet<CallChainEdge> workingCopy = cast(incompleteChain.clone());
        workingCopy.add(edgeToTest);
        return workingCopy;
    }

    private boolean reachedCallChainMaxLengthLimit(final LinkedHashSet<CallChainEdge> chain) {
        return chain.size() > maxdepth;
    }

    private void registerCopyOfSuccessfullyCompletedChain(final LinkedHashSet<CallChainEdge> almostCompleteCallChain) {
        final List<CallChainEdge> copy = new LinkedList<CallChainEdge>(almostCompleteCallChain);
        Collections.reverse(copy);
        chains.add(copy);
    }

    private void registerIncomingEdge(final CallChainEdge edge, final IType returnType) {
        final CallChainTypeNode node = nodes.get(returnType);
        node.incomingEdges.add(edge);
    }

    private void registerNewNode(final IType returnType) {
        // System.out.println("new node " + returnType);
        final CallChainTypeNode newNode = new CallChainTypeNode(returnType);
        nodes.put(returnType, newNode);
    }
}

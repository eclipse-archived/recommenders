/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Stefan Henss - re-implementation in response to https://bugs.eclipse.org/bugs/show_bug.cgi?id=376796.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain;

import static org.eclipse.recommenders.internal.completion.rcp.chain.TypeBindingAnalyzer.findVisibleInstanceFieldsAndRelevantInstanceMethods;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ChainFinder {

    private final TypeBinding expectedType;
    private final int expectedDimension;

    private final InvocationSite invocationSite;
    private final Scope scope;

    private final List<List<ChainElement>> chains = Lists.newLinkedList();

    private final Map<Binding, ChainElement> edgeCache = Maps.newHashMap();
    private final Map<TypeBinding, Collection<Binding>> fieldsAndMethodsCache = Maps.newHashMap();
    private final Map<ChainElement, Boolean> assignableCache = Maps.newHashMap();

    ChainFinder(final TypeBinding expectedType, final InvocationSite invocationSite, final Scope scope) {
        if (expectedType instanceof ArrayBinding) {
            this.expectedType = TypeBindingAnalyzer.removeArrayWrapper(expectedType);
            expectedDimension = ((ArrayBinding) expectedType).dimensions();
        } else {
            this.expectedType = expectedType;
            expectedDimension = 0;
        }
        this.invocationSite = invocationSite;
        this.scope = scope;
    }

    void startChainSearch(final List<ChainElement> entrypoints, final int maxChains, final int minDepth,
            final int maxDepth) {
        final LinkedList<LinkedList<ChainElement>> incompleteChains = prepareQueue(entrypoints);

        while (!incompleteChains.isEmpty()) {
            final LinkedList<ChainElement> chain = incompleteChains.poll();
            final ChainElement edge = chain.getLast();
            if (isValidEndOfChain(edge)) {
                if (isValidChain(chain, minDepth)) {
                    chains.add(chain);
                    if (chains.size() == maxChains) {
                        break;
                    }
                }
                continue;
            }
            if (chain.size() < maxDepth) {
                searchDeeper(chain, incompleteChains, edge.getReturnType());
            }
        }
    }

    /**
     * Returns the potentially incomplete list of call chains that could be found before a time out happened. The
     * contents of this list are mutable and may change as the search makes progress.
     */
    public List<List<ChainElement>> getChains() {
        return chains;
    }

    private static LinkedList<LinkedList<ChainElement>> prepareQueue(final List<ChainElement> entrypoints) {
        final LinkedList<LinkedList<ChainElement>> incompleteChains = Lists.newLinkedList();
        for (final ChainElement entrypoint : entrypoints) {
            final LinkedList<ChainElement> chain = Lists.newLinkedList();
            chain.add(entrypoint);
            incompleteChains.add(chain);
        }
        return incompleteChains;
    }

    private boolean isValidEndOfChain(final ChainElement edge) {
        Boolean isAssignable = assignableCache.get(edge);
        if (isAssignable == null) {
            isAssignable = TypeBindingAnalyzer.isAssignable(edge, expectedType, expectedDimension);
            assignableCache.put(edge, isAssignable);
        }
        return isAssignable.booleanValue();
    }

    private static boolean isValidChain(final LinkedList<ChainElement> chain, final int minDepth) {
        if (chain.size() < minDepth) {
            return false;
        }
        if (chain.size() == 1 && chain.get(0).getElementType() != ChainElement.ElementType.METHOD) {
            return false;
        }
        return true;
    }

    private void searchDeeper(final LinkedList<ChainElement> chain,
            final List<LinkedList<ChainElement>> incompleteChains, final TypeBinding currentlyVisitedType) {
        for (final Binding element : findAllFieldsAndMethods(currentlyVisitedType)) {
            final ChainElement newEdge = createEdge(element);
            if (!chain.contains(newEdge)) {
                incompleteChains.add(cloneChainAndAppendEdge(chain, newEdge));
            }
        }
    }

    private Collection<Binding> findAllFieldsAndMethods(final TypeBinding chainElementType) {
        Collection<Binding> cached = fieldsAndMethodsCache.get(chainElementType);
        if (cached == null) {
            cached = findVisibleInstanceFieldsAndRelevantInstanceMethods(chainElementType, invocationSite, scope);
            fieldsAndMethodsCache.put(chainElementType, cached);
        }
        return cached;
    }

    private ChainElement createEdge(final Binding member) {
        ChainElement cached = edgeCache.get(member);
        if (cached == null) {
            cached = new ChainElement(member, false);
            edgeCache.put(member, cached);
        }
        return cached;
    }

    private static LinkedList<ChainElement> cloneChainAndAppendEdge(final LinkedList<ChainElement> chain,
            final ChainElement newEdge) {
        final LinkedList<ChainElement> chainCopy = cast(chain.clone());
        chainCopy.add(newEdge);
        return chainCopy;
    }

}

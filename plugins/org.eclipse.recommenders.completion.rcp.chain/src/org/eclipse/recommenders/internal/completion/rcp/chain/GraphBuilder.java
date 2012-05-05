/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Stefan Henß - re-implementation in response to https://bugs.eclipse.org/bugs/show_bug.cgi?id=376796.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain;

import static org.eclipse.jdt.internal.corext.util.JdtFlags.isPublic;
import static org.eclipse.jdt.internal.corext.util.JdtFlags.isStatic;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findAllRelevanFieldsAndMethods;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.hasPrimitiveReturnType;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.isVoid;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

/**
 * A graph builder creates the call chain graph from a list of given entry points.
 * 
 * @see MemberEdge
 */
public class GraphBuilder {

    private static final int maxdepth = 4;
    private static final int maxchains = 20;

    private static Predicate<IField> FILTER_FIELDS = new Predicate<IField>() {

        public boolean apply(final IField m) {
            try {
                return isStatic(m);
            } catch (final Exception e) {
                return true;
            }
        }
    };
    private static Predicate<IMethod> FILTER_METHODS = new Predicate<IMethod>() {

        public boolean apply(final IMethod m) {
            try {
                if (isVoid(m) || m.isConstructor() || isStatic(m) || hasPrimitiveReturnType(m)) {
                    return true;
                }
                return m.getElementName().equals("toString") && m.getSignature().equals("()java.lang.String;");
            } catch (final Exception e) {
                return true;
            }
        }
    };

    private final List<List<MemberEdge>> chains = Lists.newLinkedList();

    private final Map<IJavaElement, MemberEdge> edgeCache = Maps.newHashMap();
    private final Map<IType, Collection<IMember>> fieldsAndMethodsCache = Maps.newHashMap();
    private final Table<MemberEdge, IType, Boolean> assignableCache = HashBasedTable.create();

    void startChainSearch(final IJavaElement enclosingElement, final List<MemberEdge> entrypoints,
            final IType expectedType, final int expectedDimension) {
        final LinkedList<LinkedList<MemberEdge>> incompleteChains = prepareQueue(entrypoints);
        final IType enclosingType = (IType) enclosingElement.getAncestor(IJavaElement.TYPE);
        while (!incompleteChains.isEmpty()) {
            final LinkedList<MemberEdge> chain = incompleteChains.poll();
            final MemberEdge edge = chain.getLast();
            final Optional<IType> returnTypeOpt = edge.getReturnType();
            if (!returnTypeOpt.isPresent()) {
                continue;
            }
            if (isAssignableTo(edge, expectedType, expectedDimension)) {
                if (chain.size() > 1) {
                    chains.add(chain);
                    if (chains.size() == maxchains) {
                        break;
                    }
                }
                continue;
            }
            if (chain.size() >= maxdepth) {
                continue;
            }
            final Collection<IMember> allMethodsAndFields = findAllFieldsAndMethods(returnTypeOpt.get(), enclosingType);
            for (final IJavaElement element : allMethodsAndFields) {
                final MemberEdge newEdge = createEdge(element);
                if (!chain.contains(newEdge)) {
                    incompleteChains.add(cloneChainAndAppend(chain, newEdge));
                }
            }
        }
    }

    /**
     * Returns the potentially incomplete list of call chains that could be found before a time out happened. The
     * contents of this list are mutable and may change as the search makes progress.
     */
    public List<List<MemberEdge>> getChains() {
        return chains;
    }

    private static LinkedList<LinkedList<MemberEdge>> prepareQueue(final List<MemberEdge> entrypoints) {
        final LinkedList<LinkedList<MemberEdge>> incompleteChains = Lists.newLinkedList();
        for (final MemberEdge entrypoint : entrypoints) {
            final LinkedList<MemberEdge> chain = Lists.newLinkedList();
            chain.add(entrypoint);
            incompleteChains.add(chain);
        }
        return incompleteChains;
    }

    private boolean isAssignableTo(final MemberEdge edge, final IType expectedType, final int expectedDimension) {
        Boolean isAssignable = assignableCache.get(edge, expectedType);
        if (isAssignable == null) {
            isAssignable = expectedDimension <= edge.getDimension()
                    && JdtUtils.isAssignable(expectedType, edge.getReturnType().get());
            assignableCache.put(edge, expectedType, isAssignable);
        }
        return isAssignable;
    }

    private Collection<IMember> findAllFieldsAndMethods(final IType chainElementType, final IType contextEnclosingType) {
        Collection<IMember> cached = fieldsAndMethodsCache.get(chainElementType);
        if (cached == null) {
            cached = Lists.newLinkedList();
            final boolean isEverythingVisible = isVisibleFromCompletionContext(contextEnclosingType, chainElementType);
            for (final IMember element : findAllRelevanFieldsAndMethods(chainElementType, FILTER_FIELDS, FILTER_METHODS)) {
                try {
                    if (!isEverythingVisible && !isPublic(element)) {
                        continue;
                    }
                } catch (final Exception e) {
                    // TODO: proper exception handling.
                    e.printStackTrace();
                    continue;
                }
                cached.add(element);
            }
            fieldsAndMethodsCache.put(chainElementType, cached);
        }
        return cached;
    }

    private static boolean isVisibleFromCompletionContext(final IType contextEnclosingType, final IType element) {
        // TODO: Check type hierarchy and visibility levels.
        return contextEnclosingType.equals(element);
    }

    private MemberEdge createEdge(final IJavaElement member) {
        MemberEdge cached = edgeCache.get(member);
        if (cached == null) {
            cached = new MemberEdge(member);
            edgeCache.put(member, cached);
        }
        return cached;
    }

    private static LinkedList<MemberEdge> cloneChainAndAppend(final LinkedList<MemberEdge> chain,
            final MemberEdge newEdge) {
        final LinkedList<MemberEdge> chainCopy = cast(chain.clone());
        chainCopy.add(newEdge);
        return chainCopy;
    }

}

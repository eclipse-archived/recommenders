/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.extdocs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.TreeBag;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ReceiverCallSite;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MethodSelfcallDirectivesGenerator {

    private Map<IMethodName, TreeBag<IMethodName>> globalSupermethodsIndex;
    private TreeBag<IMethodName> globalImlementorsCounter;

    public void initialize() {
        globalSupermethodsIndex = Maps.newHashMap();
        globalImlementorsCounter = TreeBag.newTreeBag();
    }

    public void analyzeCompilationUnits(final Iterable<CompilationUnit> cus) {
        for (final CompilationUnit cu : cus) {
            analyzeCompilationUnit(cu);
        }
    }

    public List<MethodSelfcallDirectives> generate() {
        final List<MethodSelfcallDirectives> res = Lists.newLinkedList();
        for (final Entry<IMethodName, TreeBag<IMethodName>> entry : globalSupermethodsIndex.entrySet()) {
            final IMethodName superMethod = entry.getKey();
            final TreeBag<IMethodName> calls = entry.getValue();
            final int numberOfSubclasses = globalImlementorsCounter.count(superMethod);
            filterInfrequentMethods(calls, numberOfSubclasses);
            if (calls.isEmpty()) {
                continue;
            }
            final MethodSelfcallDirectives directive = toDirective(superMethod, calls);
            res.add(directive);
        }
        return res;
    }

    private void analyzeCompilationUnit(final CompilationUnit cu) {
        final TypeDeclaration subclass = cu.primaryType;
        for (final MethodDeclaration method : subclass.methods) {
            if (method.superDeclaration == null) {
                continue;
            }
            final IMethodName superMethodName = method.superDeclaration;
            final Set<IMethodName> observedSelfCalls = Sets.newHashSet();
            for (final ObjectInstanceKey obj : method.objects) {
                if (!obj.isThis()) {
                    continue;
                }
                for (final ReceiverCallSite callsite : obj.receiverCallSites) {
                    observedSelfCalls.add(callsite.targetMethod);
                }
                TreeBag<IMethodName> globalSelfCalls = globalSupermethodsIndex.get(superMethodName);
                if (globalSelfCalls == null) {
                    globalSelfCalls = TreeBag.newTreeBag();
                    globalSupermethodsIndex.put(superMethodName, globalSelfCalls);
                }
                globalSelfCalls.addAll(observedSelfCalls);
                globalImlementorsCounter.add(method.superDeclaration);
            }
        }
    }

    private MethodSelfcallDirectives toDirective(final IMethodName superMethod, final TreeBag<IMethodName> value) {
        final int numberOfSubclasses = globalImlementorsCounter.count(superMethod);
        final MethodSelfcallDirectives res = MethodSelfcallDirectives.create(superMethod, numberOfSubclasses,
                value.asMap());
        res.validate();
        return res;
    }

    private void filterInfrequentMethods(final TreeBag<IMethodName> value, final int numberOfSubclasses) {
        for (final IMethodName method : Sets.newHashSet(value.elements())) {
            final int timesObserved = value.count(method);
            final double percentage = 100 * timesObserved / (double) numberOfSubclasses;
            if (percentage < 5) {
                value.removeAll(method);
            }
        }
    }
}

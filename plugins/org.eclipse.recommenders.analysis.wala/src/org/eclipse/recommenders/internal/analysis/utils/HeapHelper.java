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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.NormalAllocationInNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

/**
 * An dreadful helper-class which filters the manually introduced instance keys
 * in the heap model ( e.g, the field initializations created inside the
 * recommenders-initializers).
 */
public class HeapHelper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final HeapGraph heapgraph;

    private final HeapModel model;

    private final Multimap<PointerKey, InstanceKey> helper = HashMultimap.create();

    public HeapHelper(final HeapGraph graph, final CallGraph callgraph) {
        heapgraph = graph;
        model = graph.getHeapModel();
        resolveDanglingReferences(callgraph);
    }

    public Set<InstanceKey> getInstanceKeys(final CGNode node, final int valueNumber) {
        final PointerKey pointerKey = getPointerKey(node, valueNumber);
        return getInstanceKeys(pointerKey);
    }

    public Set<InstanceKey> getInstanceKeys(final PointerKey pointer) {
        final HashSet<InstanceKey> res = new HashSet<InstanceKey>();
        collectInstanceKeys(pointer, res);
        filterUnnecessaryRecommendersInitAllocations(res);
        if (res.isEmpty() && helper.containsKey(pointer)) {
            // res = helper.get(pointer);
        }
        return res;
    }

    public HeapModel getModel() {
        return model;
    }

    private void collectInstanceKeys(final PointerKey pointer, final HashSet<InstanceKey> res) {
        for (final Iterator<?> it = heapgraph.getSuccNodes(pointer); it.hasNext();) {
            final Object succ = it.next();
            if (succ instanceof InstanceKey) {
                res.add((InstanceKey) succ);
            } else {
                collectInstanceKeys(pointer, res);
            }
        }
    }

    private void filterUnnecessaryRecommendersInitAllocations(final HashSet<InstanceKey> res) {
        if (res.size() < 2) {
            return;
        }
        for (final Iterator<InstanceKey> it = res.iterator(); it.hasNext();) {
            final InstanceKey key = it.next();
            if (key instanceof NormalAllocationInNode) {
                final NormalAllocationInNode n = (NormalAllocationInNode) key;
                if (RecommendersInits.isRecommendersInit(n.getNode())) {
                    it.remove();
                }
            }
        }
        if (res.size() == 0) {
            log.error("filtered all instancekey recommenders-init instances???");
        }
    }

    private PointerKey getPointerKey(final CGNode node, final int valueNumber) {
        return model.getPointerKeyForLocal(node, valueNumber);
    }

    private void resolveDanglingReferences(final CallGraph callgraph) {
        final Queue<CGNode> q = new LinkedList<CGNode>(callgraph.getEntrypointNodes());
        final HashSet<CGNode> traversed = new HashSet<CGNode>();
        final HashMap<IField, InstanceKey> tmp = new HashMap<IField, InstanceKey>();
        while (!q.isEmpty()) {
            final CGNode cur = q.poll();
            final IR ir = cur.getIR();
            if (ir == null) {
                continue;
            }
            ir.visitNormalInstructions(new SSAInstruction.Visitor() {
                @Override
                public void visitGet(final SSAGetInstruction instruction) {
                    final PointerKey pointerKey = getPointerKey(cur, instruction.getDef());
                    final Collection<InstanceKey> instanceKeys = getInstanceKeys(pointerKey);
                    if (instanceKeys.isEmpty()) {
                        final IField field = callgraph.getClassHierarchy().resolveField(instruction.getDeclaredField());
                        if (field == null) {
                            log.warn("Failed to resolve field: " + instruction.getDeclaredField());
                            return;
                        }
                        InstanceKey instanceKey = tmp.get(field);
                        if (instanceKey == null) {
                            final IClass fieldtype = field.getClassHierarchy().lookupClass(
                                    field.getFieldTypeReference());
                            if (fieldtype != null) {
                                instanceKey = new InstanceKey() {
                                    @Override
                                    public IClass getConcreteType() {
                                        return fieldtype;
                                    }

                                    @Override
                                    public String toString() {
                                        return field.getReference().getSignature() + " in "
                                                + cur.getMethod().getSignature();
                                    }
                                };
                            }
                            helper.put(pointerKey, instanceKey);
                            tmp.put(field, instanceKey);
                        }
                    }
                }

                @Override
                public void visitPut(final SSAPutInstruction instruction) {
                    //
                }
            });
            traversed.add(cur);
            for (final CGNode succ : Iterators.toArray(callgraph.getSuccNodes(cur), CGNode.class)) {
                if (!traversed.contains(succ) && !q.contains(succ)) {
                    q.add(succ);
                }
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

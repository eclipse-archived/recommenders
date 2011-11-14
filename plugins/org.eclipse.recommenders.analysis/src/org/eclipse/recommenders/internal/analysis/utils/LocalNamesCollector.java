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

import static org.eclipse.recommenders.utils.Checks.cast;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.ibm.wala.ipa.summaries.SyntheticIR;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

/**
 * Collects the name for all local variables - if available.
 */
public class LocalNamesCollector {

    private final static Map<IR, Multimap<Integer, String>> cache = new MapMaker().maximumSize(100).concurrencyLevel(1)
            .makeMap();

    public static final String UNKNOWN = "unnamed";

    // private static final Logger log =
    // Logger.getLogger(LocalNamesCollector.class);
    private Multimap<Integer/* value number */, String> names = HashMultimap.create();

    private IR ir;

    private List<ISSABasicBlock> basicBlocks;

    private HashSet<Integer> bbEndIndex;

    public LocalNamesCollector(final IR ir) {

        if (ir instanceof SyntheticIR) {
            return;
        } else if (cache.containsKey(ir)) {
            names = cache.get(ir);
        } else {
            cache.put(ir, names);
            storeIR(ir);
            storeBasicBlocks();
            collectParameterNames();
            collectLocalValueNames();
        }
    }

    private void storeIR(final IR ir) {
        this.ir = ir;
    }

    private void storeBasicBlocks() {
        final SSACFG cfg = ir.getControlFlowGraph();
        bbEndIndex = Sets.newHashSet();

        basicBlocks = new LinkedList<ISSABasicBlock>();
        for (final ISSABasicBlock block : cfg) {
            bbEndIndex.add(block.getLastInstructionIndex());
            if (blockHasPositiveLastInstructionIndex(block)) {
                basicBlocks.add(block);
            }
        }
    }

    private boolean blockHasPositiveLastInstructionIndex(final ISSABasicBlock block) {
        return block.getLastInstructionIndex() > -1;
    }

    private void collectParameterNames() {
        for (final int parameterValueNumber : ir.getParameterValueNumbers()) {
            final String[] localNames = ir.getLocalNames(0, parameterValueNumber);
            storeLocalNamesForValueNumber(parameterValueNumber, localNames);
        }
    }

    private void storeLocalNamesForValueNumber(final int valueNumber, final String[] localNames) {
        if (localNames == null) {
            return;
        }
        for (final String name : localNames) {
            if (name == null) {
                continue;
            } else {
                storeLocalNameForValueNumber(valueNumber, name);
            }
        }
    }

    private boolean storeLocalNameForValueNumber(final int valueNumber, final String name) {
        if (Thread.interrupted()) {
            System.out.println("canceled while analyzing " + ir.getMethod().getSignature());
            // throw Throws.throwCancelationException();
        }
        return names.put(valueNumber, name);
    }

    private void collectLocalValueNames() {

        final HashSet<Integer> yetUnresolvedValues = Sets.newHashSet();
        final HashMultimap<Integer, String> fieldAccessNames = HashMultimap.create();
        final SSAInstruction[] instructions = ir.getInstructions();

        for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
            final SSAInstruction instr = instructions[instructionIndex];
            if (instr == null) {
                continue;
            }

            addAllDefs(yetUnresolvedValues, instr);
            addAllUnresolvedUses(yetUnresolvedValues, instr);
            addFieldAccessNames(fieldAccessNames, instr);

            resolveAllUnkownVariableNames(yetUnresolvedValues, instructionIndex);
        }

        for (final Integer vn : yetUnresolvedValues.toArray(new Integer[0])) {
            if (fieldAccessNames.containsKey(vn)) {
                for (final String name : fieldAccessNames.get(vn)) {
                    storeLocalNameForValueNumber(vn, name);
                    yetUnresolvedValues.remove(vn);
                }
            } else {
                storeLocalNameForValueNumber(vn, UNKNOWN);
            }
        }

    }

    private void resolveAllUnkownVariableNames(final HashSet<Integer> yetUnresolvedValues, final int instructionIndex) {
        if (bbEndIndex.contains(instructionIndex)) {
            for (final Integer vn : yetUnresolvedValues.toArray(new Integer[0])) {
                final String[] localNames = ir.getLocalNames(instructionIndex, vn);
                if (localNames == null) {
                    continue;
                }
                for (final String name : localNames) {
                    if (name != null) {
                        storeLocalNameForValueNumber(vn, name);
                        yetUnresolvedValues.remove(vn);
                    }
                }
            }
        }
    }

    private void addFieldAccessNames(final HashMultimap<Integer, String> fieldAccessNames, final SSAInstruction instr) {
        if (instr instanceof SSAGetInstruction) {
            final SSAGetInstruction get = cast(instr);
            final String name = get.getDeclaredField().getName().toString();
            fieldAccessNames.put(get.getDef(), name);
        } else if (instr instanceof SSAPutInstruction) {
            final SSAPutInstruction put = (SSAPutInstruction) instr;
            final String name = put.getDeclaredField().getName().toString();
            fieldAccessNames.put(put.getVal(), name);
        }
    }

    private void addAllUnresolvedUses(final HashSet<Integer> yetUnresolvedValues, final SSAInstruction instr) {
        for (int useIndex = instr.getNumberOfUses(); useIndex-- > 0;) {
            final int vn = instr.getUse(useIndex);
            if (!names.containsKey(vn)) {
                yetUnresolvedValues.add(vn);
            }
        }
    }

    private void addAllDefs(final HashSet<Integer> yetUnresolvedValues, final SSAInstruction instr) {
        for (int defIndex = instr.getNumberOfDefs(); defIndex-- > 0;) {
            final int vn = instr.getDef(defIndex);
            yetUnresolvedValues.add(vn);
        }
    }

    /**
     * Returns the name for a given value number - if one exists.
     * 
     * @return {@link #UNKNOWN} if no name could be found for the given value
     *         number or the first name under which found for this value number
     */
    public String getName(final int valueNumber) {
        final String internalGetName = internalGetName(valueNumber);
        return internalGetName;
    }

    private String internalGetName(final int valueNumber) {
        if (!names.containsKey(valueNumber)) {
            return UNKNOWN;
        }
        final Collection<String> knownNamesForValue = names.get(valueNumber);

        return knownNamesForValue.iterator().next();
    }

    /**
     * Returns the values for which a name could be found.
     */
    public Collection<Integer> getValues() {
        return names.keySet();
    }

    /**
     * Returns the values for which a name could be found.
     */
    public Collection<String> getNames() {
        return names.values();
    }
}

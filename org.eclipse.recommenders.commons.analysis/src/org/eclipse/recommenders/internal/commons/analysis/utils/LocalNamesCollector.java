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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

/**
 * Collects the name for all local variables - if available.
 */
public class LocalNamesCollector {
    public static final String UNKNOWN = "unnamed";

    // private static final Logger log =
    // Logger.getLogger(LocalNamesCollector.class);
    private final Multimap<Integer/* value number */, String> names = HashMultimap.create();

    private IR ir;

    private List<ISSABasicBlock> basicBlocks;

    public LocalNamesCollector(final IR ir) {
        storeIR(ir);
        storeBasicBlocks();
        collectParameterNames();
        collectLocalValueNames();
    }

    private void storeIR(final IR ir) {
        this.ir = ir;
    }

    private void storeBasicBlocks() {
        final SSACFG cfg = ir.getControlFlowGraph();
        basicBlocks = new LinkedList<ISSABasicBlock>();
        for (final ISSABasicBlock block : cfg) {
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
        return names.put(valueNumber, name);
    }

    private void collectLocalValueNames() {
        for (final Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
            final SSAInstruction instr = it.next();
            {
                // check for field access
                if (instr instanceof SSAGetInstruction) {
                    final SSAGetInstruction instruction = (SSAGetInstruction) instr;
                    final String name = instruction.getDeclaredField().getName().toString();
                    final int val = instruction.getDef();
                    storeLocalNameForValueNumber(val, name);
                } else if (instr instanceof SSAPutInstruction) {
                    final SSAPutInstruction instruction = (SSAPutInstruction) instr;
                    final String name = instruction.getDeclaredField().getName().toString();
                    final int val = instruction.getVal();
                    storeLocalNameForValueNumber(val, name);
                } else if (instr instanceof SSACheckCastInstruction) {
                    // TODO cast need special treatment. Instance keys need
                    // refinement because of cast.
                    // handle cases like ' Composite container = (Composite)
                    // super.createDialogArea(parent);'
                    // XXX w/o updating the instance key this results in worse
                    // recommendations. ignore for now and let
                    // instant code completion handle this.
                    //
                    // final SSACheckCastInstruction instruction =
                    // (SSACheckCastInstruction) instr;
                    // final int use = instruction.getUse(0);
                    // final int def = instruction.getDef();
                    // findLocalNamesForValueNumberInBasicBlocks(def);
                    // final String defName = getName(instr.getDef());
                    // final String useName = getName(use);
                    // if (useName == UNKNOWN && defName != UNKNOWN) {
                    // storeLocalNameForValueNumber(use, defName);
                    // }
                }
            }
            for (int defIndex = instr.getNumberOfDefs(); defIndex-- > 0;) {
                final int def = instr.getDef(defIndex);
                findLocalNamesForValueNumberInBasicBlocks(def);
            }
            for (int useIndex = instr.getNumberOfUses(); useIndex-- > 0;) {
                final int use = instr.getUse(useIndex);
                findLocalNamesForValueNumberInBasicBlocks(use);
            }
        }
    }

    private void findLocalNamesForValueNumberInBasicBlocks(final int valueNumber) {
        if (names.containsKey(valueNumber)) {
            return;
        }
        for (final ISSABasicBlock block : basicBlocks) {
            final int last = block.getLastInstructionIndex();
            final String[] localNames = ir.getLocalNames(last, valueNumber);
            storeLocalNamesForValueNumber(valueNumber, localNames);
        }
    }

    /**
     * Returns the name for a given value number - if one exists.
     * 
     * @return {@link #UNKNOWN} if no name could be found for the given value
     *         number or the first name under which found for this value number
     */
    public String getName(final int valueNumber) {
        if (!names.containsKey(valueNumber)) {
            return UNKNOWN;
        }
        final Collection<String> knownNamesForValue = names.get(valueNumber);
        // if (knownNamesForValue.size() > 1) {
        // logMoreThanOneNameFoundWarning(valueNumber);
        // }
        return knownNamesForValue.iterator().next();
    }

    // private void logMoreThanOneNameFoundWarning(final int valueNumber) {
    // final String msg =
    // String.format("%s:\n\tMore than one name for vale %d found: %s",
    // ir.getMethod()
    // .getSignature(), valueNumber, names);
    // log.warn(msg);
    // }
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

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
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

/**
 * The {@code VariableTable} is a helper class for resolving the names of a
 * method's values.
 * <p>
 * Note: This is not working properly right now - it cannot resolve every name.
 * This would require a better code analysis as we have right now. However, it
 * is at least a nice beginning.
 */
public class MethodVariableTable {
    public static final String STATIC = "<static>";

    public static final String UNKNOWN = "<unknown>";

    private final SSAInstruction.Visitor v = new SSAInstruction.Visitor() {
        @Override
        public void visitGet(final SSAGetInstruction instruction) {
            final String name = instruction.getDeclaredField().getName().toString();
            final int val = instruction.getDef();
            registerName(name, val);
        }

        @Override
        public void visitInvoke(final com.ibm.wala.ssa.SSAInvokeInstruction instruction) {
            final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instruction);
            final int lastInstructionIndex = bb.getLastInstructionIndex();
            if (instruction.hasDef()) {
                final int def = instruction.getDef();
                lookupNames(def, lastInstructionIndex);
            }
            if (!instruction.isStatic()) {
                final int receiver = instruction.getReceiver();
                lookupNames(receiver, lastInstructionIndex);
            }
        }

        @Override
        public void visitPhi(final SSAPhiInstruction instruction) {
            final int def = instruction.getDef();
            final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instruction);
            final int lastInstructionIndex = bb.getLastInstructionIndex();
            final String[] localNames = ir.getLocalNames(lastInstructionIndex, def);
            if (localNames != null && localNames.length > 0 && localNames[0] != null) {
                for (int i = instruction.getNumberOfUses(); i-- > 0;) {
                    index.put(instruction.getUse(i), localNames[0]);
                }
            }
        }

        @Override
        public void visitPut(final SSAPutInstruction instruction) {
            final String name = instruction.getDeclaredField().getName().toString();
            final int val = instruction.getVal();
            registerName(name, val);
        }
    };

    private final Multimap<Integer, String> index = HashMultimap.create();

    private final IR ir;

    public MethodVariableTable(final IR ir) {
        this.ir = ir;
        if (ir != null) {
            registerParameterNames();
            ir.visitAllInstructions(v);
        }
    }

    public String get(final int valueNumber) {
        final String valueName = lookupNameForValue(valueNumber);
        return valueName == null ? "<ambiguous>" : valueName;
    }

    public Set<Integer> getAllNamedValues() {
        return index.keySet();
    }

    public String getArgName(final SSAAbstractInvokeInstruction instr, final int argumentIndex) {
        final int argument = instr.getUse(argumentIndex);
        String name = lookupNameForValue(argument);
        if (name != null) {
            return name;
        }
        final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instr);
        final int lastInBb = bb.getLastInstructionIndex();
        final String[] localNames = ir.getLocalNames(lastInBb, argument);
        if (localNames == null) {
            // go through the instructions in this basic block and try to
            // resolve this...
            final SSAInstruction[] instructions = ir.getInstructions();
            for (int i = bb.getFirstInstructionIndex(); i < bb.getLastInstructionIndex(); i++) {
                if (instructions[i] != null) {
                    instructions[i].visit(v);
                }
            }
            name = lookupNameForValue(argument);
            return name == null ? UNKNOWN : name;
        }
        if (localNames.length == 0 || localNames[0] == null) {
            return UNKNOWN;
        }
        return localNames[0];
    }

    public String getDefName(final SSAAbstractInvokeInstruction instr) {
        final int receiver = instr.getDef();
        String name = lookupNameForValue(receiver);
        if (name != null) {
            return name;
        }
        final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instr);
        final int lastInBb = bb.getLastInstructionIndex();
        final String[] localNames = ir.getLocalNames(lastInBb, receiver);
        if (localNames == null) {
            // go through the instructions in this basic block and try to
            // resolve this...
            final SSAInstruction[] instructions = ir.getInstructions();
            for (int i = bb.getFirstInstructionIndex(); i < bb.getLastInstructionIndex(); i++) {
                if (instructions[i] != null) {
                    instructions[i].visit(v);
                }
            }
            name = lookupNameForValue(receiver);
            return name == null ? UNKNOWN : name;
        }
        if (localNames.length == 0 || localNames[0] == null) {
            return UNKNOWN;
        }
        return localNames[0];
    }

    public String getReceiverName(final SSAAbstractInvokeInstruction instr) {
        if (instr.isStatic()) {
            return STATIC;
        }
        final int receiver = instr.getReceiver();
        String name = lookupNameForValue(receiver);
        if (name != null) {
            return name;
        }
        final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instr);
        if (bb == null) {
            return UNKNOWN;
        }
        final int lastInBb = bb.getLastInstructionIndex();
        final String[] localNames = ir.getLocalNames(lastInBb, receiver);
        if (localNames == null) {
            // go through the instructions in this basic block and try to
            // resolve this...
            final SSAInstruction[] instructions = ir.getInstructions();
            for (int i = bb.getFirstInstructionIndex(); i < bb.getLastInstructionIndex(); i++) {
                if (instructions[i] != null) {
                    instructions[i].visit(v);
                }
            }
            name = lookupNameForValue(receiver);
            return name == null ? UNKNOWN : name;
        }
        if (localNames.length == 0 || localNames[0] == null) {
            return UNKNOWN;
        }
        return localNames[0];
    }

    private String lookupNameForValue(final int valueNumber) {
        final Collection<String> name = index.get(valueNumber);
        return name == null ? null : Iterables.getFirst(name, null);
    }

    private void registerParameterNames() {
        for (final int parameterValueNumber : ir.getParameterValueNumbers()) {
            lookupNames(parameterValueNumber, 0);
        }
    }

    private void lookupNames(final int valueNumber, final int bytecodeIndex) {
        final String[] localNames = ir.getLocalNames(bytecodeIndex, valueNumber);
        if (localNames != null && localNames.length > 0 && localNames[0] != null) {
            registerName(localNames[0], valueNumber);
        }
    }

    private void registerName(final String name, final int val) {
        index.put(val, name);
    }

    //
    // @Override
    // public void visitNew(final SSANewInstruction instruction)
    // {
    // registerNameWithInstanceKeys(instruction);
    // }
    //
    //
    //
    // private Set<InstanceKey> getInstanceKeysForDef(final SSAInstruction
    // instruction)
    // {
    // Assert.isTrue(instruction.hasDef(), "instruction has no def");
    // return heap.getInstanceKeys(node, instruction.getDef());
    // }
    //
    //
    //
    // private String[] getNamesForDef(final SSAInstruction instruction)
    // {
    // Assert.isTrue(instruction.hasDef(), "instruction has no def");
    // final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instruction);
    // final String[] localNames =
    // ir.getLocalNames(bb.getLastInstructionIndex(),
    // instruction.getDef());
    // return localNames == null ? NO_NAMES : localNames;
    // }
    // private void registerNameWithInstanceKeys(final SSANewInstruction
    // instruction)
    // {
    // final Set<InstanceKey> keys = getInstanceKeysForDef(instruction);
    // final String[] localNames = getNamesForDef(instruction);
    // //
    // for (final String localName : localNames)
    // {
    // for (final InstanceKey key : keys)
    // {
    // names.add(key, localName);
    // }
    // }
    // }
    @Override
    public String toString() {
        return String.format("MethodVariableTable [index=%s]", index);
    }

    public Collection<String> getNames() {
        return index.values();
    }
}

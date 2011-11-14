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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;

public class LocalNamesCollectorNaiveEdition {

    public static final String UNKNOWN = "unnamed";
    private final Multimap<Integer/* value number */, String> names = HashMultimap.create();
    private IR ir;

    public LocalNamesCollectorNaiveEdition(final IR ir) throws InvalidClassFileException {
        storeIR(ir);
        collectParameterNames();
        collectLocalValueNames();
    }

    private void storeIR(final IR ir) {
        this.ir = ir;
    }

    private void collectParameterNames() {
        for (final int parameterValueNumber : ir.getParameterValueNumbers()) {
            final String[] localNames = ir.getLocalNames(0, parameterValueNumber);
            storeLocalNamesForValueNumber(parameterValueNumber, localNames);
        }
    }

    private void storeLocalNamesForValueNumber(final int valueNumber, final String[] localNames) {
        if (localNames != null) {
            for (final String name : localNames) {
                storeLocalNameForValueNumber(valueNumber, name);
            }
        }
    }

    private boolean storeLocalNameForValueNumber(final int valueNumber, final String name) {
        if (name != null) {
            return names.put(valueNumber, name);
        }
        return false;
    }

    private void collectLocalValueNames() throws InvalidClassFileException {
        final SSAInstruction[] instructions = ir.getInstructions();
        for (int instructionIndex = 0; instructionIndex < instructions.length; instructionIndex++) {
            final SSAInstruction instruction = instructions[instructionIndex];
            if (isNullOrHasNoDef(instruction)) {
                continue;
            }
            final int defValueNumber = instruction.getDef();
            final int bytecodeIndex = ((IBytecodeMethod) ir.getMethod()).getBytecodeIndex(instructionIndex);
            final String[] irIndexlocalNames = ir.getLocalNames(instructionIndex, defValueNumber);
            final String[] bcIndexlocalNames = ir.getLocalNames(bytecodeIndex, defValueNumber);
            final String[] justForDebug = ir.getLocalNames(instructions.length - 1, defValueNumber);

            storeLocalNamesForValueNumber(defValueNumber, irIndexlocalNames);
        }
    }

    private boolean isNullOrHasNoDef(final SSAInstruction instruction) {
        return instruction == null || !instruction.hasDef();
    }

    public Collection<Integer> getValues() {
        return names.keySet();
    }

    public Collection<String> getNames() {
        return names.values();
    }
}

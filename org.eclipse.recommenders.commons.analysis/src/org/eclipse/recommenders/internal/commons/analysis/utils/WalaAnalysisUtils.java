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

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.newsites.NewSiteReferenceForMethodReturn;

import com.google.common.collect.Lists;
import com.ibm.wala.cfg.InducedCFG;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SyntheticIR;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

public class WalaAnalysisUtils {
    /**
     * Checks every get/put field instruction whether it accesses a (non-static)
     * field on this.
     */
    private static final class ThisFieldAccessVisitor extends Visitor {
        public boolean usesFields;

        @Override
        public void visitGet(final SSAGetInstruction instruction) {
            final int id = instruction.getRef();
            usesFields |= id == 1 && !instruction.isStatic();
        }

        @Override
        public void visitInvoke(final SSAInvokeInstruction instruction) {
            // check uses this?
            for (int i = instruction.getNumberOfUses(); i-- > 0;) {
                if (instruction.getUse(i) == 1) {
                    usesFields = true;
                }
            }
        }

        @Override
        public void visitPut(final SSAPutInstruction instruction) {
            for (int i = instruction.getNumberOfUses(); i-- > 0;) {
                if (instruction.getUse(i) == 1) {
                    usesFields = true;
                }
            }
        }
    }

    /**
     * Creates an almost empty method stub for the given method. It returns an
     * empty method body in the case of void-methods and a two-instructions IR
     * in the case that method returns something else like:
     * 
     * <pre>
     * ... &lt;T&gt; method(...)
     * {
     *      &lt;T&gt; local = new &lt;T&gt;; // note, this is not a constructor call! allocation only!
     *      return local;
     * }
     * </pre>
     * 
     * Note that no constructor call is used - just an allocation is used to
     * enforce WALA to create a new instance key.
     */
    public static IR createStub(final IMethod method) {
        final SSAInstructionFactory instructionFactory = Language.JAVA.instructionFactory();
        final TypeReference returnType = method.getReturnType();
        final int local = method.isStatic() ? method.getNumberOfParameters() + 1 : method.getNumberOfParameters() + 2;
        SSAInstruction[] res = null;
        if (returnType == TypeReference.Void) {
            res = new SSAInstruction[0];
        } else {
            final NewSiteReference site = NewSiteReference.make(1, returnType);
            res = new SSAInstruction[] {
                    returnType.isArrayType() ? instructionFactory.NewInstruction(local, site, new int[0])
                            : instructionFactory.NewInstruction(local, site),
                    // SSAInstructionFactory.InvokeInstruction(new int[]
                    // {
                    // local
                    // }, local + 1, makeFruitInit(ret)),
                    instructionFactory.ReturnInstruction(local, returnType.isPrimitiveType()), };
        }
        final SyntheticIR ir = new SyntheticIR(method, Everywhere.EVERYWHERE, new InducedCFG(res, method,
                Everywhere.EVERYWHERE), res, new SSAOptions(), new HashMap<Integer, ConstantValue>());
        return ir;
    }

    public static IMethod createStubMethod(final MethodReference ref, final IClass receiver) {
        final SSAInstructionFactory instructionFactory = Language.JAVA.instructionFactory();
        ensureIsNotNull(ref, "ref");
        ensureIsNotNull(receiver, "receiver");
        final IMethod target = receiver.getMethod(ref.getSelector());
        ensureIsNotNull(target);
        final MethodSummary sum = new MethodSummary(ref);
        sum.setStatic(target.isStatic());
        final TypeReference ret = ref.getReturnType();
        if (ret == TypeReference.Void) {
            sum.addStatement(instructionFactory.ReturnInstruction());
            return new SummarizedMethod(ref, sum, receiver);
        }
        final int local = target.isStatic() ? ref.getNumberOfParameters() + 1 : ref.getNumberOfParameters() + 2;
        //
        final NewSiteReference site = NewSiteReferenceForMethodReturn.create(target.isStatic() ? 1 : 2 /*
                                                                                                        * why
                                                                                                        * 2
                                                                                                        * ?
                                                                                                        * ?
                                                                                                        * ?
                                                                                                        */, ret,
                target.getReference());
        sum.addStatement(ret.isArrayType() ? instructionFactory.NewInstruction(local, site, new int[0])
                : instructionFactory.NewInstruction(local, site));
        // sum.addStatement(SSAInstructionFactory.InvokeInstruction(new
        // int[]
        // {
        // local
        // }, local + 1, makeFruitInit(ref.getReturnType())));
        sum.addStatement(instructionFactory.ReturnInstruction(local, ret.isPrimitiveType()));
        // } else
        // {
        // sum.addStatement(SSAInstructionFactory.ReturnInstruction());
        // }
        return new SummarizedMethod(ref, sum, receiver);
    }

    public static IClass getFieldType(final IField field) {
        final TypeReference fieldTypeReference = field.getFieldTypeReference();
        final IClass fieldType = field.getClassHierarchy().lookupClass(fieldTypeReference);
        return fieldType;
    }

    public static List<IClass> getFieldTypes(final Collection<IField> fields) {
        final List<IClass> res = Lists.newLinkedList();
        for (final IField field : fields) {
            final IClass fieldType = getFieldType(field);
            if (fieldType != null) {
                res.add(fieldType);
            }
        }
        return res;
    }

    public static int getInstrLineNumber(final CGNode callerNode, final SSAInvokeInstruction instruction) {
        return getLineNumber(callerNode.getMethod(), instruction);
    }

    /**
     * Returns all fields that are accessible from internal, i.e., from
     * {@code this}. This includes all private fields declared in the given
     * clazz and all non-private fields declared in the parents of clazz.
     */
    public static List<IField> getInternalAccessibleFields(final IClass clazz) throws ClassHierarchyException {
        final LinkedList<IField> accessibleFields = new LinkedList<IField>();
        for (final IField field : clazz.getAllFields()) {
            if (clazz == field.getDeclaringClass()) {
                accessibleFields.add(field);
            } else if (!field.isPrivate()) {
                accessibleFields.add(field);
            }
        }
        return accessibleFields;
    }

    public static int getLineNumber(final IMethod declaringMethod, final SSAAbstractInvokeInstruction instruction) {
        return declaringMethod.getLineNumber(instruction.getProgramCounter());
    }

    public static HashSet<String> getReceiverReferences(final CGNode callerNode, final SSAInvokeInstruction instruction) {
        return getReceiverReferences(callerNode.getIR(), instruction);
    }

    /**
     * Find local names for the given variable - if exist (anonymous calls for
     * instance using call chaining do not have a local name. Also constructor
     * calls do not have a local name.
     */
    public static HashSet<String> getReceiverReferences(final IR ir, final SSAAbstractInvokeInstruction instruction) {
        final HashSet<String> res = new HashSet<String>();
        final ISSABasicBlock bb = ir.getBasicBlockForInstruction(instruction);
        final String[] localReferences = ir.getLocalNames(bb.getLastInstructionIndex(), instruction.getReceiver());
        if (localReferences == null) {
            final SSAInstruction[] instructions = ir.getInstructions();
            final int receiver = instruction.getReceiver();
            String name = null;
            for (int i = 0; i < instructions.length; i++) {
                if (instructions[i] instanceof SSAPutInstruction) {
                    final SSAPutInstruction put = (SSAPutInstruction) instructions[i];
                    if (put.getRef() == receiver) {
                        name = put.getDeclaredField().getName().toString();
                        break;
                    }
                } else if (instructions[i] instanceof SSAGetInstruction) {
                    final SSAGetInstruction get = (SSAGetInstruction) instructions[i];
                    if (get.getDef() == receiver) {
                        name = get.getDeclaredField().getName().toString();
                        break;
                    }
                }
            }
            res.add(name);
        } else
        // localReferences != null
        {
            for (final String name : localReferences) {
                // names might be null, thus, check!
                if (name != null) {
                    res.add(name);
                }
            }
        }
        return res;
    }

    public static boolean isBypassSynteticClassReference(final TypeReference typeRef) {
        ensureIsNotNull(typeRef);
        final String fullyQualifiedReference = typeRef.getName().toString();
        return fullyQualifiedReference.startsWith("L$");
    }

    public static void isInternalCaller(final CGNode caller, final CallSiteReference site, final IClass receiver) {
    }

    /**
     * @param nestedClazz
     *            the class that might be nested
     * @param primaryClazz
     *            the class that might contain the declaration of nestedClass
     */
    public static boolean isNestedClass(final IClass nestedClazz, final IClass primaryClazz) {
        final String nestedReference = nestedClazz.getReference().toString();
        final String primaryReference = primaryClazz.getReference().toString();
        return nestedReference.startsWith(primaryReference + "$");
    }

    /**
     * Returns true if the given class belongs to the primordinal (determined by
     * checking the {@link ClassLoaderReference}.
     * 
     * @param clazz
     *            the class to check
     */
    public static boolean isPrimordial(final IClass clazz) {
        ensureIsNotNull(clazz, "clazz");
        return ClassLoaderReference.Primordial.equals(clazz.getClassLoader().getReference());
    }

    public static boolean isPrimordial(final TypeReference typeRef) {
        ensureIsNotNull(typeRef, "typeRef");
        return ClassLoaderReference.Primordial.equals(typeRef.getClassLoader());
    }

    public static boolean isStatic(final IClass definition) {
        return 0 != (definition.getModifiers() & Modifier.STATIC);
    }

    /**
     * Create the WALA TypeReference from a given Java class
     */
    public static com.ibm.wala.types.TypeName makeTypeReference(final Class<?> javaClazz) {
        return com.ibm.wala.types.TypeName.findOrCreate("L" + javaClazz.getCanonicalName().replaceAll("\\.", "/"));
    }

    public static Selector toSelector(final IMethodName method) {
        return Selector.make(method.getSignature());
    }

    /**
     * Returns {@code true} iff inside this IR no (non-static) fields of
     * {@code this} (i.e. the class definition that declared the method whos IR
     * we are examining) are accessed or a Reference on this is given as
     * argument to any other object.
     * 
     * @param ir
     * @return
     */
    public static boolean usesParametersOnly(final IR ir) {
        final ThisFieldAccessVisitor v = new ThisFieldAccessVisitor();
        ir.visitNormalInstructions(v);
        return !v.usesFields;
    }
}

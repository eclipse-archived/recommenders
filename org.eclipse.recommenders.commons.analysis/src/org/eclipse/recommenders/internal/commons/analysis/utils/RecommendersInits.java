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

import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.internal.commons.analysis.newsites.NewSiteReferenceForField;
import org.eclipse.recommenders.internal.commons.analysis.selectors.IFieldsSelector;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.shrikeBT.IInvokeInstruction.Dispatch;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

public class RecommendersInits {
    public static final String NAME_RECOMMENDERS_INIT = "<recommenders-init>";

    private static final SSAInstructionFactory factory = Language.JAVA.instructionFactory();

    public static boolean isRecommendersInit(final CGNode node) {
        final MethodReference reference = node.getMethod().getReference();
        return isRecommendersInit(reference);
    }

    public static boolean isRecommendersInit(final MethodReference methodRef) {
        return methodRef.getName().toString().equals(NAME_RECOMMENDERS_INIT);
    }

    // public static IMethod createRecommendersSmallInit(final IClass receiver, final MethodReference ref) {
    // final SSAInstructionFactory instructionFactory = Language.JAVA.instructionFactory();
    // final MethodSummary sum = new MethodSummary(ref);
    // if (receiver.isArrayClass()) {
    // return new SummarizedMethod(ref, sum, receiver);
    // }
    // int pc = 2;
    // int local = 0;
    // for (final IField f : getAllAccessibleFields(receiver)) {
    // local += 2;
    // final TypeReference fieldRef = f.getFieldTypeReference();
    // // final IClass fieldClazz =
    // // f.getClassHierarchy().lookupClass(f.getFieldTypeReference());
    // if (fieldRef.isArrayType()
    // // || fieldClazz == null ||isPrimordial(fieldClazz)
    // ) {
    // continue;
    // }
    // final NewSiteReference site = NewSiteReference.make(pc++, fieldRef);
    // final SSANewInstruction newInstruction = instructionFactory.NewInstruction(local, site);
    // final SSAPutInstruction putInstruction = instructionFactory.PutInstruction(1, local, f.getReference());
    // sum.addStatement(newInstruction);
    // if (f.getFieldTypeReference().isReferenceType()) {
    // // sum.addStatement(SSAInstructionFactory.InvokeInstruction(new
    // // int[]
    // // {
    // // local
    // // }, local + 1, makeFruitInit(f.getFieldTypeReference())));
    // }
    // sum.addStatement(putInstruction);
    // }
    // sum.addStatement(instructionFactory.ReturnInstruction());
    // return new SummarizedMethod(ref, sum, receiver);
    // }
    public static CallSiteReference makeRecommendersInit(final TypeReference type) {
        return CallSiteReference.make(0, MethodReference.findOrCreate(type, NAME_RECOMMENDERS_INIT, "()V"),
                Dispatch.SPECIAL);
    }

    // public static IMethod createRecommendersInitThatInitalizesEnclosingFieldReferencesToo(final CGNode caller,
    // final CallSiteReference call, final IClass receiverType, final SSAPropagationCallGraphBuilder builder) {
    // ensureIsNotNull(caller);
    // ensureIsNotNull(call);
    // ensureIsNotNull(receiverType);
    // ensureIsNotNull(builder);
    // if (receiverType.isArrayClass()) {
    // return createEmptyStubMethod(receiverType, call.getDeclaredTarget());
    // }
    // final MethodSummary sum = new MethodSummary(call.getDeclaredTarget());
    // int programCounter = 2;
    // int localValuePointer = 0;
    // for (final IField field : getAllAccessibleFields(receiverType)) {
    // final TypeReference fieldType = field.getFieldTypeReference();
    // //
    // // ignore primitives and array types
    // if (fieldType.isPrimitiveType() || fieldType.isArrayType()) {
    // continue;
    // }
    // //
    // localValuePointer += 2;
    // //
    // // create a new key for this field and add some pseudo new/put
    // // operations to the method stub
    // final Tuple<SSANewInstruction, SSAPutInstruction> instr = createKeyForField(programCounter++, field,
    // builder, caller, localValuePointer);
    // sum.addStatement(instr.getFirst());
    // sum.addStatement(instr.getSecond());
    // //
    // // is field.getDeclaringType a enclosingType, than add a
    // // recommenders-init for this instance too.
    // final IClass fieldTypeClass = ClassUtils
    // .findClass(field.getFieldTypeReference(), field.getClassHierarchy());
    // if (fieldTypeClass == null) {
    // continue;
    // }
    // if (ClassUtils.isNestedClass(receiverType, fieldTypeClass)) {
    // final CallSiteReference newRecommendersInitCall = makeRecommendersInit(field.getFieldTypeReference());
    // final int[] params = new int[] { localValuePointer };
    // sum.addStatement(factory.InvokeInstruction(params, localValuePointer + 1, newRecommendersInitCall));
    // }
    // }
    // sum.addStatement(factory.ReturnInstruction());
    // return new SummarizedMethod(call.getDeclaredTarget(), sum, receiverType);
    // // }
    // private static Collection<IField> getAllAccessibleFields(final IClass baseclass) {
    // final List<IField> accessibleFields = Lists.newLinkedList();
    // for (final IField field : baseclass.getAllFields()) {
    // if (field.getDeclaringClass() == baseclass) {
    // accessibleFields.add(field);
    // } else if (field.isProtected() || field.isPublic()) {
    // accessibleFields.add(field);
    // }
    // }
    // return accessibleFields;
    // }
    // public static IMethod createRecommendersInit2(final CGNode caller, final IClass receiver,
    // final MethodReference ref, final SSAPropagationCallGraphBuilder builder) {
    // final SSAInstructionFactory instructionFactory = Language.JAVA.instructionFactory();
    // final MethodSummary sum = new MethodSummary(ref);
    // if (receiver.isArrayClass()) {
    // return new SummarizedMethod(ref, sum, receiver);
    // }
    // int pc = 2;
    // int local = 0;
    // for (final IField f : receiver.getAllInstanceFields()) {
    // local += 2;
    // final TypeReference fieldRef = f.getFieldTypeReference();
    // // final IClass fieldClazz =
    // // f.getClassHierarchy().lookupClass(f.getFieldTypeReference());
    // if (fieldRef.isArrayType()
    // // || fieldClazz == null ||isPrimordial(fieldClazz)
    // ) {
    // continue;
    // }
    // //
    // // create a resolved new site instance
    // final NewSiteReference site = NewSiteReference.make(pc++, fieldRef);
    // builder.getInstanceKeyForAllocation(caller, site);
    // final SSANewInstruction newInstruction = instructionFactory.NewInstruction(local, site);
    // final SSAPutInstruction putInstruction = instructionFactory.PutInstruction(1, local, f.getReference());
    // sum.addStatement(newInstruction);
    // if (f.getFieldTypeReference().isReferenceType()
    // && !WalaAnalysisUtils.isPrimordial(f.getReference().getFieldType())) {
    // sum.addStatement(instructionFactory.InvokeInstruction(new int[] { local }, local + 1,
    // RecommendersInits.makeRecommendersInit(f.getFieldTypeReference())));
    // }
    // sum.addStatement(putInstruction);
    // }
    // sum.addStatement(instructionFactory.ReturnInstruction());
    // return new SummarizedMethod(ref, sum, receiver);
    // }
    public static IMethod createRecommendersInit(final CGNode caller, final CallSiteReference call,
            final IClass receiverType, final SSAPropagationCallGraphBuilder builder,
            final IFieldsSelector receiverFieldsSelector) {
        ensureIsNotNull(caller);
        ensureIsNotNull(call);
        ensureIsNotNull(receiverType);
        ensureIsNotNull(builder);
        ensureIsNotNull(receiverFieldsSelector);
        //
        if (receiverType.isArrayClass()) {
            return MethodUtils.createEmptyStubMethod(receiverType, call.getDeclaredTarget());
        }
        final MethodSummary sum = new MethodSummary(call.getDeclaredTarget());
        int programCounter = 2;
        int localValuePointer = 0;
        for (final IField field : receiverFieldsSelector.select(receiverType)) {
            localValuePointer += 2;
            //
            // create a new key for this field and add some pseudo new/put
            // operations to the method stub
            final Tuple<SSANewInstruction, SSAPutInstruction> instr = createKeyForField(programCounter++, field,
                    builder, caller, localValuePointer);
            //
            sum.addStatement(instr.getFirst());
            sum.addStatement(instr.getSecond());
            //
            // is field.getDeclaringType an enclosingType, than add a
            // recommenders-init for this instance too.
            if (isEnclosingClassField(field)) {
                final CallSiteReference newRecommendersInitCall = makeRecommendersInit(field.getFieldTypeReference());
                final int[] params = new int[] { localValuePointer };
                sum.addStatement(factory.InvokeInstruction(params, localValuePointer + 1, newRecommendersInitCall));
            }
        }
        sum.addStatement(factory.ReturnInstruction());
        return new SummarizedMethod(call.getDeclaredTarget(), sum, receiverType);
    }

    private static boolean isEnclosingClassField(final IField field) {
        final TypeReference fieldTypeReference = field.getFieldTypeReference();
        final IClassHierarchy cha = field.getClassHierarchy();
        final IClass fieldTypeClass = ClassUtils.findClass(fieldTypeReference, cha);
        if (fieldTypeClass == null) {
            return false;
        }
        return ClassUtils.isNestedClass(field.getDeclaringClass(), fieldTypeClass);
    }

    private static Tuple<SSANewInstruction, SSAPutInstruction> createKeyForField(final int programCounter,
            final IField field, final PropagationCallGraphBuilder builder, final CGNode caller,
            final int localValuePointer) {
        final TypeReference declaredType = field.getFieldTypeReference();
        final NewSiteReference newSite = NewSiteReferenceForField.create(programCounter, declaredType,
                field.getReference());
        builder.getInstanceKeyForAllocation(caller, newSite);
        //
        final SSANewInstruction newInstruction = factory.NewInstruction(localValuePointer, newSite);
        final SSAPutInstruction putInstruction = factory.PutInstruction(1, localValuePointer, field.getReference());
        //
        return Tuple.create(newInstruction, putInstruction);
    }
}

/**
 * Copyright (c) 2010 Gary Fritz, and Andreas Kaluza.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gary Fritz - initial API and implementation.
 *    Andreas Kaluza - modified implementation to use WALA
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal.ChainedProposalAnchor;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal.ChainingAlgorithmWorker;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal.FieldChainWalaElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal.FieldsAndMethodsCompletionContext;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal.MethodChainWalaElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.util.LookupUtilJdt;
import org.eclipse.recommenders.rcp.wala.IClassHierarchyService;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;

@SuppressWarnings("restriction")
public class ChainingAlgorithm {

  private IClass expectedType;

  private final List<ChainedJavaProposal> proposals;

  private ThreadPoolExecutor executor;

  private final IClassHierarchyService walaService;

  private FieldsAndMethodsCompletionContext ctx;

  private static Map<IClass, Map<IMember, IClass>> searchMap;

  private IClass callingContext;

  public ChainingAlgorithm() {
    expectedType = null;

    proposals = Collections.synchronizedList(new LinkedList<ChainedJavaProposal>());
    walaService = InjectionService.getInstance().requestInstance(IClassHierarchyService.class);
    searchMap = Collections.synchronizedMap(new HashMap<IClass, Map<IMember, IClass>>());
  }

  public void execute(final JavaContentAssistInvocationContext jctx) throws JavaModelException {

    callingContext = getCallingContext(jctx);

    if (!isValidComputationContext(jctx))
      return;

    initThreadPool();

    processMembers();

    waitForThreadPoolTermination();
  }

  private IClass getCallingContext(final JavaContentAssistInvocationContext jctx) throws JavaModelException {
    ctx = new FieldsAndMethodsCompletionContext(jctx);

    final IType callingContextType = ctx.getCallingContext();
    return walaService.getType(callingContextType);
  }

  private boolean isValidComputationContext(final JavaContentAssistInvocationContext jctx) {

    if (callingContext == null)
      return false;

    final char[] expectedTypeSignature = ctx.getExpectedTypeSignature();
    // REVIEW: what do you actually check here? failed to resolve?,
    // void?,primitive? no clue. --> if 'my' compiler does not find out what we
    // are searching for, the algorithm should terminate
    // you said the next 4 lines would be merged it into an util class
    if ((expectedTypeSignature == null) || (expectedTypeSignature.length == 0))
      return false;

    // REVIEW: use isSimpleType(expSignature) Maybe use of ItypeName?
    if (LookupUtilJdt.isSignatureOfSimpleType(new String(expectedTypeSignature)))
      return false;

    expectedType = walaService.getType(ctx.getExpectedType());

    return expectedType != null;
  }

  private void initThreadPool() {

    executor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(),
        Constants.AlgorithmSettings.WORKER_KEEP_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS,
        new PriorityBlockingQueue<Runnable>(11, new Comparator<Runnable>() {

          // sort the workers according to their priority
          @Override
          public int compare(final Runnable o1, final Runnable o2) {
            return ((ChainingAlgorithmWorker) o1).getPriority() - ((ChainingAlgorithmWorker) o2).getPriority();
          }
        }));
    executor.allowCoreThreadTimeOut(true);
  }

  private void processMembers() throws JavaModelException {
    processInitialFields();
    processLocalVariables();
    processMethods();
  }

  private void waitForThreadPoolTermination() {
    try {
      executor.awaitTermination(Constants.AlgorithmSettings.EXECUTOR_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      JavaPlugin.log(e);
    }
  }

  private void processMethods() throws JavaModelException {
    for (final ChainedProposalAnchor methodProposal : ctx.getProposedMethods()) {
      final TypeName returnType = getMethodReturnTypeName(methodProposal);
      if (returnType != null) {
        startMethodWorkerFromMethodProposal(methodProposal, returnType);
      }
    }
  }

  private void startMethodWorkerFromMethodProposal(final ChainedProposalAnchor methodProposal, final TypeName returnType) {
    final LinkedList<IChainWalaElement> proposalElementList = new LinkedList<IChainWalaElement>();
    for (final IMethod method : callingContext.getAllMethods()) {
      if (isValidMethod(methodProposal, returnType, method)) {
        proposalElementList.add(new MethodChainWalaElement(method));
        executor.execute(new ChainingAlgorithmWorker(proposalElementList, 0, this, executor, expectedType));
        break;
      }
    }
  }

  private boolean isValidMethod(final ChainedProposalAnchor methodProposal, final TypeName fieldType,
      final IMethod method) {

    final int thisParameterInParameterTypes = computeParameterInParameterType(method);
    TypeName returnReference = method.getReturnType().getName();

    boolean sameReference = returnReference.equals(fieldType);
    boolean sameMethodName = method.getName().toString().equals(methodProposal.getCompletion());
    boolean sameParameterCount = (method.getNumberOfParameters() - thisParameterInParameterTypes == methodProposal
        .getParameterNames().length);
    boolean isPrimitive = method.getReturnType().isPrimitiveType();

    return sameReference && sameMethodName && sameParameterCount && !isPrimitive;
  }

  private int computeParameterInParameterType(final IMethod method) {
    int thisParameterInParameterTypes = 1;
    if (method.isStatic()) {
      thisParameterInParameterTypes = 0;
    }
    return thisParameterInParameterTypes;
  }

  private TypeName getMethodReturnTypeName(final ChainedProposalAnchor methodProposal) throws JavaModelException {
    final char[] signature = methodProposal.getSignature();
    final IType fullyQualifiedType = LookupUtilJdt.lookupType(signature);
    if ((fullyQualifiedType == null) || !LookupUtilJdt.isWantedType(fullyQualifiedType))
      return null;
    if (!isPrimitive(signature, fullyQualifiedType)) {
      TypeName fieldType = walaService.getType(fullyQualifiedType).getName();
      return fieldType;
    } else
      return null;
  }

  private void processLocalVariables() throws JavaModelException {
    for (final ChainedProposalAnchor variableProposal : ctx.getProposedVariables()) {
      if (isValidLocalVariable(ctx, variableProposal)) {
        startWorkerForVariableProposal(variableProposal);
      }
    }
  }

  private void startWorkerForVariableProposal(final ChainedProposalAnchor variableProposal) throws JavaModelException {
    final char signature[] = variableProposal.getSignature();
    final IType fullyQualifiedType = LookupUtilJdt.lookupType(signature);
    if (!isPrimitive(signature, fullyQualifiedType)) {
      final LinkedList<IChainWalaElement> proposalElementList = new LinkedList<IChainWalaElement>();
      proposalElementList.add(createFieldChainElement(variableProposal, fullyQualifiedType));
      executor.execute(new ChainingAlgorithmWorker(proposalElementList, 0, this, executor, expectedType));
    }
  }

  private FieldChainWalaElement createFieldChainElement(final ChainedProposalAnchor variableProposal,
      final IType fullyQualifiedType) {

    String variableCompletion = variableProposal.getCompletion();
    String variableSigniture = new String(variableProposal.getSignature());

    IClass type = walaService.getType(fullyQualifiedType);
    IClassHierarchy classHierarchy = type.getClassHierarchy();
    ClassLoaderReference classLoaderReference = type.getClassLoader().getReference();

    return new FieldChainWalaElement(variableCompletion, variableSigniture, classHierarchy, classLoaderReference);
  }

  // REVIEW: can || (fullyQualifiedType instanceof LookupUtilJdt.PrimitiveType)
  // happen? Primitives are well defined, right? --> right, but without
  // testcases I don't wanna remove it ;)
  private boolean isPrimitive(final char[] signature, final IType fullyQualifiedType) {
    return LookupUtilJdt.isSignatureOfSimpleType(new String(signature))
        || (fullyQualifiedType instanceof LookupUtilJdt.PrimitiveType);
  }

  private boolean isValidLocalVariable(final FieldsAndMethodsCompletionContext ctx,
      final ChainedProposalAnchor variableProposal) {
    boolean isOfExpectedType = Arrays.equals(variableProposal.getSignature(), ctx.getExpectedTypeSignature());
    boolean isSameLocalVariableName = Arrays.equals(variableProposal.getCompletion().toCharArray(),
        ctx.getCallingVariableName());

    return isOfExpectedType && !isSameLocalVariableName;
  }

  private void processInitialFields() throws JavaModelException {
    for (final ChainedProposalAnchor fieldProposal : ctx.getProposedFields()) {
      final char signature[] = fieldProposal.getSignature();
      final IType fullyQualifiedType = LookupUtilJdt.lookupType(signature);
      if (!isPrimitive(signature, fullyQualifiedType)) {
        TypeName fieldType = walaService.getType(fullyQualifiedType).getName();
        startWorkerForFieldProposal(fieldProposal, fieldType);
      }
    }
  }

  private void startWorkerForFieldProposal(final ChainedProposalAnchor fieldProposal, final TypeName fieldType) {
    final LinkedList<IChainWalaElement> proposalElementList = new LinkedList<IChainWalaElement>();
    for (final IField field : callingContext.getAllFields()) {
      TypeName fieldReference = null;
      if (!field.getFieldTypeReference().isPrimitiveType()) {
        fieldReference = field.getFieldTypeReference().getName();
        if (fieldReference.equals(fieldType) && field.getName().toString().equals(fieldProposal.getCompletion())) {
          proposalElementList.add(new FieldChainWalaElement(field));
          executor.execute(new ChainingAlgorithmWorker(proposalElementList, 0, this, executor, expectedType));
          break;
        }
      }
    }
  }

  public void addCastedProposal(final LinkedList<IChainWalaElement> workingChain, final IClass expectedType) {
    synchronized (proposals) {
      proposals.add(new ChainedJavaProposal(workingChain, expectedType));
    }
  }

  public void addProposal(final LinkedList<IChainWalaElement> workingChain) {
    synchronized (proposals) {
      proposals.add(new ChainedJavaProposal(workingChain));
    }
  }

  public IClass getExpectedType() {
    return expectedType;
  }

  public List<ChainedJavaProposal> getProposals() {
    return proposals;
  }

  public static void setSearchMap(Map<IClass, Map<IMember, IClass>> searchMap) {
    ChainingAlgorithm.searchMap = searchMap;
  }

  public static Map<IClass, Map<IMember, IClass>> getSearchMap() {
    return searchMap;
  }
}

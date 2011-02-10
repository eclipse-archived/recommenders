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
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.wala.IClassHierarchyService;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeName;

@SuppressWarnings("restriction")
public class ChainingAlgorithm {

  private IClass expectedType;

  private final List<ChainedJavaProposal> proposals;

  private ThreadPoolExecutor executor;

  private final IClassHierarchyService walaService;

  private final JavaElementResolver resolver;

  private FieldsAndMethodsCompletionContext ctx;
  
  private static Map<IClass, Map<IMember, IClass>> searchMap;

  public ChainingAlgorithm() {
    expectedType = null;
    // REVIEW: unclear why sychronized. init in field + comment/explanation? -->
    // I fill this list by several threads, so synchronization is needed.
    proposals = Collections.synchronizedList(new LinkedList<ChainedJavaProposal>());

    walaService = InjectionService.getInstance().requestInstance(IClassHierarchyService.class);
    resolver = InjectionService.getInstance().requestInstance(JavaElementResolver.class);
    searchMap = Collections.synchronizedMap(new HashMap<IClass, Map<IMember, IClass>>());
  }

  public void execute(final JavaContentAssistInvocationContext jctx) throws JavaModelException {

    final IClass callingContext = getCallingContext(jctx);

    if (!isValidComputationContext(jctx, ctx, callingContext))
      return;

    initThreadPool();

    startWorkers(callingContext);

    waitForThreadPoolTermination();
  }

  private IClass getCallingContext(final JavaContentAssistInvocationContext jctx) throws JavaModelException {
    ctx = new FieldsAndMethodsCompletionContext(jctx);

    final IType callingContextType = ctx.getCallingContext();
    return walaService.getType(callingContextType);
  }

  private boolean isValidComputationContext(final JavaContentAssistInvocationContext jctx,
      final FieldsAndMethodsCompletionContext ctx, final IClass callingContext) {
    //
    // Marcel: minimal HACK to terminate computation if completion is unlikely
    // to find anything meaningful, i.e.,
    // use it on this or an any other qualified name but nowhere else.
    // You should have a closer look on this to improve the code further.

    // Andreas Kaluza: Sry this HACK does not work. If you have for example a
    // member PlattformUI findMe; and you trigger this completion in a method
    // within the fragment IWorkbenchHelpSystem d = find<^+Space>, than you get
    // no proposals. The proposal should be 'findMe'. I commented this HACK out.

    /*
     * final IntelligentCompletionContext context = new
     * IntelligentCompletionContext(jctx, resolver); if
     * (!context.isReceiverImplicitThis() && !(context.getCompletionNode()
     * instanceof CompletionOnQualifiedNameReference)) return false;
     */
    // HACK ended

    if (callingContext == null)
      return false;

    final char[] expectedTypeSignature = ctx.getExpectedTypeSignature();
    // REVIEW: what do you actually check here? failed to resolve?,
    // void?,primitive? no clue.
    if ((expectedTypeSignature == null) || (expectedTypeSignature.length == 0))
      return false;

    // REVIEW: use isSimpleType(expSignature) Maybe use of ItypeName?
    if (LookupUtilJdt.isSignatureOfSimpleType(new String(expectedTypeSignature)))
      return false;

    expectedType = walaService.getType(ctx.getExpectedType());

    // }
    // REVIEW: unclear why this could happen. What is the meaning of ==null? use
    // a well named boolean variable or (preferred) method
    if (expectedType == null)
      return false;

    return true;
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
    //executor.prestartAllCoreThreads();

    ChainingAlgorithmWorker.setExecutor(executor);
    ChainingAlgorithmWorker.setExpectedType(expectedType);
  }

  private void startWorkers(final IClass callingContext) throws JavaModelException {
    processInitialFields(callingContext);
    processLocalVariables();
    processMethodsReturnType(callingContext);
  }

  private void waitForThreadPoolTermination() {
    try {
      executor.awaitTermination(Constants.AlgorithmSettings.EXECUTOR_ALIVE_TIME_IN_MS, TimeUnit.MILLISECONDS);
    } catch (final InterruptedException e) {
      JavaPlugin.log(e);
    }
  }

  private void processMethodsReturnType(
      final IClass callingContext) throws JavaModelException {

    // REVIEW String parsing should go to private methods or utility classes.
    // use
    // speaking names...
    // REVIEW consider using NULL-Objects
    for (final ChainedProposalAnchor methodProposal : ctx.getProposedMethods()) {
      final TypeName fieldType = getTypeName(methodProposal);
      if (fieldType == null) {
        continue;
      }
      startMethodWorkerFromMethodProposal(callingContext, methodProposal, fieldType);
    }
  }

  private void startMethodWorkerFromMethodProposal(final IClass callingContext,
      final ChainedProposalAnchor methodProposal, final TypeName fieldType) {
    final LinkedList<IChainWalaElement> walaList = new LinkedList<IChainWalaElement>();
    for (final IMethod method : callingContext.getAllMethods()) {
      TypeName returnReference = null;
      if (!method.getReturnType().isPrimitiveType()) {
        returnReference = method.getReturnType().getName();
        final int thisParameterInParameterTypes = computeParameterInParameterType(method);
        if (isValidMethod(methodProposal, fieldType, method, returnReference, thisParameterInParameterTypes)) {
          walaList.add(new MethodChainWalaElement(method));
          executor.execute(new ChainingAlgorithmWorker(walaList, 0, this));
          break;
        }
      }
    }
  }

  private boolean isValidMethod(final ChainedProposalAnchor methodProposal, final TypeName fieldType,
      final IMethod method, final TypeName returnReference, final int thisParameterInParameterTypes) {
    return returnReference.equals(fieldType)
        && method.getName().toString().equals(methodProposal.getCompletion())
        && (method.getNumberOfParameters() - thisParameterInParameterTypes == methodProposal.getParameterNames().length);
  }

  private int computeParameterInParameterType(final IMethod method) {
    int thisParameterInParameterTypes = 1;
    if (method.isStatic()) {
      thisParameterInParameterTypes = 0;
    }
    return thisParameterInParameterTypes;
  }

  private TypeName getTypeName(final ChainedProposalAnchor methodProposal) throws JavaModelException {
    final char[] signature = methodProposal.getSignature();
    final IType fullyQualifiedType = LookupUtilJdt.lookupType(signature);
    if ((fullyQualifiedType == null) || !LookupUtilJdt.isWantedType(fullyQualifiedType))
      return null;
    TypeName fieldType = null;
    if (!isPrimitive(signature, fullyQualifiedType)) {
      fieldType = walaService.getType(fullyQualifiedType).getName();
    } else
      return null;
    return fieldType;
  }

  private void processLocalVariables()
      throws JavaModelException {
    for (final ChainedProposalAnchor variableProposal : ctx.getProposedVariables()) {
      if (isValidLocalVariable(ctx, variableProposal)) {
        startWorkerForVariableProposal(variableProposal);
      }
    }
  }

  private void startWorkerForVariableProposal(final ChainedProposalAnchor variableProposal) throws JavaModelException {
    final char signature[] = variableProposal.getSignature();
    final IType fullyQualifiedType = LookupUtilJdt.lookupType(signature);
    final LinkedList<IChainWalaElement> walaList = new LinkedList<IChainWalaElement>();
    if (!isPrimitive(signature, fullyQualifiedType)) {
      walaList.add(new FieldChainWalaElement(variableProposal.getCompletion(), new String(variableProposal
          .getSignature()), walaService.getType(fullyQualifiedType).getClassHierarchy(), walaService
          .getType(fullyQualifiedType).getClassLoader().getReference()));
      executor.execute(new ChainingAlgorithmWorker(walaList, 0, this));
    }
  }

  private boolean isPrimitive(final char[] signature, final IType fullyQualifiedType) {
    return LookupUtilJdt.isSignatureOfSimpleType(new String(signature))
        || (fullyQualifiedType instanceof LookupUtilJdt.PrimitiveType);
  }

  private boolean isValidLocalVariable(final FieldsAndMethodsCompletionContext ctx,
      final ChainedProposalAnchor variableProposal) {
    return Arrays.equals(variableProposal.getSignature(), ctx.getExpectedTypeSignature())
        && !Arrays.equals(variableProposal.getCompletion().toCharArray(), ctx.getCallingVariableName());
  }

  private void processInitialFields(final IClass callingContext)
      throws JavaModelException {
    for (final ChainedProposalAnchor fieldProposal : ctx.getProposedFields()) {
      final char signature[] = fieldProposal.getSignature();
      final IType fullyQualifiedType = LookupUtilJdt.lookupType(signature);
      final LinkedList<IChainWalaElement> walaList = new LinkedList<IChainWalaElement>();
      TypeName fieldType = null;
      if (!isPrimitive(signature, fullyQualifiedType)) {
        fieldType = walaService.getType(fullyQualifiedType).getName();
        startWorkerForFieldProposal(callingContext, fieldProposal, walaList, fieldType);
      }
    }
  }

  private void startWorkerForFieldProposal(final IClass callingContext, final ChainedProposalAnchor fieldProposal,
      final LinkedList<IChainWalaElement> walaList, final TypeName fieldType) {
    for (final IField field : callingContext.getAllFields()) {
      TypeName fieldReference = null;
      if (!field.getFieldTypeReference().isPrimitiveType()) {
        fieldReference = field.getFieldTypeReference().getName();
        if (fieldReference.equals(fieldType) && field.getName().toString().equals(fieldProposal.getCompletion())) {
          walaList.add(new FieldChainWalaElement(field));
          executor.execute(new ChainingAlgorithmWorker(walaList, 0, this));
          break;
        }
      }
    }
  }

  public void addCastedProposal(final LinkedList<IChainWalaElement> workingChain, final IClass expectedType) {
    // Collections.reverse(workingChain);
    synchronized (proposals) {
      proposals.add(new ChainedJavaProposal(workingChain, expectedType));
    }
  }

  public void addProposal(final LinkedList<IChainWalaElement> workingChain) {
    // Collections.reverse(workingChain);
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

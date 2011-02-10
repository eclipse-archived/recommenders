package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.Constants;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.ChainingAlgorithm;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainWalaElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.util.LookupUtilWala;

import com.ibm.wala.classLoader.ArrayClass;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeReference;

@SuppressWarnings("restriction")
public class ChainingAlgorithmWorker implements Runnable {

  private static ThreadPoolExecutor executor;

  private final LinkedList<IChainWalaElement> workingChain;

  private final int priority;

  private static IClass expectedType;

  private final ChainingAlgorithm internalProposalStore;

  public ChainingAlgorithmWorker(final LinkedList<IChainWalaElement> workingChain, final int priority,
      final ChainingAlgorithm internalProposalStore) {
    this.workingChain = workingChain;
    this.priority = priority;
    this.internalProposalStore = internalProposalStore;
  }

  private void inspectType() throws JavaModelException {
    final IClass typeToCheck = workingChain.getLast().getType();
    // check type if searched type --> store for proposal
    if (storeForProposal(typeToCheck)) {
      tryTerminateExecutor();
      return;
    }
    // check if max chain length reached
    if (getPriority() + 1 < Constants.AlgorithmSettings.MAX_CHAIN_DEPTH) {
      // check if type was already computed
      if (ChainingAlgorithm.getSearchMap().containsKey(typeToCheck)) {
        executeComputationOnExistingType(typeToCheck);
        tryTerminateExecutor();
        return;
      }
      // check fields of type --> store in search map && and create new worker
      processCheckingAndStoring(typeToCheck);
    }
    tryTerminateExecutor();
  }

  private void processCheckingAndStoring(final IClass typeToCheck) throws JavaModelException {
    if (!typeToCheck.isArrayClass()) {
      final Map<IMember, IClass> map = computeFields(typeToCheck);
      // check methods of type --> store in search map && and create new
      // worker
      map.putAll(computeMethods(typeToCheck));
      ChainingAlgorithm.getSearchMap().put(typeToCheck, map);
    } else {
      handleArrayType(typeToCheck);
    }
  }

  private void handleArrayType(final IClass typeToCheck) throws JavaModelException {
    final ArrayClass arrayTypeToCheck = (ArrayClass) typeToCheck;
    final IClass classOfArray = arrayTypeToCheck.getElementClass();
    if (classOfArray == null) {
      final TypeReference elementType = arrayTypeToCheck.getReference().getArrayElementType();
      if (elementType.isPrimitiveType())
        return;
    }
    final Map<IMember, IClass> map = computeFields(classOfArray);
    // check methods of type --> store in search map && and create new
    // worker
    map.putAll(computeMethods(classOfArray));
    ChainingAlgorithm.getSearchMap().put(typeToCheck, map);
  }

  /*
   * computes all methods from type to check
   */
  private Map<IMember, IClass> computeMethods(final IClass typeToCheck) throws JavaModelException {
    final Map<IMember, IClass> map1 = new HashMap<IMember, IClass>();
    for (final IMethod m : typeToCheck.getAllMethods()) {
      final IClass result = createMethodWorker(m);
      if (result != null) {
        map1.put(m, result);
      }
    }
    return map1;
  }

  /*
   * computes all fields from type to check
   */
  private Map<IMember, IClass> computeFields(final IClass typeToCheck) throws JavaModelException {
    final Map<IMember, IClass> map = new HashMap<IMember, IClass>();
    for (final IField f : typeToCheck.getAllFields()) {
      final IClass result = createFieldWorker(typeToCheck, f);
      if (result != null) {
        map.put(f, result);
      }
    }
    return map;
  }

  /*
   * when ever one chain is finished the terminator should be tried to be
   * terminated. The reason is, that the terminator only exits, if the max. run
   * time is reached. But we want to have the optimal run time.
   */
  private void tryTerminateExecutor() {
    if ((ChainingAlgorithmWorker.getExecutor().getPoolSize() == 1)
        && (ChainingAlgorithmWorker.getExecutor().getQueue().size() < 1)) {
      ChainingAlgorithmWorker.getExecutor().shutdownNow(); //XXX There is a problem with the executer which I cannot resolve. If you trigger the plug-in several times in a very short period, than a InterruptedException can occur 
    }
  }

  /*
   * if the chain has to be extended by a method
   */
  private IClass createMethodWorker(final IMethod m) throws JavaModelException {
    if (m.isPublic()) {//XXX: Case: If calling context is subtype of typeToCheck than field/method can be protected or package private
      final LinkedList<IChainWalaElement> list = new LinkedList<IChainWalaElement>(workingChain);
      list.add(new MethodChainWalaElement(m));
      final ChainingAlgorithmWorker worker = new ChainingAlgorithmWorker(list, getPriority() + 1, internalProposalStore);
      startWorker(worker);
      if (m.getReturnType().isPrimitiveType())
        return null;// return
                    // ChainingAlgorithm.boxPrimitiveTyp(m.getDeclaringClass(),
      // m.getReturnType().getName().toString().toCharArray());
      return m.getClassHierarchy().lookupClass(m.getReturnType());
    }
    return null;
  }

  /*
   * put worker to the executor, so that it can be processed
   */
  private void startWorker(final ChainingAlgorithmWorker worker) {
    if (ChainingAlgorithmWorker.getExecutor().getKeepAliveTime(TimeUnit.MILLISECONDS) <= Constants.AlgorithmSettings.WORKER_KEEP_ALIVE_TIME_IN_MS) {
      try {
        ChainingAlgorithmWorker.getExecutor().execute(worker);
      } catch (final RejectedExecutionException e) {
        JavaPlugin.log(e);
      }
    }
  }

  /*
   * if the chain has to be extended by a field
   */
  private IClass createFieldWorker(final IClass typeToCheck, final IField f) throws JavaModelException {
    if (f.isPublic()) { //XXX: Case: If calling context is subtype of typeToCheck than field/method can be protected or package private
      final LinkedList<IChainWalaElement> list = new LinkedList<IChainWalaElement>(workingChain);
      list.add(new FieldChainWalaElement(f));
      final ChainingAlgorithmWorker worker = new ChainingAlgorithmWorker(list, getPriority() + 1, internalProposalStore);
      startWorker(worker);
      if (f.getFieldTypeReference().isPrimitiveType()){
        return null;
      }
      return f.getClassHierarchy().lookupClass(f.getFieldTypeReference());
    }
    return null;
  }

  /*
   * if a type was found in the internal store, create either a method or field
   * worker
   */
  private void executeComputationOnExistingType(final IClass typeToCheck) {
    for (final Entry<IMember, IClass> entry : ChainingAlgorithm.getSearchMap().get(typeToCheck).entrySet()) {
      final LinkedList<IChainWalaElement> list = new LinkedList<IChainWalaElement>(workingChain);
      if (entry.getKey() instanceof IField) {
        list.add(new FieldChainWalaElement((IField) entry.getKey()));
      } else {
        list.add(new MethodChainWalaElement((IMethod) entry.getKey()));
      }
      final ChainingAlgorithmWorker worker = new ChainingAlgorithmWorker(list, getPriority() + 1, internalProposalStore);
      startWorker(worker);
    }
  }

  @Override
  public void run() {
    try {
      inspectType();
    } catch (final JavaModelException e) {
      JavaPlugin.log(e);
    }
  }

  /*
   * function to check if the call chain meets the expectations, so that it can
   * be processed for proposal computation
   */
  private boolean storeForProposal(final IClass typeToCheck) throws JavaModelException {
    if (typeToCheck == null)
      return true;
    final int testResult = LookupUtilWala.equalityTest(typeToCheck, ChainingAlgorithmWorker.getExpectedType());
    //if both types equal
    if ((testResult & LookupUtilWala.RESULT_EQUAL) > 0) {
      if (!checkRedundancy()) {
        internalProposalStore.addProposal(workingChain);
        return false;
      } else
        return true;
    }
    //if typeToCheck is primitive return
    if ((testResult & LookupUtilWala.RESULT_PRIMITIVE) > 0)
      return true;
    // Consult type hierarchy for sub-/supertypes
    if (LookupUtilWala.isSubtype(typeToCheck, ChainingAlgorithmWorker.getExpectedType())
        && !((testResult & LookupUtilWala.RESULT_EQUAL) > 0)) {
      if (!checkRedundancy()) {
        internalProposalStore.addCastedProposal(workingChain, ChainingAlgorithmWorker.getExpectedType());
        return false;
      } else
        return true;
    }
    /* else */
    if (LookupUtilWala.isSupertype(typeToCheck, ChainingAlgorithmWorker.getExpectedType())
        && !((testResult & LookupUtilWala.RESULT_EQUAL) > 0)) {
      if (!checkRedundancy()) {
        internalProposalStore.addProposal(workingChain);
        return false;
      } else
        return true;
    }
    // not equal, not in a hierarchical relation, not primitive
    return false;
  }

  /*
   * wie want to avoid: method1().method1().method1()... this function handles
   * this case
   */
  private boolean checkRedundancy() {
    final int size = workingChain.size();
    if (size >= 2) {
      final IChainWalaElement last = workingChain.get(size - 1);
      final IChainWalaElement nextLast = workingChain.get(size - 2);
      if (last.getCompletion().equals(nextLast.getCompletion()))
        return true;
    }
    return false;
  }

  public int getPriority() {
    return priority;
  }

  public static void setExecutor(final ThreadPoolExecutor executor) {
    ChainingAlgorithmWorker.executor = executor;
  }

  public static ThreadPoolExecutor getExecutor() {
    return ChainingAlgorithmWorker.executor;
  }

  public static void setExpectedType(final IClass expectedType) {
    ChainingAlgorithmWorker.expectedType = expectedType;
  }

  public static IClass getExpectedType() {
    return ChainingAlgorithmWorker.expectedType;
  }
}

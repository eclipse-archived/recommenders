package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.CompilerBindings;
import org.eclipse.recommenders.internal.rcp.codecompletion.IntelligentCompletionContext;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.wala.IClassHierarchyService;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;

@SuppressWarnings("restriction")
public class ChainCompletionContext {

  private final IntelligentCompletionContext ctx;
  private final List<IChainElement> accessibleFields = Lists.newLinkedList();
  private final List<IChainElement> accessibleMethods = Lists.newLinkedList();
  private final List<IChainElement> accessibleLocals = Lists.newLinkedList();
  private final JavaElementResolver javaElementResolver;
  private final IClassHierarchyService walaChaService;
  private IClass receiverType;
  private IClass expectedType;
  private IClass enclosingType;

  public ChainCompletionContext(final IntelligentCompletionContext ctx, final JavaElementResolver javaElementResolver,
      final IClassHierarchyService walaChaService) {
    this.ctx = ctx;
    this.javaElementResolver = javaElementResolver;
    this.walaChaService = walaChaService;
    initializeAccessibleElements();
  }

  private void initializeAccessibleElements() {
    if (!findEnclosingClass()) {
      return;
    }
    if (!findReceiverClass()) {
      return;
    }
    if (!findExpectedClass()) {
      return;
    }
    computeAccessibleFields();
    computeAccessibleMethods();
    computeAccessibleLocals();
  }

  private boolean findEnclosingClass() {
    final ITypeName name = ctx.getEnclosingType();
    enclosingType = toWalaClass(name);
    return enclosingType != null;
  }

  private boolean findExpectedClass() {
    final ITypeName expectedTypeName = ctx.getExpectedType();
    expectedType = toWalaClass(expectedTypeName);
    return expectedType != null;
  }

  private IClass toWalaClass(final ITypeName typeName) {
    ensureIsNotNull(typeName);
    final IType jdtType = javaElementResolver.toJdtType(typeName);
    if (jdtType == null) {
      return null;
    }
    final IClass walaType = walaChaService.getType(jdtType);
    return walaType;
  }

  private boolean findReceiverClass() {
    if (ctx.isReceiverImplicitThis()) {
      receiverType = enclosingType;
    } else {
      final ITypeName receiverTypeName = ctx.getReceiverType();
      receiverType = toWalaClass(receiverTypeName);
    }
    return receiverType != null;
  }

  private void computeAccessibleFields() {
    for (final IField field : receiverType.getAllFields()) {
      if (!isAccessible(field)) {
        continue;
      }
      if (!matchesPrefix(field)) {
        continue;
      }
      final FieldChainElement chainElement = new FieldChainElement(field);
      accessibleFields.add(chainElement);
    }
  }

  private boolean isAccessible(final IMember member) {
    if (member.getDeclaringClass() == receiverType) {
      return true;
    }
    if (!member.isPrivate()) {
      return true;
    }
    return false;
  }

  private boolean matchesPrefix(final IMember member) {
    final String prefixToken = ctx.getPrefixToken();
    final String name = member.getName().toString();
    return name.startsWith(prefixToken);
  }

  private void computeAccessibleMethods() {

    for (final IMethod method : receiverType.getAllMethods()) {
      if (method.isVoid()) {
        continue;
      }
      if (!isAccessible(method)) {
        continue;
      }
      if (!matchesPrefix(method)) {
        continue;
      }

      final MethodChainElement chainElement = new MethodChainElement(method);
      accessibleMethods.add(chainElement);
    }
  }

  private void computeAccessibleLocals() {
    if (!ctx.getVariable().isThis()) {
      return;
    }
    for (final LocalDeclaration local : ctx.getLocalDeclarations()) {
      final ITypeName typeName = CompilerBindings.toTypeName(local.type);
      final IClass localType = toWalaClass(typeName);
      final String localName = String.valueOf(local.name);
      final IChainElement element = new LocalVariableChainElement(localName, localType);
      accessibleLocals.add(element);
    }
  }

  public IClass getExpectedType() {
    return expectedType;
  }

  public IClass getCallingContext() {
    return enclosingType;
  }

  public List<IChainElement> getProposedFields() {
    return accessibleFields;
  }

  public List<IChainElement> getProposedMethods() {
    return accessibleMethods;
  }

  public List<IChainElement> getProposedVariables() {
    return accessibleLocals;
  }

}

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
package org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.ChainingAlgorithm;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.util.LookupUtilJdt;

/**
 * Default implementation of {@link IAccessibleElementsProposer} and being used
 * in {@link ChainingAlgorithm}
 * 
 * REVIEW NAME-CODE mismatch. Here we do some ast parsing right? separate this
 * into its own class (or resuse intelligent completion context - extensions to
 * completionContext needed? Ask on the mailing list and provide examples.).
 * 
 * Please provide a set of all potential completion events you want to complete
 * on; we use them as specification to test against.
 */
@SuppressWarnings("restriction")
public class FieldsAndMethodsCompletionContext {
  private class InternalCompletionProposalCollector extends CompletionRequestor {
    public InternalCompletionProposalCollector() {
      setRequireExtendedContext(true);
      setIgnored(CompletionProposal.KEYWORD, true);
      setIgnored(CompletionProposal.PACKAGE_REF, true);
      setIgnored(CompletionProposal.LABEL_REF, true);
      setIgnored(CompletionProposal.FIELD_IMPORT, true);
      setIgnored(CompletionProposal.METHOD_IMPORT, true);
      setIgnored(CompletionProposal.TYPE_REF, true);
    }

    @Override
    public void accept(final CompletionProposal proposal) {
      switch (proposal.getKind()) {
      case CompletionProposal.FIELD_REF:
        final String thisString = new String("this.");
        final String superString = new String("super.");
        String completionField = new String(proposal.getCompletion());
        completionField = completionField.replace(thisString, "").replace(superString, "");
        proposedFields.add(new ChainedProposalAnchor(completionField, proposal.getSignature(), null));
        break;
      case CompletionProposal.METHOD_REF:
        String completionMethod = new String(proposal.getCompletion());
        if (completionMethod.contains("(")) {
          completionMethod = completionMethod.substring(0, completionMethod.indexOf('('));
          proposedMethods.add(new ChainedProposalAnchor(completionMethod,
              Signature.getReturnType(proposal.getSignature()), proposal.findParameterNames(null)));
        }
        break;
      case CompletionProposal.LOCAL_VARIABLE_REF:
        proposedVariables.add(new ChainedProposalAnchor(proposal.getCompletion(), proposal.getSignature(), null));
        break;
      default:
        System.out.println("Different Eclipse proposal: " + proposal);
      }
    }

    @Override
    public void acceptContext(final org.eclipse.jdt.core.CompletionContext context) {
      super.acceptContext(context);
    }
  }

  private static final String DOT = String.valueOf(Signature.C_DOT);

  private final JavaContentAssistInvocationContext jctx;

  private final List<ChainedProposalAnchor> proposedFields, proposedMethods, proposedVariables;

  private JavaProject project;

  private IType callingContext;

  private char[] callingStatementName;

  private char expextedTypeSignatures[] = null;

  public FieldsAndMethodsCompletionContext(final JavaContentAssistInvocationContext jctx) throws JavaModelException {
    proposedFields = new LinkedList<ChainedProposalAnchor>();
    proposedMethods = new LinkedList<ChainedProposalAnchor>();
    proposedVariables = new LinkedList<ChainedProposalAnchor>();
    this.jctx = jctx;
    // final CompletionEngine engine =
    createEngineAndComputeCompletions();
    // final CompletionParser parser = (CompletionParser) engine.getParser();
    // System.out.println(parser.referenceContext);
  }

  public ICompilationUnit getCompilationUnit() {
    return jctx.getCompilationUnit();
  }

  public List<ChainedProposalAnchor> getProposedFields() {
    return proposedFields;
  }

  public List<ChainedProposalAnchor> getProposedMethods() {
    return proposedMethods;
  }

  public List<ChainedProposalAnchor> getProposedVariables() {
    return proposedVariables;
  }

  public char[] getExpectedTypeSignature() {
    if (expextedTypeSignatures == null) {
      final char sigs[][] = jctx.getCoreContext().getExpectedTypesSignatures();
      expextedTypeSignatures = (sigs != null) && (sigs.length > 0) ? sigs[0] : null;
      return expextedTypeSignatures;
    } else
      return expextedTypeSignatures;
  }

  public IType getExpectedType() {
    IType returnType = null;
    final CompletionContext context = jctx.getCoreContext();
    if (context != null) {
      final char[][] expectedTypes = context.getExpectedTypesSignatures();
      if ((expectedTypes != null) && (expectedTypes.length > 0)) {
        final IJavaProject project = getCompilationUnit().getJavaProject();
        if (project != null) {
          try {
            returnType = project.findType(SignatureUtil.stripSignatureToFQN(String.valueOf(expectedTypes[0])));
          } catch (final JavaModelException x) {
            JavaPlugin.log(x);
          }
        }
      } else {
        try {
          return LookupUtilJdt.lookupType(expextedTypeSignatures);
        } catch (final JavaModelException e) {
          return null;
        }
      }
    }
    return returnType;
  }

  private CompletionEngine createEngineAndComputeCompletions() throws JavaModelException {
    final ICompilationUnit cu = jctx.getCompilationUnit();
    project = (JavaProject) cu.getJavaProject();
    final WorkingCopyOwner owner = cu.getOwner();
    final SearchableEnvironment s = project.newSearchableNameEnvironment(owner);
    final CompletionEngine engine = new CompletionEngine(s, new InternalCompletionProposalCollector(),
        project.getOptions(true), project, owner, new NullProgressMonitor());
    final org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCu = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) cu;
    String prefix = "";
    try {
      prefix = FieldsAndMethodsCompletionContext.getPrefixTopFirstNonJavaCharacter(jctx.getDocument(),
          jctx.getInvocationOffset());
    } catch (final BadLocationException e) {
      // This is not expected to happen
    }
    final int length = prefix.length();
    final int start = prefix.endsWith(FieldsAndMethodsCompletionContext.DOT) ? length : 0;
    engine.complete(compilerCu, jctx.getInvocationOffset(), start, cu);
    computeCallingContext(engine, compilerCu);
    computeCallingStatementName();
    return engine;
  }

  /**
   * This method trys to get the calling statement name, if it is not availible.
   */
  private void computeCallingStatementName() {
    if (callingStatementName == null) {
      try {
        callingStatementName = getVariableName(jctx.getDocument(), jctx.getInvocationOffset()).toCharArray();
      } catch (final BadLocationException e) {
        JavaPlugin.log(e);
      }
    }
  }

  /**
   * The calling context is necessary to get the class / class reference from
   * where the call chain was executed.
   * 
   * @param engine
   * @param compilerCompilationUnit
   * @throws JavaModelException
   */
  private void computeCallingContext(final CompletionEngine engine,
      final org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCompilationUnit) throws JavaModelException {
    final CompletionParser d = (CompletionParser) engine.getParser();
    if (d.referenceContext != null) {
      if (d.referenceContext instanceof AbstractMethodDeclaration) {
        computeMethod(d);
      } else if (d.referenceContext instanceof CompilationUnitDeclaration) {
        computeMember(d);
      }
    } else {
      System.out.println("d.referenceContext == null");
    }
    if (callingContext == null) {
      computeCallingContextOtherwise(compilerCompilationUnit);
    }
  }

  /**
   * The last chance to get the calling context
   * 
   * @param compilerCu
   * @throws JavaModelException
   */
  private void computeCallingContextOtherwise(final org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCu)
      throws JavaModelException {
    final StringBuffer buffer = new StringBuffer();
    buffer.append('L');
    for (final char[] array : compilerCu.getPackageName()) {
      buffer.append(array);
      buffer.append('.');
    }
    buffer.append(compilerCu.getMainTypeName());
    buffer.append(';');
    callingContext = LookupUtilJdt.lookupType(buffer.toString().toCharArray());
  }

  /**
   * The call chain is triggered on an member, so try to get the calling context
   * from this member.
   * 
   * @param d
   * @throws JavaModelException
   */
  private void computeMember(final CompletionParser d) throws JavaModelException {
    final CompilationUnitDeclaration reference = (CompilationUnitDeclaration) d.referenceContext;
    // FIXME: Why only zero?
    if (reference.types != null) {
      final TypeDeclaration declaration = reference.types[0];
      int i = -1;
      do {
        i++;
        if (declaration.fields[i].initialization != null) {
          break;
        }
      } while (declaration.fields.length > i + 1);
      if (declaration.fields[i].initialization instanceof NameReference) {
        computeCallingFromNameReference(declaration, i);
      } else if (declaration.fields[i].initialization instanceof CompletionOnMemberAccess) {
        computeCallingFromMemberAccess(declaration, i);
      } else {
        // System.out
        // .println("!(declaration.fields[i].initialization instanceof CompletionOnQualifiedNameReference && CompletionOnMemberAccess)");
      }
    } else {
      // System.out.println("Member: reference.types == null");
    }
  }

  /**
   * The completion is parsed to a {@link CompletionOnMemberAccess}
   * 
   * @param declaration
   * @param fieldNo
   */
  private void computeCallingFromMemberAccess(final TypeDeclaration declaration, final int fieldNo) {
    final CompletionOnMemberAccess init = (CompletionOnMemberAccess) declaration.fields[fieldNo].initialization;
    if ((init.receiver != null) && (init.receiver instanceof MessageSend)) {
      final MessageSend messageSend = (MessageSend) init.receiver;
      if (messageSend.binding instanceof MethodBinding) {
        final MethodBinding binding = messageSend.binding;
        if ((binding.returnType.genericTypeSignature() != null)
            && (binding.returnType.genericTypeSignature().length >= 1)) {
          try {
            callingContext = LookupUtilJdt.lookupType(binding.returnType.genericTypeSignature());
          } catch (final JavaModelException e) {
            JavaPlugin.log(e);
          }
        } else {
          System.out.println("binding.signature() != null && binding.signature().length == 0");
        }
      } else {
        System.out.println("!messageSend.binding instanceof MethodBinding");
      }
    } else {
      System.out.println("init.receiver != null && init.receiver instanceof MessageSend");
    }
  }

  /**
   * The completion is parsed to a {@link NameReference}
   * 
   * @param declaration
   * @param fieldNo
   */
  private void computeCallingFromNameReference(final TypeDeclaration declaration, final int fieldNo)
      throws JavaModelException {
    final NameReference init = (NameReference) declaration.fields[fieldNo].initialization;
    if (init.binding != null) {
      if (init.binding instanceof ReferenceBinding) {
        final ReferenceBinding binding = (ReferenceBinding) init.binding;
        if ((binding.signature() != null) && (binding.signature().length >= 1)) {
          callingContext = LookupUtilJdt.lookupType(binding.signature());
        } else {
          System.out.println("binding.signature() != null && binding.signature().length == 0");
        }
      } else if (init.binding instanceof FieldBinding) {
        final FieldBinding binding = (FieldBinding) init.binding;
        if ((binding.type.genericTypeSignature() != null) && (binding.type.genericTypeSignature().length >= 1)) {
          callingContext = LookupUtilJdt.lookupType(binding.type.genericTypeSignature());
        } else {
          System.out.println("binding.signature() != null && binding.signature().length == 0");
        }
      }
    } else {
      System.out.println("fields: init.binding == null");
    }
  }

  /**
   * The call chain is triggered on an method, so try to get the calling context
   * from this method.
   * 
   * @param d
   * @throws JavaModelException
   */
  private void computeMethod(final CompletionParser completionParser) throws JavaModelException {
    final AbstractMethodDeclaration reference = (AbstractMethodDeclaration) completionParser.referenceContext;
    if ((reference.statements != null) && (reference.statements.length >= 1)) {
      final int statementNo = findRightStatement(reference);
      if (reference.statements[statementNo] instanceof LocalDeclaration) {
        final LocalDeclaration statement = (LocalDeclaration) reference.statements[statementNo];
        if (statement.initialization != null) {
          callingStatementName = statement.name;
          // FIXME: depending on prefix check other casts like
          // CompletionOnFieldType, etc.
          if (statement.initialization instanceof CompletionOnQualifiedNameReference) {
            computeCallingContextFromQualifiedNameReference(statement);
          } else if (statement.initialization instanceof CompletionOnMemberAccess) {
            computeCallingContextFromMemberAcces(statement);
          } else if (statement.initialization instanceof MessageSend) {
            JavaPlugin.logErrorMessage("Not yet implemented." + " Please be patient!");
            // computeCallingContextAndExpectedTypeForMethodParameter(statement);
          } else {
            System.out.println("!statement.initialization instanceof CompletionOnQualifiedNameReference: "
                + statement.initialization.getClass());
          }
        } else {
          System.out.println("statement.initialization == null");
        }
      } else if (reference.statements[statementNo] instanceof Assignment) {
        final Assignment statement = (Assignment) reference.statements[statementNo];
        computeCallingFromAssignment(statement);
      } else {
        System.out.println("!reference.statements[i] instanceof LocalDeclaration");
      }
    } else {
      System.out.println("reference.statements != null && reference.statements.length == 0");
    }
  }

  /**
   * iterate through all statements until right one is found
   * 
   * @param reference
   * @return
   */
  private int findRightStatement(final AbstractMethodDeclaration reference) {
    int i = -1;
    do {
      i++;
      if (!(reference.statements[i] instanceof LocalDeclaration) && !(reference.statements[i] instanceof Assignment)) {
        break;
      }
      if ((reference.statements[i] instanceof LocalDeclaration)
          && (((LocalDeclaration) reference.statements[i]).initialization != null)) {
        break;
      }
      if ((reference.statements[i] instanceof Assignment)
          && (((Assignment) reference.statements[i]).expression != null)) {
        break;
      }
    } while (reference.statements.length - 2 >= i);
    return i;
  }

  /**
   * This was a try to get the expected type and the calling context for
   * parameter input inside a method signature... It is whether completed nor
   * does it function.
   * 
   * @param statement
   */
  @SuppressWarnings("unused")
  private void computeCallingContextAndExpectedTypeForMethodParameter(final LocalDeclaration statement) {
    // calling Context will be computed through computeCallingContextOtherwise()
    final MessageSend init = (MessageSend) statement.initialization;
    if (init.isThis()) {
      // Methoden aus This holen
    } else if (init.isSuper()) {
      // Methoden aus Super holen
    } else {
      // Methoden aus aufrufenden Typ holen
      final TypeBinding callingMethodContext = init.actualReceiverType;
      try {
        final IType callingMethodTyp = LookupUtilJdt.lookupType(callingMethodContext.signature());
        final ArrayList<IMethod> methodList = new ArrayList<IMethod>();
        // finde methode mit gleichem namen und mindestens der selben Anzahl an
        // Parameter
        for (final IMethod method : callingMethodTyp.getMethods()) {
          if (method.getElementName().equals(new String(init.selector))
              && ((init.arguments == null) || (init.arguments.length <= method.getNumberOfParameters()))) {
            // methode gefunden
            methodList.add(method);
          }
        }
        for (final IMethod method : methodList) {
          // nach Parameter schauen ob gleich sind
          if (init.arguments == null) {
            // keine Parameter vorher eingetippt
            if (method.getParameterTypes().length == 0) {
              // erste methode nehmen, keine Inputparameter
              expextedTypeSignatures = null;
              return;
            } else {
              // erste Methode hat Inputparameter
              expextedTypeSignatures = method.getParameterTypes()[0].toCharArray();
              return;
            }
          } else {
            // parameter vorher eingetippt nach match suchen
            IMethod rightMethod = null;
            for (int i = 0; i < init.arguments.length; i++) {
              if (method.getParameterTypes()[i].equals(new String(init.arguments[i].resolvedType.signature())
                  .replaceAll("/", "."))) {
                rightMethod = method;
              } else {
                rightMethod = null;
              }
            }
            if (rightMethod == null) {
              // keine richtige Methode gefunden
              expextedTypeSignatures = null;
              continue;
            } else {
              if (rightMethod.getParameterTypes().length == init.arguments.length) {
                // wir gehen davon aus das die Methodenparameteranzahl erreicht
                // worden ist
                expextedTypeSignatures = null;
                return;
              } else {
                // wir nehmen den n�chsten Parameter aus der Methode
                expextedTypeSignatures = rightMethod.getParameterTypes()[init.arguments.length].toCharArray();
                return;
              }
            }
          }
        }
      } catch (final JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
  }

  /**
   * Compute calling context from assignment access.
   * 
   * @param assignmentInMethod
   * @throws JavaModelException
   */
  private void computeCallingFromAssignment(final Assignment assignmentInMethod) throws JavaModelException {
    if (assignmentInMethod != null) {
      if ((assignmentInMethod.expression != null)
          && (assignmentInMethod.expression instanceof CompletionOnQualifiedNameReference)) {
        final CompletionOnQualifiedNameReference expression = (CompletionOnQualifiedNameReference) assignmentInMethod.expression;
        if (expression.binding instanceof BinaryTypeBinding) {
          callingContext = LookupUtilJdt.lookupType(((BinaryTypeBinding) expression.binding).signature());
        } else {
          System.out.println("Assigmant: !(expression.binding instanceof BinaryTypeBinding)");
        }
      } else {
        System.out
            .println("statement.expression == null || !(statement.expression instanceof CompletionOnQualifiedNameReference)");
      }
    } else {
      System.out.println("Assigmant: statement == null");
    }
  }

  /**
   * Compute calling context from {@link CompletionOnMemberAccess} access.
   * 
   * @param assignmentInMethod
   * @throws JavaModelException
   */
  private void computeCallingContextFromMemberAcces(final LocalDeclaration localMethodDeclaration)
      throws JavaModelException {
    final CompletionOnMemberAccess init = (CompletionOnMemberAccess) localMethodDeclaration.initialization;
    if (init.actualReceiverType != null) {
      final TypeBinding binding = init.actualReceiverType;
      if ((binding.signature() != null) && (binding.signature().length >= 1)) {
        callingContext = LookupUtilJdt.lookupType(binding.signature());
      } else {
        System.out.println("binding.signature() != null && binding.signature().length == 0");
      }
    } else {
      System.out.println("init.actualReceiverType != null");
    }
  }

  /**
   * Compute calling context from {@link CompletionOnQualifiedNameReference}
   * access.
   * 
   * @param assignmentInMethod
   * @throws JavaModelException
   */
  private void computeCallingContextFromQualifiedNameReference(final LocalDeclaration localMethodDeclaration)
      throws JavaModelException {
    final CompletionOnQualifiedNameReference init = (CompletionOnQualifiedNameReference) localMethodDeclaration.initialization;
    if (init.binding != null) {
      if (init.binding instanceof ReferenceBinding) {
        final ReferenceBinding binding = (ReferenceBinding) init.binding;
        if ((binding.signature() != null) && (binding.signature().length >= 1)) {
          callingContext = LookupUtilJdt.lookupType(binding.signature());
        } else {
          System.out.println("binding.signature() != null && binding.signature().length == 0");
        }
      } else if (init.binding instanceof FieldBinding) {
        final FieldBinding binding = (FieldBinding) init.binding;
        if ((binding.type.genericTypeSignature() != null) && (binding.type.genericTypeSignature().length >= 1)) {
          callingContext = LookupUtilJdt.lookupType(binding.type.genericTypeSignature());
        } else {
          System.out.println("binding.signature() != null && binding.signature().length == 0");
        }
      }
    } else {
      System.out.println("init.actualReceiverType != null");
    }
  }

  /**
   * Helper method to detect the portion of code that has already been typed up
   * to the point where content assist got invoked
   * 
   * @param doc
   * @param offset
   * @return
   * @throws BadLocationException
   */
  public static String getPrefixTopFirstNonJavaCharacter(final IDocument doc, int offset) throws BadLocationException {
    final StringBuilder sb = new StringBuilder();
    char c = doc.getChar(offset - 1);
    while (Character.isJavaIdentifierPart(c)) {
      sb.insert(0, c);
      c = doc.getChar(--offset - 1);
    }
    return sb.toString();
  }

  /**
   * Helper method to detect the portion of code to the equal-symbol
   * 
   * @param doc
   * @param offset
   * @return
   * @throws BadLocationException
   */
  public static String getPrefixToEqualSymbol(final IDocument doc, int offset) throws BadLocationException {
    final IRegion lineRegion = doc.getLineInformationOfOffset(offset);
    final int lineStart = lineRegion.getOffset();
    final StringBuilder sb = new StringBuilder();
    char c = doc.getChar(offset - 1);
    while (c != '=') {
      sb.insert(0, c);
      c = doc.getChar(--offset - 1);
      if (offset <= lineStart)
        return "";
    }
    if ((sb.length() > 0) && sb.substring(0, 1).equals(" ")) {
      sb.replace(0, 1, "");
    }
    return sb.toString();
  }

  /**
   * Helper method to detect the position of the equal-symbol
   * 
   * @param doc
   * @param offset
   * @return
   * @throws BadLocationException
   */
  public static int getOffsetToEqualSymbol(final IDocument doc, int offset) throws BadLocationException {
    final IRegion lineRegion = doc.getLineInformationOfOffset(offset);
    final int lineStart = lineRegion.getOffset();
    char c = doc.getChar(offset - 1);
    while (c != '=') {
      c = doc.getChar(--offset - 1);
      if (offset <= lineStart)
        return -1;
    }
    return offset;
  }

  private String getVariableName(final IDocument doc, int offset) throws BadLocationException {
    final IRegion lineRegion = doc.getLineInformationOfOffset(offset);
    final int lineStart = lineRegion.getOffset();
    final StringBuilder sb = new StringBuilder();
    char c = doc.getChar(offset - 1);
    while (c != '=') {
      c = doc.getChar(offset--);
      if (offset <= lineStart)
        return "";
    }
    while (!Character.isJavaIdentifierPart(c)) {
      c = doc.getChar(offset--);
      if (offset <= lineStart)
        return "";
    }
    while (Character.isJavaIdentifierPart(c)) {
      sb.insert(0, c);
      c = doc.getChar(offset--);
      if (offset <= lineStart)
        return "";
    }
    return sb.toString();
  }

  public IType getCallingContext() {
    return callingContext;
  }

  public char[] getCallingVariableName() {
    return callingStatementName;
  }
}

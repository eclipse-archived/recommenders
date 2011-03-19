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
package org.eclipse.recommenders.internal.rcp.codecompletion.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.FieldChainElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.InheritanceHierarchyCache;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.MethodChainElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeName;

@SuppressWarnings("restriction")
public class ChainTemplateProposalGenerator {

  private final TemplateContextType templateContextType;

  private final Image templateIcon;

  private JavaContentAssistInvocationContext jctx;

  private List<IJavaCompletionProposal> completionProposals;

  private List<ChainTemplateProposal> proposals;

  private IClass expectedType;

  private Integer expectedTypeDimension;

  public ChainTemplateProposalGenerator() {
    final ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
    templateContextType = templateContextRegistry.getContextType(JavaContextType.ID_ALL);
    JavaPlugin.getDefault().getCodeTemplateContextRegistry().addContextType(templateContextType);
    templateIcon = AbstractUIPlugin.imageDescriptorFromPlugin(Constants.ProposalSettings.PLUGIN_ID,
        Constants.ProposalSettings.IMAGE_PATH).createImage();
  }

  @SuppressWarnings("unchecked")
  public List<IJavaCompletionProposal> generateJavaCompletionProposals(final List<ChainTemplateProposal> proposals,
      final JavaContentAssistInvocationContext jctx, final long algortihmComputeTime) {
    this.proposals = proposals;
    this.jctx = jctx;
    final long proposalStartTime = System.currentTimeMillis();
    if (isValidProposal()) {
      return Collections.EMPTY_LIST;
    }
    sortProposals();
    synchronized (proposals) {
      return computeProposalList(algortihmComputeTime, proposalStartTime);
    }
  }

  private boolean isValidProposal() {
    return proposals == null || proposals.isEmpty();
  }

  // This method sorts all computed 'raw' proposals.
  private void sortProposals() {
    // Prefer proposals that do not cope with casts
    Collections.sort(proposals, new ProposalComperator());
  }

  private String computePrefixToEquals() {
    String prefixToEquals = "";
    try {
      prefixToEquals = getPrefixToEqualSymbol(jctx.getDocument(), jctx.getInvocationOffset());
    } catch (final BadLocationException e) {
      JavaPlugin.log(e);
    }
    return prefixToEquals;
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
    StringBuilder sb = new StringBuilder();
    char c = doc.getChar(--offset);
    boolean foundComma = false;
    boolean foundClosingBracket = false;
    boolean foundOpeningBracket = false;
    String temp = new String();
    for (;;) {

      if (c == '=') {
        break;
      }
      sb.insert(0, c);
      if (c == ',' && !foundComma) {
        foundComma = true;
        temp = sb.toString();
      }
      if (c == ')' || c == ']') {
        foundClosingBracket = true;
      }
      if (c == '(' || c == '[') {
        foundOpeningBracket = true;
      }
      if (foundClosingBracket && foundOpeningBracket) {
        foundOpeningBracket = false;
        foundClosingBracket = false;
        foundComma = false;
        temp = new String();
      } else if (!foundClosingBracket && foundOpeningBracket || !foundClosingBracket && foundOpeningBracket
          && foundComma) {
        if (!temp.isEmpty()) {
          sb = new StringBuilder(temp);
        }
        break;
      }
      c = doc.getChar(--offset);
      if (offset <= lineStart) {
        return "";
      }
    }
    if (sb.length() > 0 && sb.substring(0, 1).equals(" ")) {
      sb.replace(0, 1, "");
    }
    return sb.toString();
  }

  // Computes the Java context. Due to all proposals are erased, the offset to
  // the equals symbol is needed. --> //XXX need a solution, if this plug-in is
  // called to gain method parameter.
  private JavaContext computeJavaContext() {
    int offsetToEquals;
    offsetToEquals = computeOffsetToEquals(jctx);
    offsetToEquals++;
    offsetToEquals++;
    final IDocument document = jctx.getDocument();
    final JavaContext ctx = new JavaContext(templateContextType, document, offsetToEquals, 0, jctx.getCompilationUnit());
    ctx.setForceEvaluation(true);
    return ctx;
  }

  private int computeOffsetToEquals(final JavaContentAssistInvocationContext jctx) {
    int offsetToEquals = -1;
    try {
      offsetToEquals = getOffsetToEqualSymbol(jctx.getDocument(), jctx.getInvocationOffset());
      if (offsetToEquals == -1) {
        offsetToEquals = jctx.getInvocationOffset();
      }
    } catch (final BadLocationException e) {
      JavaPlugin.log(e);
    }
    return offsetToEquals;
  }

  public static int getOffsetToEqualSymbol(final IDocument doc, int offset) throws BadLocationException {
    final IRegion lineRegion = doc.getLineInformationOfOffset(offset);
    final int lineStart = lineRegion.getOffset();
    char c = doc.getChar(offset - 1);
    boolean foundComma = false;
    boolean foundClosingBracket = false;
    boolean foundOpeningBracket = false;
    int temp = offset--;
    for (;;) {
      if (c == '=') {
        break;
      }
      if (c == ',' && !foundComma) {
        foundComma = true;
        temp = offset++;
      }
      if (c == ')' || c == ']') {
        foundClosingBracket = true;
      }
      if (c == '(' || c == '[') {
        foundOpeningBracket = true;
      }
      if (foundClosingBracket && foundOpeningBracket) {
        foundOpeningBracket = false;
        foundClosingBracket = false;
        foundComma = false;
        temp = offset;
      } else if (!foundClosingBracket && foundOpeningBracket && !foundClosingBracket && foundOpeningBracket
          && foundComma) {
        if (temp > offset) {
          offset = temp;
        }
        break;
      }
      c = doc.getChar(--offset);
      if (offset <= lineStart) {
        return -1;
      }
    }
    if (doc.getChar(offset) == ' ') {
      offset++;
    }
    return offset;
  }

  // Iterates over all computed proposals. First this method checks if the time
  // is up, or the max. number of proposals
  // are reached. Then it generates the code, description and finally the
  // proposal template.
  private List<IJavaCompletionProposal> computeProposalList(final long algortihmComputeTime,
      final long proposalStartTime) {
    completionProposals = new ArrayList<IJavaCompletionProposal>();

    int proposalNo = 1;
    for (final ChainTemplateProposal chainProposal : proposals) {
      expectedType = chainProposal.getExpectedType();
      expectedTypeDimension = chainProposal.getExpectedTypeDimension();
      if (isMaxProposalCount(proposalNo) || isMaxPluginComputationTime(algortihmComputeTime, proposalStartTime)) {
        break;
      }
      proposalNo = computeProposalPart(proposalNo, chainProposal);
    }
    return completionProposals;
  }

  private int computeProposalPart(int proposalNo, final ChainTemplateProposal chainProposal) {
    try {
      final String code = generateCode(chainProposal);
      if (code != null) {
        computeAndAddProposal(proposalNo, chainProposal, code);
      }
      proposalNo++;
    } catch (final Exception e) {
      JavaPlugin.log(e);
    }
    return proposalNo;
  }

  private void computeAndAddProposal(final int proposalNo, final ChainTemplateProposal chainProposal, final String code)
      throws BadLocationException, TemplateException {
    final JavaContext ctx = computeJavaContext();
    final Template template = generateTemplate(chainProposal, code); // name
    final IRegion region = computeRegion(ctx);
    if (canEvaluateTemplate(ctx, template)) {
      final TemplateProposal prop = new TemplateProposal(template, ctx, region, templateIcon);
      prop.setRelevance(500 + proposals.size() - proposalNo);
      completionProposals.add(prop);
    } else {
      System.err.println("Evaluation failed: " + code);
    }
  }

  private boolean canEvaluateTemplate(final JavaContext ctx, final Template template) throws BadLocationException,
      TemplateException {
    boolean isEvaluable;
    boolean hasProposalString;
    try {
      isEvaluable = ctx.evaluate(template) != null;
      hasProposalString = ctx.evaluate(template).getString() != null;
    } catch (Exception e) {
      isEvaluable = false;
      hasProposalString = false;
    }

    return isEvaluable && hasProposalString;
  }

  private IRegion computeRegion(final JavaContext ctx) {
    int start = computeOffsetToEquals(jctx);
    try {
      if (ctx.getDocument().getChar(start) != ' ') {
        start++;
      }
    } catch (BadLocationException e) {
      JavaPlugin.log(e);
    }
    final int end = ctx.getEnd();
    final IRegion region = new Region(start, end - start);
    return region;
  }

  private Template generateTemplate(final ChainTemplateProposal chainProposal, final String code) {
    final String description = computeDescription(chainProposal);
    final String name = computeName(chainProposal);
    final Template template = new Template(name, description, "java", code, true);
    return template;
  }

  private boolean isMaxPluginComputationTime(final long algortihmComputeTime, final long proposalStartTime) {
    return System.currentTimeMillis() - proposalStartTime > Constants.ProposalSettings.MAX_PROPOSAL_COMPUTATION_TIME_IN_MS
        + algortihmComputeTime;
  }

  private boolean isMaxProposalCount(final int proposalNo) {
    return Constants.ProposalSettings.MAX_PROPOSAL_COUNT < proposalNo;
  }

  // This method computes the name, which is displayed in the proposal box.
  private String computeName(final ChainTemplateProposal chainProposal) {
    StringBuilder name = null;
    for (final IChainElement part : chainProposal.getProposedChain()) {
      final String partName = makePartName(part, chainProposal.getProposedChain().indexOf(part));
      if (partName == null) {
        return null;
      }
      if (name == null) {
        name = new StringBuilder().append(partName);
      } else {
        name.append(Signature.C_DOT).append(partName);
      }
    }
    computeCastingForName(chainProposal, name);
    return name.toString();
  }

  private void computeCastingForName(final ChainTemplateProposal chainProposal, final StringBuilder code) {
    if (chainProposal.needsCast()) {
      code.insert(0,
          String.format("(%s) ", chainProposal.getCastingType().getReference().getName().getClassName().toString()));
    }
  }

  // This method generates the part name for the proposal box. If the part is a
  // method with input parameters an
  // '(...)' is added, else '()'
  private String makePartName(final IChainElement part, int chainPosition) {
    String result = new String();
    switch (part.getElementType()) {
    case FIELD:
      result = checkForThisQualifier((FieldChainElement) part, chainPosition) ? "this." + part.getCompletion() : part
          .getCompletion();
      break;
    case METHOD:
      result = makeTemplatePartNameForMethod(part);
      break;
    case LOCAL:
      result = part.getCompletion();
      break;
    }
    result += computeArrayBracketsForTemplate(part).replaceAll("[${}]", "");
    return result;
  }

  private String makeTemplatePartNameForMethod(final IChainElement part) {
    final MethodChainElement me = (MethodChainElement) part;
    final StringBuilder methodCode = new StringBuilder().append(part.getCompletion());
    if (me.getParameterNames().length == 0) {
      methodCode.append("()");
    } else {
      methodCode.append("(...)");
    }
    return methodCode.toString();
  }

  // This method computes the description for the proposal box
  private String computeDescription(final ChainTemplateProposal chainProposal) {
    final int chainLength = chainProposal.getProposedChain().size();
    StringBuffer res = new StringBuffer();
    res.append("(");
    res.append(chainLength == 1 ? "1 element, " : chainLength + " elements, ");

    if (chainProposal.needsCast()) {
      res.append("type cast, ");
    }
    if (chainProposal.getProposedChain().get(0).isStatic()) {
      res.append("static, ");
    }
    res.append(chainProposal.getExpectedType().getName().getClassName().toString());
    res.append(computeArrayBracketsForCasting(chainProposal));
    res.append(")");
    return res.toString();
  }

  // This method computes the code for the proposals. Therefore the hole string
  // is created, so that every prefix has
  // to be overridden.
  private String generateCode(final ChainTemplateProposal chainProposal) throws JavaModelException {

    final String prefixToLastDot = computePrefixToLastDot();

    StringBuilder code = null;
    for (final IChainElement part : chainProposal.getProposedChain()) {
      final String partCode = makePartCode(part, chainProposal.getProposedChain().indexOf(part));
      if (partCode == null) {
        return null;
      }
      if (code == null) {
        code = new StringBuilder().append(prefixToLastDot).append(partCode);
      } else {
        code.append(Signature.C_DOT).append(partCode);
      }
    }
    computeCastingForCode(chainProposal, code);
    code.append("${cursor}");
    return code.toString();
  }

  private String computePrefixToLastDot() {
    final String prefixToEquals = computePrefixToEquals();
    final String prefixToLastDot = prefixToEquals.substring(0, prefixToEquals.lastIndexOf('.') + 1);
    return prefixToLastDot;
  }

  private void computeCastingForCode(final ChainTemplateProposal chainProposal, final StringBuilder code) {
    if (chainProposal.needsCast()) {
      final String castingString = String.format("(${type:newType(%s)}%s)", computeCastingForCode(chainProposal),
          computeArrayBracketsForCasting(chainProposal));
      code.insert(0, castingString);
    }
  }

  private String computeArrayBracketsForCasting(ChainTemplateProposal chainProposal) {
    String brackets = new String();
    for (int i = expectedTypeDimension; i > 0; i--) {
      brackets += "[]";
    }
    return brackets;
  }

  private String computeCastingForCode(final ChainTemplateProposal chainProposal) {
    TypeName name = chainProposal.getCastingType().getName();
    String casting = name.getPackage().toString() + "/" + name.getClassName().toString();
    casting = casting.replaceAll("/", ".");
    return casting;
  }

  // This method generates a part of the code for one proposal.
  private String makePartCode(final IChainElement part, int chainPosition) throws JavaModelException {
    String result = new String();
    switch (part.getElementType()) {
    case FIELD:
      result = checkForThisQualifier((FieldChainElement) part, chainPosition) ? "this." + part.getCompletion() : part
          .getCompletion();
      break;
    case METHOD:
      final MethodChainElement methodChainElement = (MethodChainElement) part;
      final StringBuilder methodCode = new StringBuilder().append(part.getCompletion()).append(Signature.C_PARAM_START);
      includeParameterNames(methodChainElement, methodCode);
      methodCode.append(Signature.C_PARAM_END);
      result = methodCode.toString();
      break;
    case LOCAL:
      result = part.getCompletion();
      break;
    }
    result += computeArrayBracketsForTemplate(part);
    return result;
  }

  private String computeArrayBracketsForTemplate(final IChainElement part) {
    String result = new String();
    if (isEqualToExpectedType(part) || isInSubtypeHierarchie(part) || isInSupertypeHierarchie(part)) {
      for (int i = part.getArrayDimension() - expectedTypeDimension; i > 0; i--) {
        result += "[${" + ProposalNameGenerator.generateFreeVariableName() + "}]";
      }
    } else {
      for (int i = part.getArrayDimension(); i > 0; i--) {
        result += "[${" + ProposalNameGenerator.generateFreeVariableName() + "}]";
      }
    }
    return result;
  }

  private boolean isInSupertypeHierarchie(IChainElement part) {
    try {
      return InheritanceHierarchyCache.isSupertype(part.getType(), expectedType, expectedTypeDimension);
    } catch (JavaModelException e) {
      return false;
    }
  }

  private boolean isInSubtypeHierarchie(IChainElement part) {
    try {
      return InheritanceHierarchyCache.isSubtype(part.getType(), expectedType, expectedTypeDimension);
    } catch (JavaModelException e) {
      return false;
    }
  }

  private boolean isEqualToExpectedType(final IChainElement part) {
    return expectedType.getName().equals(part.getType().getReference().getInnermostElementType().getName());
  }

  // XXX methods check
  private boolean checkForThisQualifier(final FieldChainElement part, int chainPosition) {
    if (part.hasThisQualifier()) {
      return chainPosition == 0;// prefixToLastDot.isEmpty() ||
                                // prefixToLastDot.equals(" ");
    }
    return false;
  }

  private void includeParameterNames(final MethodChainElement me, final StringBuilder methodCode) {
    boolean firstParam = true;
    for (final String paramName : me.getParameterNames()) {
      if (!firstParam) {
        methodCode.append(", ");
      }
      methodCode.append(String.format("${" + paramName + "}"));
      firstParam = false;
    }
  }
}

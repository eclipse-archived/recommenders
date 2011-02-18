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
import java.util.Comparator;
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
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.MethodChainElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("restriction")
public class ChainProposalTemplateGenerator {
  private final TemplateContextType templateContextType;

  private final Image templateIcon;

  private JavaContentAssistInvocationContext jctx;

  private List<IJavaCompletionProposal> completionProposals;

  public ChainProposalTemplateGenerator() {
    final ContextTypeRegistry templateContextRegistry = JavaPlugin.getDefault().getTemplateContextRegistry();
    templateContextType = templateContextRegistry.getContextType(JavaContextType.ID_ALL);
    JavaPlugin.getDefault().getCodeTemplateContextRegistry().addContextType(templateContextType);
    templateIcon = AbstractUIPlugin.imageDescriptorFromPlugin(Constants.ProposalSettings.PLUGIN_ID,
        Constants.ProposalSettings.IMAGE_PATH).createImage();
  }

  @SuppressWarnings("unchecked")
  public List<IJavaCompletionProposal> generateJavaCompletionProposals(final List<ChainProposal> proposals,
      final JavaContentAssistInvocationContext jctx, final long algortihmComputeTime) {
    this.jctx = jctx;
    final long proposalStartTime = System.currentTimeMillis();
    if (isValidProposal(proposals)) {
      return Collections.EMPTY_LIST;
    }
    sort(proposals);
    synchronized (proposals) {
      return computeProposalList(proposals, algortihmComputeTime, proposalStartTime);
    }
  }

  private boolean isValidProposal(final List<ChainProposal> proposals) {
    return proposals == null || proposals.isEmpty();
  }

  // This method sorts all computed 'raw' proposals.
  private void sort(final List<ChainProposal> proposals) {
    // Prefer proposals that do not cope with casts
    Collections.sort(proposals, new Comparator<ChainProposal>() {
      @Override
      public int compare(final ChainProposal p1, final ChainProposal p2) {
        if (p1.getProposedChain().size() < p2.getProposedChain().size()) {
          return -1;
        } else if (p1.getProposedChain().size() > p2.getProposedChain().size()) {
          return 1;
        } else {
          if (!p1.needsCast() && p2.needsCast()) {
            return -1;
          }
          if (p1.needsCast() && !p2.needsCast()) {
            return 1;
          }
          return 0;
        }
      }
    });
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
    final StringBuilder sb = new StringBuilder();
    char c = doc.getChar(offset - 1);
    while (c != '=') {
      sb.insert(0, c);
      c = doc.getChar(--offset - 1);
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
    final int offsetToEquals = computeOffsetToEquals(jctx);
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
      } else {
        offsetToEquals++;
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
    while (c != '=') {
      c = doc.getChar(--offset - 1);
      if (offset <= lineStart) {
        return -1;
      }
    }
    return offset;
  }

  // Iterates over all computed proposals. First this method checks if the time
  // is up, or the max. number of proposals
  // are reached. Then it generates the code, description and finally the
  // proposal template.
  private List<IJavaCompletionProposal> computeProposalList(final List<ChainProposal> proposals,
      final long algortihmComputeTime, final long proposalStartTime) {
    completionProposals = new ArrayList<IJavaCompletionProposal>();

    int proposalNo = 1;
    final int proposalListSize = proposals.size();
    for (final ChainProposal proposal : proposals) {
      if (isMaxProposalCount(proposalNo) || isMaxPluginComputationTime(algortihmComputeTime, proposalStartTime)) {
        break;
      }
      proposalNo = computeProposalPart(proposalListSize, proposalNo, proposal);
    }
    return completionProposals;
  }

  private int computeProposalPart(final int proposalListSize, int proposalNo, final ChainProposal proposal) {
    try {
      final String code = generateCode(proposal);
      if (code != null) {
        computeAndAddProposal(proposalListSize, proposalNo, proposal, code);
      }
      proposalNo++;
    } catch (final Exception e) {
      JavaPlugin.log(e);
    }
    return proposalNo;
  }

  private void computeAndAddProposal(final int proposalSize, final int proposalNo, final ChainProposal proposal,
      final String code) throws BadLocationException, TemplateException {
    final JavaContext ctx = computeJavaContext();
    final Template template = generateTemplate(proposal, code); // name
    final IRegion region = computeRegion(ctx);
    if (canEvaluateTemplate(ctx, template)) {
      final TemplateProposal prop = new TemplateProposal(template, ctx, region, templateIcon);
      prop.setRelevance(500 + proposalSize - proposalNo);
      completionProposals.add(prop);
    } else {
      System.err.println("Evaluation failed: " + code);
    }
  }

  private boolean canEvaluateTemplate(final JavaContext ctx, final Template template) throws BadLocationException,
      TemplateException {
    final boolean isEvaluable = ctx.evaluate(template) != null;
    final boolean hasProposalString = ctx.evaluate(template).getString() != null;

    return isEvaluable && hasProposalString;
  }

  private IRegion computeRegion(final JavaContext ctx) {
    final int start = ctx.getStart();
    final int end = ctx.getEnd();
    final IRegion region = new Region(start, end - start);
    return region;
  }

  private Template generateTemplate(final ChainProposal proposal, final String code) {
    final String description = computeDescription(proposal);
    final String name = computeName(proposal);
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
  private String computeName(final ChainProposal proposal) {
    StringBuilder name = null;
    for (final IChainElement part : proposal.getProposedChain()) {
      final String partName = makePartName(part);
      if (partName == null) {
        return null;
      }
      if (name == null) {
        name = new StringBuilder().append(partName);
      } else {
        name.append(Signature.C_DOT).append(partName);
      }
    }
    computeCastingForName(proposal, name);
    return name.toString();
  }

  private void computeCastingForName(final ChainProposal proposal, final StringBuilder code) {
    if (proposal.needsCast()) {
      code.insert(0,
          String.format("(%s) ", proposal.getCastingType().getName().getClassName().toString().replaceAll("/", ".")));
    }
  }

  // This method generates the part name for the proposal box. If the part is a
  // method with input parameters an
  // '(...)' is added, else '()'
  private String makePartName(final IChainElement part) {
    switch (part.getElementType()) {
    case FIELD:
      return part.getCompletion();
    case METHOD:
      return makeTemplatePartNameForMethod(part);
    default:
      return "";
    }
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
  private String computeDescription(final ChainProposal proposal) {
    final int chainLength = proposal.getProposedChain().size();
    String description = chainLength == 1 ? "(1 element" : "(" + chainLength + " elements";
    if (proposal.needsCast()) {
      description += ", type cast";
    }
    description += ')';
    return description;
  }

  // This method computes the code for the proposals. Therefore the hole string
  // is created, so that every prefix has
  // to be overridden.
  private String generateCode(final ChainProposal proposal) throws JavaModelException {

    final String prefixToEquals = computePrefixToEquals();
    final String prefixToLastDot = prefixToEquals.substring(0, prefixToEquals.lastIndexOf('.') + 1);

    StringBuilder code = null;
    for (final IChainElement part : proposal.getProposedChain()) {
      final String partCode = makePartCode(part);
      if (partCode == null) {
        return null;
      }
      if (code == null) {
        code = new StringBuilder().append(prefixToLastDot).append(partCode);
      } else {
        code.append(Signature.C_DOT).append(partCode);
      }
    }
    computeCastingForCode(proposal, code);
    code.append("${cursor}");
    return code.toString();
  }

  private void computeCastingForCode(final ChainProposal proposal, final StringBuilder code) {
    if (proposal.needsCast()) {
      final String castingString = String.format("(${type:newType(%s)})", proposal.getCastingType().getName()
          .getClassName().toString().replaceAll("/", "."));
      code.insert(0, castingString);
    }
  }

  // This method generates a part of the code for one proposal.
  private String makePartCode(final IChainElement part) throws JavaModelException {
    switch (part.getElementType()) {
    case FIELD:
      return part.getCompletion();
    case METHOD:
      final MethodChainElement methodChainElement = (MethodChainElement) part;
      final StringBuilder methodCode = new StringBuilder().append(part.getCompletion()).append(Signature.C_PARAM_START);
      includeParameterNames(methodChainElement, methodCode);
      methodCode.append(Signature.C_PARAM_END);
      return methodCode.toString();
    default:
      return "";
    }
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

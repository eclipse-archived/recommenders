package org.eclipse.recommenders.internal.rcp.codecompletion.chain;

import java.util.Comparator;

import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainElement;
import org.eclipse.recommenders.internal.rcp.codecompletion.chain.algorithm.IChainElement.ChainElementType;

public class ProposalComperator implements Comparator<ChainTemplateProposal> {
  @Override
  public int compare(final ChainTemplateProposal p1, final ChainTemplateProposal p2) {
    // shortest length first
    if (p1.getProposedChain().size() < p2.getProposedChain().size()) {
      return -1;
    } else if (p1.getProposedChain().size() > p2.getProposedChain().size()) {
      return 1;
    } else {
      // casting at the end
      if (!p1.needsCast() && p2.needsCast()) {
        return -1;
      } else if (p1.needsCast() && !p2.needsCast()) {
        return 1;
      } else {
        // sort: variables < fields < method < name of completion
        for (int i = 0; i < p1.getProposedChain().size(); i++) {
          IChainElement p1Element = p1.getProposedChain().get(i);
          IChainElement p2Element = p2.getProposedChain().get(i);
          switch (p1Element.getElementType()) {
          case LOCAL:
            if (p2Element.getElementType().equals(ChainElementType.FIELD)
                || p2Element.getElementType().equals(ChainElementType.METHOD)) {
              return -1;
            }
            break;
          case FIELD:
            if (p2Element.getElementType().equals(ChainElementType.LOCAL)) {
              return 1;
            } else if (p2Element.getElementType().equals(ChainElementType.METHOD)) {
              return -1;
            }
            break;
          case METHOD:
            if (p2Element.getElementType().equals(ChainElementType.FIELD)
                || p2Element.getElementType().equals(ChainElementType.LOCAL)) {
              return 1;
            }
            break;
          }

          int stringCompare = p1Element.getCompletion().compareTo(p2Element.getCompletion());
          if (stringCompare != 0) {
            return stringCompare;
          }
        }
        return 0;
      }

    }
  }
}

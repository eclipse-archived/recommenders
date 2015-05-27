/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn- initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.recommenders.utils.Logs.log;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.recommenders.completion.rcp.IProposalNameProvider;
import org.eclipse.recommenders.completion.rcp.utils.ProposalUtils;
import org.eclipse.recommenders.internal.completion.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.base.Optional;

public class ProposalNameProvider implements IProposalNameProvider {

    @Override
    public Optional<IMethodName> toMethodName(CompletionProposal coreProposal) {
        Optional<IMethodName> result = ProposalUtils.toMethodName(coreProposal);
        if (!result.isPresent()) {
            log(LogMessages.ERROR_PROPOSAL_MATCHING_FAILED, coreProposal);
        }
        return result;
    }
}

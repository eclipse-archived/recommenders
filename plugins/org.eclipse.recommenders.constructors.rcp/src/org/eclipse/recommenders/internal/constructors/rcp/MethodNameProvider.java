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
package org.eclipse.recommenders.internal.constructors.rcp;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.recommenders.completion.rcp.utils.ProposalUtils;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.base.Optional;

public class MethodNameProvider implements IMethodNameProvider {

    @Override
    public Optional<IMethodName> toMethodName(CompletionProposal coreProposal, LookupEnvironment env) {
        return ProposalUtils.toMethodName(coreProposal, env);
    }
}

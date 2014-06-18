/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.testing.jdt;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;

import com.google.common.collect.Lists;

public final class SimpleCompletionRequestor extends CompletionRequestor {
    public List<CompletionProposal> proposals = Lists.newLinkedList();
    public InternalCompletionContext context;

    public SimpleCompletionRequestor() {
        setRequireExtendedContext(true);
    }

    @Override
    public void accept(final CompletionProposal proposal) {
        proposals.add(proposal);
    }

    @Override
    public void acceptContext(final CompletionContext context) {
        super.acceptContext(context);
        this.context = (InternalCompletionContext) context;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

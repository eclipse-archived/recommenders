/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.jdt.core.CompletionProposal.ANNOTATION_ATTRIBUTE_REF;
import static org.eclipse.jdt.core.CompletionProposal.ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION;
import static org.eclipse.jdt.core.CompletionProposal.ANONYMOUS_CLASS_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.CONSTRUCTOR_INVOCATION;
import static org.eclipse.jdt.core.CompletionProposal.FIELD_IMPORT;
import static org.eclipse.jdt.core.CompletionProposal.FIELD_REF;
import static org.eclipse.jdt.core.CompletionProposal.FIELD_REF_WITH_CASTED_RECEIVER;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_BLOCK_TAG;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_FIELD_REF;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_INLINE_TAG;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_METHOD_REF;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_PARAM_REF;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_TYPE_REF;
import static org.eclipse.jdt.core.CompletionProposal.JAVADOC_VALUE_REF;
import static org.eclipse.jdt.core.CompletionProposal.KEYWORD;
import static org.eclipse.jdt.core.CompletionProposal.LABEL_REF;
import static org.eclipse.jdt.core.CompletionProposal.LOCAL_VARIABLE_REF;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_IMPORT;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_NAME_REFERENCE;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF;
import static org.eclipse.jdt.core.CompletionProposal.METHOD_REF_WITH_CASTED_RECEIVER;
import static org.eclipse.jdt.core.CompletionProposal.PACKAGE_REF;
import static org.eclipse.jdt.core.CompletionProposal.POTENTIAL_METHOD_DECLARATION;
import static org.eclipse.jdt.core.CompletionProposal.TYPE_IMPORT;
import static org.eclipse.jdt.core.CompletionProposal.TYPE_REF;
import static org.eclipse.jdt.core.CompletionProposal.VARIABLE_DECLARATION;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.recommenders.completion.rcp.IProcessableProposal;
import org.eclipse.recommenders.completion.rcp.ProposalProcessorManager;
import org.eclipse.recommenders.completion.rcp.SessionProcessor;

public class BaseRelevanceProcessor extends SessionProcessor {

    @Override
    public void process(IProcessableProposal proposal) throws Exception {
        final CompletionProposal core = proposal.getCoreProposal().orNull();
        if (core == null) return;
        int factor = 1;
        switch (core.getKind()) {
        case CONSTRUCTOR_INVOCATION:
        case ANONYMOUS_CLASS_CONSTRUCTOR_INVOCATION:
            factor = 10;
            break;
        case METHOD_IMPORT:
        case METHOD_DECLARATION:
        case METHOD_NAME_REFERENCE:
        case METHOD_REF:
        case METHOD_REF_WITH_CASTED_RECEIVER:
        case POTENTIAL_METHOD_DECLARATION:
            factor = 9;
            break;
        case LOCAL_VARIABLE_REF:
            factor = 8;
            break;
        case FIELD_IMPORT:
        case FIELD_REF:
        case FIELD_REF_WITH_CASTED_RECEIVER:
            factor = 7;
            break;

        case VARIABLE_DECLARATION:
            factor = 6;
            break;

        case ANONYMOUS_CLASS_DECLARATION:
        case TYPE_IMPORT:
        case TYPE_REF:
            factor = 5;
            break;
        case PACKAGE_REF:
            factor = 4;
            break;
        case ANNOTATION_ATTRIBUTE_REF:
            factor = 3;
            break;

        case LABEL_REF:
            factor = 2;
            break;
        case KEYWORD:
            factor = 1;
            break;
        case JAVADOC_FIELD_REF:
        case JAVADOC_METHOD_REF:
        case JAVADOC_TYPE_REF:
        case JAVADOC_VALUE_REF:
        case JAVADOC_PARAM_REF:
        case JAVADOC_BLOCK_TAG:
        case JAVADOC_INLINE_TAG:
            factor = 1;
            break;
        default:
            factor = 1;
        }
        int score = factor << 13;
        ProposalProcessorManager mgr = proposal.getProposalProcessorManager();
        mgr.addProcessor(new SimpleProposalProcessor(score));
    }
}

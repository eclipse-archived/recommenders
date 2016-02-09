/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon Laffoy - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import static org.eclipse.jface.viewers.StyledString.DECORATIONS_STYLER;
import static org.eclipse.recommenders.completion.rcp.tips.ConfigureContentAssistInformationControl.*;
import static org.eclipse.recommenders.internal.completion.rcp.EnabledCompletionProposal.*;
import static org.eclipse.recommenders.internal.types.rcp.Constants.SESSION_PROCESSOR_ID;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.completion.rcp.tips.AbstractCompletionTipProposal;
import org.eclipse.recommenders.completion.rcp.tips.ConfigureContentAssistInformationControl;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.completion.rcp.EmptyCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.EnabledCompletionProposal;
import org.eclipse.recommenders.internal.types.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("restriction")
public class EnableTypesCompletionProposal extends AbstractCompletionTipProposal {

    private static final List<SessionProcessorDescriptor> NONE = Collections.<SessionProcessorDescriptor>emptyList();

    private static final String URL_ABOUT_ENABLE = X_SESSION_PROCESSOR + SWITCH_ON + SESSION_PROCESSOR_ID;
    private static final String URL_HTTP_MANUAL = "https://www.eclipse.org/recommenders/manual/#completion-engines"; //$NON-NLS-1$

    private static final String INFO = MessageFormat.format(Messages.PROPOSAL_TOOLTIP_ENABLE_TYPES_COMPLETION,
            URL_ABOUT_ENABLE, URL_HTTP_MANUAL);

    /**
     * Don't sort this proposal based on its label, but always show it before all other proposals except
     * {@link EnabledCompletionProposal} (and {@link EmptyCompletionProposal}).
     */
    private static final int RELEVANCE = ENABLE_CODE_COMPLETION_RELEVANCE - RELEVANCE_STEP_SIZE;
    private static final String SORT_STRING = "\u0001";

    private static final int TIME_DELAY_IN_MINUTES = 15;

    private final CompletionRcpPreferences preferences;

    @Inject
    public EnableTypesCompletionProposal(SharedImages images, CompletionRcpPreferences completionPreferences) {
        this.preferences = completionPreferences;

        setImage(images.getImage(Images.OBJ_LIGHTBULB));

        setStyledDisplayString(new StyledString(Messages.PROPOSAL_LABEL_ENABLE_TYPES_COMPLETION, DECORATIONS_STYLER));

        setRelevance(RELEVANCE);

        setSortString(SORT_STRING);

        suppressProposal(TIME_DELAY_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public boolean isApplicable(IRecommendersCompletionContext context) {
        if (!super.isApplicable(context)) {
            return false;
        }

        SessionProcessorDescriptor descriptor = preferences.getSessionProcessorDescriptor(SESSION_PROCESSOR_ID);
        if (descriptor == null) {
            return false;
        }

        if (preferences.isEnabled(descriptor)) {
            return false;
        }

        Set<ITypeName> expectedTypes = context.getExpectedTypeNames();
        if (expectedTypes.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        SessionProcessorDescriptor descriptor = preferences.getSessionProcessorDescriptor(SESSION_PROCESSOR_ID);
        if (descriptor != null) {
            preferences.setSessionProcessorEnabled(ImmutableList.of(descriptor), NONE);
        }
    }

    @Override
    protected IInformationControl createInformationControl(Shell parent, String statusLineText) {
        return new ConfigureContentAssistInformationControl(parent, statusLineText, INFO, preferences);
    }
}

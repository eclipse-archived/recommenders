/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.tips;

import static org.eclipse.jface.viewers.StyledString.DECORATIONS_STYLER;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.tips.AbstractCompletionTipProposal;
import org.eclipse.recommenders.completion.rcp.tips.ConfigureContentAssistInformationControl;
import org.eclipse.recommenders.internal.completion.rcp.l10n.Messages;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.recommenders.rcp.utils.Dialogs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class DiscoveryCompletionProposal extends AbstractCompletionTipProposal {

    private static final String DISCOVERY_URL = "http://download.eclipse.org/recommenders/discovery/2.x/directories/completion.xml"; //$NON-NLS-1$

    private static final int TIME_DELAY_IN_MINUTES = 30;

    @Inject
    public DiscoveryCompletionProposal(SharedImages images) {
        Image image = images.getImage(Images.OBJ_LIGHTBULB);
        setImage(image);
        StyledString text = new StyledString(Messages.PROPOSAL_LABEL_DISCOVER_EXTENSIONS, DECORATIONS_STYLER);
        setStyledDisplayString(text);
        setSortString(text.getString());
        suppressProposal(TIME_DELAY_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        Dialogs.newExtensionsDiscoveryDialog(DISCOVERY_URL).open();
    }

    @Override
    protected IInformationControl createInformationControl(Shell parent, String statusLineText) {
        return new ConfigureContentAssistInformationControl(parent, statusLineText,
                Messages.PROPOSAL_TOOLTIP_DISCOVER_EXTENSIONS, null);
    }
}

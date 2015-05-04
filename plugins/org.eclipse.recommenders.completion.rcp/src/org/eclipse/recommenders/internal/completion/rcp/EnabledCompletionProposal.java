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
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.jdt.internal.ui.JavaPlugin.getActiveWorkbenchShell;
import static org.eclipse.jface.viewers.StyledString.DECORATIONS_STYLER;
import static org.eclipse.recommenders.internal.completion.rcp.Constants.*;
import static org.eclipse.recommenders.rcp.utils.PreferencesHelper.createLinkLabelToPreferencePage;
import static org.eclipse.ui.dialogs.PreferencesUtil.createPreferenceDialogOn;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.DisableContentAssistCategoryJob;
import org.eclipse.recommenders.completion.rcp.EnableContentAssistCategoryJob;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.recommenders.rcp.utils.BrowserUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class EnabledCompletionProposal extends AbstractJavaCompletionProposal {

    private static final Object DUMMY_INFO = new Object();

    private static final String ABOUT_PREFERENCES = "about:preferences"; //$NON-NLS-1$
    private static final String ABOUT_DISABLE = "about:disable"; //$NON-NLS-1$
    private static final String HTTP_HOMEPAGE = "http://www.eclipse.org/recommenders/"; //$NON-NLS-1$
    private static final String PAGE_NAME = createLinkLabelToPreferencePage(COMPLETION_PREFERENCE_PAGE_ID);

    private static final String INFO = MessageFormat.format(Messages.PROPOSAL_TOOLTIP_ENABLED_COMPLETION, PAGE_NAME,
            ABOUT_PREFERENCES, ABOUT_DISABLE, HTTP_HOMEPAGE);

    // leave a bit space for other, maybe more important proposals
    public static final int RELEVANCE_STEP_SIZE = 10000;
    public static final int ENABLE_CODE_COMPLETION_RELEVANCE = Integer.MAX_VALUE - RELEVANCE_STEP_SIZE;

    public EnabledCompletionProposal(SharedImages images, int offset) {
        StyledString text = new StyledString(Messages.PROPOSAL_LABEL_ENABLED_COMPLETION, DECORATIONS_STYLER);
        setStyledDisplayString(text);
        setImage(images.getImage(Images.OBJ_LIGHTBULB));
        setRelevance(ENABLE_CODE_COMPLETION_RELEVANCE);
        setCursorPosition(offset);
        setReplacementString(""); //$NON-NLS-1$
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return DUMMY_INFO;
    }

    @Override
    protected boolean isValidPrefix(String prefix) {
        return true;
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new ConfigureContentAssistInformationControl(parent);
            }
        };
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
    }

    private void openPreferencePage() {
        createPreferenceDialogOn(getActiveWorkbenchShell(), COMPLETION_PREFERENCE_PAGE_ID, null, null).open();
    }

    private final class ConfigureContentAssistInformationControl extends AbstractInformationControl {

        private ConfigureContentAssistInformationControl(Shell parentShell) {
            super(parentShell, Messages.PROPOSAL_CATEGORY_CODE_RECOMMENDERS);
            create();
        }

        @Override
        public boolean hasContents() {
            return true;
        }

        @Override
        protected void createContent(Composite parent) {
            Link link = new Link(parent, SWT.NONE);
            Dialog.applyDialogFont(link);
            link.setForeground(parent.getForeground());
            link.setBackground(parent.getBackground());
            link.setText(INFO);
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    dispose();
                    if (ABOUT_DISABLE.equals(e.text)) {
                        new DisableContentAssistCategoryJob(RECOMMENDERS_ALL_CATEGORY_ID).schedule();
                        new EnableContentAssistCategoryJob(MYLYN_ALL_CATEGORY).schedule();
                        new EnableContentAssistCategoryJob(JDT_ALL_CATEGORY).schedule();
                    } else if (ABOUT_PREFERENCES.equals(e.text)) {
                        openPreferencePage();
                    } else if (HTTP_HOMEPAGE.equals(e.text)) {
                        BrowserUtils.openInExternalBrowser(e.text);
                    }
                }
            });
        }
    }
}

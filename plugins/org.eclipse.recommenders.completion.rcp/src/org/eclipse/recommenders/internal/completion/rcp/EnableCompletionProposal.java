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

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.completion.rcp.DisableContentAssistCategoryJob;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

@SuppressWarnings("restriction")
public class EnableCompletionProposal extends AbstractJavaCompletionProposal {

    private static final Object DUMMY_INFO = new Object();
    private static final String HERE = "here";
    private static final String HOMEPAGE = "project homepage";
    private static final String PAGE_NAME = "Preferences > "
            + createLinkLabelToPreferencePage(COMPLETION_PREFERENCE_PAGE_ID);

    private static final String INFO = "Code Recommenders is available in your Eclipse installation. "
            + "If you want to enable Code Recommenders to get a smarter content assist, simply press return.\n\n"
            + "To configure Code Recommenders first, visit <a>" + PAGE_NAME + "</a>.\n\n"
            + "To disable Code Recommenders click <a>" + HERE
            + "</a>. To learn more about Code Recommenders please visit the <a>" + HOMEPAGE + "</a>.";

    // leave a bit space for other, maybe more important proposals
    private static final int RELEVANCE = Integer.MAX_VALUE - 10000;

    public EnableCompletionProposal(SharedImages images, int offset) {
        Image image = images.getImage(Images.OBJ_LIGHTBULB);
        StyledString text = new StyledString("Enable intelligent code completion?", DECORATIONS_STYLER);
        setStyledDisplayString(text);
        setImage(image);
        setRelevance(RELEVANCE);
        setCursorPosition(offset);
        setReplacementString("");
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return DUMMY_INFO;
    }

    @Override
    protected boolean isPrefix(String prefix, String string) {
        return true;
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new ConfigureContentAssistInformationControl(parent, "Eclipse Code Recommenders");
            }
        };
    }

    /**
     * Disables Mylyn and JDT content assists.
     */
    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
        new DisableContentAssistCategoryJob(MYLYN_ALL_CATEGORY).schedule();
        new DisableContentAssistCategoryJob(JDT_ALL_CATEGORY).schedule();
    }

    private final class ConfigureContentAssistInformationControl extends AbstractInformationControl {

        private ConfigureContentAssistInformationControl(Shell parentShell, String statusFieldText) {
            super(parentShell, statusFieldText);
            create();
        }

        @Override
        public boolean hasContents() {
            return true;
        }

        @Override
        protected void createContent(Composite parent) {
            Link link = new Link(parent, SWT.NONE);
            link.setBackground(parent.getBackground());
            link.setText(INFO);

            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    dispose();
                    if (HERE.equals(e.text)) {
                        diableCodeRecommenders();
                    } else if (PAGE_NAME.equals(e.text)) {
                        openPreferencePage();
                    } else if (HOMEPAGE.equals(e.text)) {
                        openHomepageInBrowser();
                    }
                }

                private void diableCodeRecommenders() {
                    new DisableContentAssistCategoryJob(RECOMMENDERS_ALL_CATEGORY_ID).schedule();
                }

                private void openPreferencePage() {
                    createPreferenceDialogOn(getActiveWorkbenchShell(), COMPLETION_PREFERENCE_PAGE_ID, null, null)
                            .open();
                }

                private void openHomepageInBrowser() {
                    try {
                        IWebBrowser browser = PlatformUI
                                .getWorkbench()
                                .getBrowserSupport()
                                .createBrowser(SWT.NONE, "recommenders-homepage", "Code Recommenders",
                                        "Learn about Code Recommenders");
                        browser.openURL(new URL("http://eclipse.org/recommenders"));
                    } catch (Exception e1) {
                    }
                }
            });
        }
    }
}

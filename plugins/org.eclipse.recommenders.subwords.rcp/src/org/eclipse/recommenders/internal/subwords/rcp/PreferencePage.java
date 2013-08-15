/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Olav Lenz - externalize Strings.
 */
package org.eclipse.recommenders.internal.subwords.rcp;

import static org.eclipse.recommenders.internal.subwords.rcp.SubwordsUtils.*;

import java.util.Set;

import org.eclipse.recommenders.rcp.utils.ContentAssistEnablementBlock;
import org.eclipse.recommenders.rcp.utils.PreferencesHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage {

    private static final String PREFPAGE_ID_CODE_ASSIST_ADVANCED = "org.eclipse.jdt.ui.preferences.CodeAssistPreferenceAdvanced"; //$NON-NLS-1$
    private ContentAssistEnablementBlock enablement;

    public PreferencePage() {
        setDescription(Messages.PREFPAGE_INTRO);
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        final Link link = new Link(container, SWT.NONE | SWT.WRAP);
        link.setText(String.format(Messages.PREFPAGE_SEE_LINK_TO_CONTENT_ASSIST,
                "<a>'" + PreferencesHelper.createLinkLabelToPreferencePage(PREFPAGE_ID_CODE_ASSIST_ADVANCED) + "'</a>")); //$NON-NLS-1$ //$NON-NLS-2$
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(), PREFPAGE_ID_CODE_ASSIST_ADVANCED, null, null);
            }
        });

        enablement = new ContentAssistEnablementBlock(container, Messages.PREFPAGE_ENABLE_PROPOSALS,
                SubwordsCompletionProposalComputer.CATEGORY_ID) {

            @Override
            protected void additionalExcludedCompletionCategoriesUpdates(final boolean isEnabled, final Set<String> cats) {
                if (isEnabled) {
                    // enable subwords - disable mylyn and jdt
                    cats.add(JDT_ALL_CATEGORY);
                    cats.add(MYLYN_ALL_CATEGORY);
                } else {
                    // disable subwords - enable jdt -- or mylyn if installed.
                    if (isMylynInstalled()) {
                        cats.remove(MYLYN_ALL_CATEGORY);
                    } else {
                        cats.remove(JDT_ALL_CATEGORY);
                    }
                }
            }
        };

        return container;
    }

    @Override
    public void init(final IWorkbench workbench) {
    }

    @Override
    public void setVisible(final boolean visible) {
        // respond to changes in Java > Editor > Content Assist > Advanced:
        // this works only one-way. We respond to changes made in JDT but JDT page may show deprecated values.
        enablement.loadSelection();
        super.setVisible(visible);
    }

}

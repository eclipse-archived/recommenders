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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.JDT_ALL_CATEGORY;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.MYLYN_ALL_CATEGORY;
import static org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsUtils.isMylynInstalled;

import java.util.Set;

import org.eclipse.recommenders.utils.rcp.internal.ContentAssistEnablementBlock;
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

    private ContentAssistEnablementBlock enablement;

    public PreferencePage() {
        setDescription("Subwords is a new experimental content assist for Java. It uses 'fuzzy word matching' which allows you to specify just a subsequence of the proposal's text you want to insert.\n\n"
                + "Note that Subwords essentially makes the same proposals as the standard Java content assist, and thus, will automatically disabled itself when either JDT or Mylyn completion is active to avoid duplicated proposals. "
                + "The button below is a shortcut for enabling Subwords and disabling standard Java and Mylyn content assist (and reverse).");
    }

    @Override
    protected Control createContents(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        final Link link = new Link(container, SWT.NONE | SWT.WRAP);
        link.setText("See <a>'Java > Editor > Content Assist > Advanced'</a> to configure content assist directly.");
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                PreferencesUtil.createPreferenceDialogOn(getShell(),
                        "org.eclipse.jdt.ui.preferences.CodeAssistPreferenceAdvanced", null, null);
            }
        });

        enablement = new ContentAssistEnablementBlock(container, "Enable Java Subwords Proposals.",
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

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

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.collect.Sets;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage {

    static final String JDT_ALL_CATEGORY = "org.eclipse.jdt.ui.javaAllProposalCategory";
    static final String MYLYN_ALL_CATEGORY = "org.eclipse.mylyn.java.ui.javaAllProposalCategory";
    private Button enablement;

    public PreferencePage() {
        setDescription("Subwords is a new experimental content assist for Java. It uses 'fuzzy word matching' which allows you to specify just a subsequence of the proposal's text you want to insert.\n\n"
                + "Note that Subwords essentially makes the same proposals as the standard Java content assist, and thus, will automatically disabled itself when either JDT or Mylyn completion is active to avoid duplicated proposals. "
                + "The button below is a shortcut for enabling Subwords and disabling standard Java and Mylyn content assist. Disabling Subwords here automatically enabled standard Java content assist (but not Mylyn)\n\n"
                + "For full control of which content assistants should contribute to the default content assist tab check the advanced preference page found under 'Java > Editor > Content Assist > Advanced > default'");
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        enablement = new Button(container, SWT.CHECK);
        enablement.setText("Enable Java Subwords Proposals.");
        enablement.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
                if (enablement.getSelection()) {
                    // enable subwords - disable mylyn and jdt
                    cats.remove(SubwordsCompletionProposalComputer.CATEGORY_ID);
                    cats.add(JDT_ALL_CATEGORY);
                    cats.add(MYLYN_ALL_CATEGORY);
                } else {
                    // disable subwords - enable jdt -- or mylyn if installed.
                    cats.add(SubwordsCompletionProposalComputer.CATEGORY_ID);
                    if (isMylynInstalled()) {
                        cats.remove(MYLYN_ALL_CATEGORY);
                    } else {
                        cats.remove(JDT_ALL_CATEGORY);
                    }
                }
                PreferenceConstants.setExcludedCompletionProposalCategories(cats.toArray(new String[cats.size()]));
            }

            private boolean isMylynInstalled() {
                CompletionProposalComputerRegistry reg = CompletionProposalComputerRegistry.getDefault();
                for (CompletionProposalCategory cat : reg.getProposalCategories()) {
                    if (cat.getId().equals(MYLYN_ALL_CATEGORY)) {
                        return true;
                    }
                }
                return false;
            }

        });

        return container;
    }

    @Override
    public void setVisible(boolean visible) {
        // respond to changes in Java > Editor > Content Assist > Advanced:
        // this works only one-way. We respond to changes made in JDT but JDT page may show deprecated values.
        enablement.setSelection(isSubwordsEnabled());
        super.setVisible(visible);
    }

    private boolean isSubwordsEnabled() {
        String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
        return !ArrayUtils.contains(excluded, SubwordsCompletionProposalComputer.CATEGORY_ID);
    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
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
package org.eclipse.recommenders.rcp.utils;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Sets;

public class ContentAssistEnablementBlock {

    public static final String JDT_ALL_CATEGORY = "org.eclipse.jdt.ui.javaAllProposalCategory"; //$NON-NLS-1$
    public static final String MYLYN_ALL_CATEGORY = "org.eclipse.mylyn.java.ui.javaAllProposalCategory"; //$NON-NLS-1$

    protected final String categoryId;
    protected final Button enablement;

    public ContentAssistEnablementBlock(final Composite parent, final String label, final String categoryId) {
        this.categoryId = categoryId;
        enablement = new Button(parent, SWT.CHECK);
        enablement.setText(label);
        enablement.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
                if (enablement.getSelection()) {
                    cats.remove(categoryId);
                } else {
                    cats.add(categoryId);
                }
                additionalExcludedCompletionCategoriesUpdates(enablement.getSelection(), cats);
                PreferenceConstants.setExcludedCompletionProposalCategories(cats.toArray(new String[cats.size()]));
            }
        });
    }

    protected void additionalExcludedCompletionCategoriesUpdates(final boolean isEnabled, final Set<String> cats) {
    };

    public void loadSelection() {
        final String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
        final boolean isFeatureEnabled = !ArrayUtils.contains(excluded, categoryId);
        enablement.setSelection(isFeatureEnabled);
    }

    public static boolean isMylynInstalled() {
        CompletionProposalComputerRegistry reg = CompletionProposalComputerRegistry.getDefault();
        for (CompletionProposalCategory cat : reg.getProposalCategories()) {
            if (cat.getId().equals(MYLYN_ALL_CATEGORY)) {
                return true;
            }
        }
        return false;
    }
}

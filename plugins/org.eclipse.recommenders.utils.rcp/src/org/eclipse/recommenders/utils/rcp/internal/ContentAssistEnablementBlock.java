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
package org.eclipse.recommenders.utils.rcp.internal;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Sets;

public class ContentAssistEnablementBlock {

    private final String categoryId;
    private final Button enablement;

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

}

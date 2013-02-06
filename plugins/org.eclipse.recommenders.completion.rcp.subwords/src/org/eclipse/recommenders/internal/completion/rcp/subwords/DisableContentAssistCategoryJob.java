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
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.recommenders.completion.rcp.subwords.l10n.Messages;
import org.eclipse.ui.progress.UIJob;

import com.google.common.collect.Sets;

/**
 * Disables a content assist category on default (i.e., first) content assist list.
 * <p>
 * The update operation is performed on the UI thread to ensure that no running content assist session is interrupted.
 */
public class DisableContentAssistCategoryJob extends UIJob {

    private String categoryId;

    /**
     * 
     * @param categoryId
     *            the content assist category as specified in plugin.xml.
     */
    public DisableContentAssistCategoryJob(String categoryId) {
        super(String.format(Messages.JOB_DISABLING, categoryId));
        this.categoryId = categoryId;
        setSystem(true);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        String[] excluded = PreferenceConstants.getExcludedCompletionProposalCategories();
        Set<String> cats = Sets.newHashSet(excluded);
        cats.add(categoryId);
        String[] newExcluded = cats.toArray(new String[cats.size()]);
        PreferenceConstants.setExcludedCompletionProposalCategories(newExcluded);
        return Status.OK_STATUS;
    }
}

/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation. Origin: Mylyn.
 */
package org.eclipse.recommenders.internal.completion.rcp.subwords;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class JavaUiUtil {

    static final String SEPARATOR_CODEASSIST = "\0"; //$NON-NLS-1$

    public static final String ASSIST_JDT_ALL = "org.eclipse.jdt.ui.javaAllProposalCategory"; //$NON-NLS-1$

    public static final String ASSIST_JDT_TYPE = "org.eclipse.jdt.ui.javaTypeProposalCategory"; //$NON-NLS-1$

    public static final String ASSIST_JDT_NOTYPE = "org.eclipse.jdt.ui.javaNoTypeProposalCategory"; //$NON-NLS-1$

    public static boolean isDefaultAssistActive(String computerId) {
        if (JavaUiUtil.ASSIST_JDT_ALL.equals(computerId)) {
            CompletionProposalCategory category = getProposalCategory(computerId);
            return (category != null) ? category.isEnabled() && category.isIncluded() : false;
        }
        Set<String> disabledIds = getDisabledIds(JavaPlugin.getDefault().getPreferenceStore());
        return !disabledIds.contains(computerId);
    }

    public static CompletionProposalCategory getProposalCategory(String computerId) {
        List<?> computers = CompletionProposalComputerRegistry.getDefault().getProposalCategories();
        for (Object object : computers) {
            CompletionProposalCategory proposalCategory = (CompletionProposalCategory) object;
            if (computerId.equals((proposalCategory).getId())) {
                return proposalCategory;
            }
        }
        return null;
    }

    public static Set<String> getDisabledIds(IPreferenceStore javaPrefs) {
        String oldValue = javaPrefs.getString(PreferenceConstants.CODEASSIST_EXCLUDED_CATEGORIES);
        StringTokenizer tokenizer = new StringTokenizer(oldValue, SEPARATOR_CODEASSIST);
        Set<String> disabledIds = new HashSet<String>();
        while (tokenizer.hasMoreTokens()) {
            disabledIds.add((String) tokenizer.nextElement());
        }
        return disabledIds;
    }

}

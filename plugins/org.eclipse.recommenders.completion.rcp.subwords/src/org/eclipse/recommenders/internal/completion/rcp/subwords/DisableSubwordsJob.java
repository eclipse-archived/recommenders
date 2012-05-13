package org.eclipse.recommenders.internal.completion.rcp.subwords;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.progress.UIJob;

import com.google.common.collect.Sets;

public class DisableSubwordsJob extends UIJob {

    public DisableSubwordsJob() {
        super("Disabling subwords");
        setSystem(true);
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        Set<String> cats = Sets.newHashSet(PreferenceConstants.getExcludedCompletionProposalCategories());
        cats.add(SubwordsCompletionProposalComputer.CATEGORY_ID);
        PreferenceConstants.setExcludedCompletionProposalCategories(cats.toArray(new String[cats.size()]));
        return Status.OK_STATUS;
    }

}

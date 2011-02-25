/**
 * Copyright (c) 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codesearch.jobs;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.recommenders.commons.codesearch.ICodeSearchResource;
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.commons.codesearch.FeedbackType;
import org.eclipse.recommenders.commons.codesearch.Proposal;
import org.eclipse.recommenders.commons.codesearch.Request;

public class SendUserClickFeedbackJob extends WorkspaceJob {
    private final Proposal hit;
    private final Request request;

    public SendUserClickFeedbackJob(final Request request, final Proposal hit) {
        super("Sending user click-through feedback");
        this.request = request;
        this.hit = hit;
        setUser(false);
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        final Feedback feedback = Feedback.create(hit.source, request.uniqueRequestId, FeedbackType.EDITOR_OPENED);
        final ICodeSearchResource service = SendCodeSearchRequestJob.createTransport();
        service.addFeedback(feedback);
        return Status.OK_STATUS;
    }
}

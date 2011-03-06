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
import org.eclipse.recommenders.commons.codesearch.Feedback;
import org.eclipse.recommenders.internal.rcp.codesearch.client.CodeSearchClient;

public class SendUserClickFeedbackJob extends WorkspaceJob {

    private final String requestId;
    private final Feedback feedback;
    private final CodeSearchClient client;

    public SendUserClickFeedbackJob(final String requestId, final Feedback feedback, final CodeSearchClient client) {
        super("Sending user click-through feedback");
        this.requestId = requestId;
        this.feedback = feedback;
        this.client = client;
        setUser(false);
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
        client.addFeedback(requestId, feedback);
        return Status.OK_STATUS;
    }
}

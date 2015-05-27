/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.recommenders.coordinates.DependencyInfo;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.google.common.collect.ImmutableSet;

public class ContextLoadingProposal extends Job implements ICompletionProposal, ICompletionProposalExtension {

    private final IProjectCoordinateProvider pcProvider;
    private final ImmutableSet<DependencyInfo> dependencies;
    private final Image image;

    private boolean resolutionJobDone = false;

    public ContextLoadingProposal(IProjectCoordinateProvider pcProvider, ImmutableSet<DependencyInfo> dependencies,
            Image image) {
        super(Messages.JOB_NAME_IDENTIFYING_PROJECT_DEPENDENCIES);
        this.pcProvider = pcProvider;
        this.dependencies = dependencies;
        this.image = image;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        for (DependencyInfo dependencyInfo : dependencies) {
            pcProvider.resolve(dependencyInfo);
        }
        resolutionJobDone = true;
        return Status.OK_STATUS;
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        // Do nothing
    }

    @Override
    public boolean isValidFor(IDocument document, int offset) {
        return false;
    }

    @Override
    public char[] getTriggerCharacters() {
        return null;
    }

    @Override
    public int getContextInformationPosition() {
        return -1;
    }

    @Override
    public void apply(IDocument document) {
        // Do nothing
    }

    @Override
    public Point getSelection(IDocument document) {
        return null;
    }

    @Override
    public String getDisplayString() {
        return Messages.PROPOSAL_LABEL_IDENTIFYING_PROJECT_DEPENDENCIES;
    }

    @Override
    public String getAdditionalProposalInfo() {
        return Messages.PROPOSAL_INFO_IDENTIFYING_PROJECT_DEPENDENCIES;
    }

    @Override
    public Image getImage() {
        return image;
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    public boolean isStillLoading() {
        return !resolutionJobDone;
    }
}

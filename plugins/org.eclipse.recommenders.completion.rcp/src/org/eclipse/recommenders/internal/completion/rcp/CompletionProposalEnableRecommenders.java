/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.analysis.rcp.RecommendersNature;
import org.eclipse.recommenders.internal.rcp.IDs;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

@SuppressWarnings("restriction")
public class CompletionProposalEnableRecommenders extends JavaCompletionProposal {

    private static Image image = loadImageOnce();

    private final IProject project;

    public CompletionProposalEnableRecommenders(final IProject project, final int invocationOffsetInEditor) {
        super("", invocationOffsetInEditor, 0, null,
                "Intelligent Code Completion not enabled for this project. Fix that?", 100);
        this.project = project;
    }

    @Override
    public void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
        RecommendersNature.addNature(project);
    }

    @Override
    public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
        return "Adds the Code Recommenders nature and its associated builder to this project.";
    }

    @Override
    public Image getImage() {
        return image;
    }

    private static Image loadImageOnce() {
        final Bundle bundle = FrameworkUtil.getBundle(CompletionProposalEnableRecommenders.class);
        final ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(bundle.getSymbolicName(),
                IDs.IMG_PATH_SLICE);
        return descriptor.createImage();
    }
}

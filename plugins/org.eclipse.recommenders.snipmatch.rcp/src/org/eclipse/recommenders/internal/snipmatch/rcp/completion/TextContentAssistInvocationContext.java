/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn, Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.recommenders.utils.Nullable;

import com.google.common.base.Optional;

public class TextContentAssistInvocationContext extends ContentAssistInvocationContext {

    private final IJavaProject javaProject;

    public TextContentAssistInvocationContext(ITextViewer viewer, int offset, @Nullable IJavaProject javaProject) {
        super(viewer, offset);
        this.javaProject = javaProject;
    }

    public Optional<IJavaProject> getProject() {
        return Optional.fromNullable(javaProject);
    }
}

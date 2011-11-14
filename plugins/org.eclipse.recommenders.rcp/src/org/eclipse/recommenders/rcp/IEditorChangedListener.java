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
package org.eclipse.recommenders.rcp;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.recommenders.utils.annotations.Provisional;

@Provisional
public interface IEditorChangedListener {
    public NotificationPriority getNotifcationPriority();

    void editorVisble(IEditorDashboard dashboard, IProgressMonitor monitor) throws CoreException;

    void editorHidden(IEditorDashboard dashboard, IProgressMonitor monitor) throws CoreException;

    void editorOpened(IEditorDashboard dashboard, IProgressMonitor monitor) throws CoreException;

    void editorClosed(IEditorDashboard dashboard, IProgressMonitor monitor) throws CoreException;

    void editorDeactivated(IEditorDashboard dashboard, IProgressMonitor monitor) throws CoreException;

    void editorActivated(IEditorDashboard dashboard, IProgressMonitor monitor) throws CoreException;
}

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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;

/**
 * {@link IArtifactStoreChangedListener} are called by the artifact store
 * whenever new artifacts have been registered.
 */
@Provisional
public interface IArtifactStoreChangedListener {
    public NotificationPriority getNotifcationPriority();

    /**
     * Called by the artifact store whenever the one or more artifacts for the
     * given compilation unit changed. This method is called in a background
     * job.
     */
    void unitChanged(ICompilationUnit cu, IProgressMonitor monitor) throws CoreException;
}

/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package scenarios;

import org.eclipse.core.runtime.IProgressMonitor;

public class UseProgressMonitorDone {

    public void useProgressMonitor(final IProgressMonitor monitor) {
        monitor.beginTask("Performing task: ", 10);
        monitor.subTask("step");
        monitor.worked(1);
    }
}

/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils.rcp;

import org.eclipse.core.runtime.IProgressMonitor;

public final class ConsoleProgressMonitor implements IProgressMonitor {
    @Override
    public void worked(int work) {
    }

    @Override
    public void subTask(String name) {
        System.out.println(name);
    }

    @Override
    public void setTaskName(String name) {
    }

    @Override
    public void setCanceled(boolean value) {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void internalWorked(double work) {
    }

    @Override
    public void done() {
    }

    @Override
    public void beginTask(String name, int totalWork) {
        System.out.println(name);
    }
}
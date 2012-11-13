/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rdk.wiring;

import org.eclipse.recommenders.rdk.index.IndexCommands;
import org.eclipse.recommenders.rdk.utils.Commands;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        Object[] cmds = { new IndexCommands() };
        for (Object cmd : cmds)
            Commands.registerAnnotatedCommand(context, cmd);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}

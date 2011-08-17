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
package org.eclipse.recommenders.internal.rcp;

import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.ui.IStartup;

/**
 * This class belongs to the Eclipse startup extension of the code recommenders
 * plugin. This startup extension ensures that all code recommenders related
 * plugins are started as soon as possible but after the Eclipse UI has been
 * started.
 */
public class EclipseStartupHook implements IStartup {
    /**
     * This method gets called by Eclipse after the plugin's activator.start
     * method. Without this method/class, however, this bundle wouldn't be
     * activated... :-) Anyway, all important work takes place in
     * {@link RecommendersPlugin#start}.
     */
    @Override
    public void earlyStartup() {
        // XXX: initialize the injection service. This may need an too early
        // startup of code recommenders but this seems acceptable due to
        // parallelization issues.
        InjectionService.getInstance().getInjector();
    }
}

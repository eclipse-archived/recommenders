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
package org.eclipse.recommenders.internal.apidocs.rcp;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ApidocsRcpPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.recommenders.apidocs.rcp"; //$NON-NLS-1$
    private static ApidocsRcpPlugin INSTANCE;

    public static ApidocsRcpPlugin getDefault() {
        return INSTANCE;
    }

    @Override
    public void start(final org.osgi.framework.BundleContext context) throws Exception {
        super.start(context);
        INSTANCE = this;
    };

    @Override
    public void stop(BundleContext context) throws Exception {
        INSTANCE = null;
        super.stop(context);
    }
}

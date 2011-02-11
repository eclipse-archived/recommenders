/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public final class TemplatesPlugin extends AbstractUIPlugin {

    private static TemplatesPlugin plugin;

    /**
     * Gets the <code>TemplatesPlugin</code>'s default instance.
     * 
     * @return The default instance of the <code>Plugin</code>.
     */
    public static TemplatesPlugin getDefault() {
        return TemplatesPlugin.plugin;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        TemplatesPlugin.plugin = this;
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        TemplatesPlugin.plugin = null;
        super.stop(context);
    }
}

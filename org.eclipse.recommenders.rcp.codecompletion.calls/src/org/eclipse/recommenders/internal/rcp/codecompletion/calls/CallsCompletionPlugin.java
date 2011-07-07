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
package org.eclipse.recommenders.internal.rcp.codecompletion.calls;

import org.eclipse.recommenders.commons.injection.InjectionService;
import org.eclipse.recommenders.internal.rcp.codecompletion.calls.store.FragmentIndex;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Injector;

public class CallsCompletionPlugin extends AbstractUIPlugin {
    private static CallsCompletionPlugin plugin;

    public static CallsCompletionPlugin getDefault() {
        return plugin;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        new ModelStoreInitializerJob().schedule(1000);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        storeFragmentIndex();
        super.stop(context);
    }

    private void storeFragmentIndex() {
        final Injector injector = InjectionService.getInstance().getInjector();
        final FragmentIndex fragmentIndex = injector.getInstance(FragmentIndex.class);
        fragmentIndex.store();
    }
}

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
package org.eclipse.recommenders.internal.extdoc.rcp.wiring;

import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ManualModelStoreWiring.ClassOverridesModelStore;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ManualModelStoreWiring.ClassOverridesPatternsModelStore;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ManualModelStoreWiring.ClassSelfcallsModelStore;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ManualModelStoreWiring.MethodSelfcallsModelStore;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Injector;

public class ExtdocPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.recommenders.extdoc.rcp";
    private static ExtdocPlugin INSTANCE;

    public static ExtdocPlugin getDefault() {
        return INSTANCE;
    }

    @Override
    public void start(final org.osgi.framework.BundleContext context) throws Exception {
        super.start(context);
        INSTANCE = this;
    };

    @Override
    @SuppressWarnings("rawtypes")
    public void stop(BundleContext context) throws Exception {
        INSTANCE = null;
        Injector i = InjectionService.getInstance().getInjector();
        IModelArchiveStore[] stores = { i.getInstance(ClassOverridesPatternsModelStore.class),
                i.getInstance(ClassOverridesModelStore.class), i.getInstance(ClassSelfcallsModelStore.class),
                i.getInstance(MethodSelfcallsModelStore.class) };
        for (IModelArchiveStore<?, ?> store : stores) {
            try {
                store.close();
            } catch (Exception e) {
            }
        }
        super.stop(context);
    }
}

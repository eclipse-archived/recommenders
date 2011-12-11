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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("rawtypes")
public class AdapterFactory implements IAdapterFactory {
    private class OverridesWorkbenchAdapter extends WorkbenchAdapter {
        final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.recommenders.rcp.codecompletion.overrides", "/icons/obj16/overrides.png");

        @Override
        public String getLabel(final Object object) {
            final OverridesRecommendation recommendation = cast(object);
            return Names.vm2srcSimpleMethod(recommendation.method);
        }

        @SuppressWarnings("unchecked")
        private <T> T cast(final Object object) {
            return (T) object;
        }

        @Override
        public ImageDescriptor getImageDescriptor(final Object object) {
            return desc;
        }
    };

    private final OverridesWorkbenchAdapter adapter = new OverridesWorkbenchAdapter();

    @Override
    public Object getAdapter(final Object adaptableObject, final Class adapterType) {
        // one for all:
        return adapter;
    }

    @Override
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }
}

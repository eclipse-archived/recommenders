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
package org.eclipse.recommenders.internal.completion.rcp.calls;

import static org.eclipse.recommenders.utils.Checks.cast;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.utils.Names;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("rawtypes")
public class CallsCompletionAdapterFactory implements IAdapterFactory {

    private final class CallsRecommendationWorkbenchAdapter extends WorkbenchAdapter {

        final ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.recommenders.rcp.codecompletion.calls", "/icons/obj16/calls.png");

        @Override
        public String getLabel(final Object object) {
            final CallsRecommendation recommendation = cast(object);
            final StringBuilder sb = new StringBuilder();
            if (recommendation.method.isInit()) {
                sb.append("new ");
            }
            sb.append(Names.vm2srcSimpleMethod(recommendation.method));
            return sb.toString();
        }

        @Override
        public ImageDescriptor getImageDescriptor(final Object object) {
            return desc;
        }
    }

    private final CallsRecommendationWorkbenchAdapter adapter = new CallsRecommendationWorkbenchAdapter();

    @Override
    public Object getAdapter(final Object adaptableObject, final Class adapterType) {
        return adapter;
    }

    @Override
    public Class[] getAdapterList() {
        return new Class[] { IWorkbenchAdapter.class };
    }
}

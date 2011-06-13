/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public abstract class AbstractProvider implements IProvider {

    private String providerName;
    private String providerFullName;
    private Image providerIcon;

    @Override
    public final void setInitializationData(final IConfigurationElement config, final String propertyName,
            final Object data) throws CoreException {
        providerName = Preconditions.checkNotNull(config.getAttribute("short_name"));
        providerFullName = Preconditions.checkNotNull(config.getAttribute("long_name"));

        final String pluginId = config.getNamespaceIdentifier();
        final String icon = config.getAttribute("icon");
        final ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, icon);
        providerIcon = Preconditions.checkNotNull(imageDescriptor, pluginId + " / " + icon).createImage();
    }

    @Override
    public final String getProviderName() {
        return providerName;
    }

    @Override
    public final String getProviderFullName() {
        return providerFullName;
    }

    @Override
    public final Image getIcon() {
        return providerIcon;
    }

}

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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Preconditions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Supports instantiation of a provider from an extension declaration, allowing
 * access to configuration parameters.
 */
abstract class AbstractProvider implements IProvider {

    private static final BundleContext BUNDLECONTEXT = ExtDocPlugin.getDefault().getBundle().getBundleContext();

    private String providerName;
    private String providerFullName;
    private Image providerIcon;

    @Override
    public final void setInitializationData(final IConfigurationElement config, final String propertyName,
            final Object data) throws CoreException {
        providerName = Preconditions.checkNotNull(config.getAttribute("short_name"));
        providerFullName = Preconditions.checkNotNull(config.getAttribute("long_name"));
        setProviderIcon(config);
    }

    @Override
    public String getProviderName() {
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

    private void setProviderIcon(final IConfigurationElement config) {
        final long declaringBundleId = Long.parseLong(((RegistryContributor) config.getContributor()).getActualId());
        final Bundle bundle = BUNDLECONTEXT.getBundle(declaringBundleId);
        final String icon = config.getAttribute("icon");
        providerIcon = ImageDescriptor.createFromURL(bundle.getEntry(icon)).createImage();
    }

    @Override
    public final boolean equals(final Object object) {
        return object instanceof IProvider && hashCode() == object.hashCode();
    }

    @Override
    public final int hashCode() {
        return getClass().getName().hashCode();
    }

}

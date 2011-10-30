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

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Preconditions;

/**
 * Supports instantiation of a provider from an extension declaration, allowing
 * access to configuration parameters.
 */
abstract class AbstractProvider implements IProvider {

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
        final URL icon = ExtDocPlugin.getBundleEntry(declaringBundleId, config.getAttribute("icon"));
        providerIcon = ImageDescriptor.createFromURL(icon).createImage();
    }

    @Override
    public boolean ignoreTimeout() {
        return false;
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

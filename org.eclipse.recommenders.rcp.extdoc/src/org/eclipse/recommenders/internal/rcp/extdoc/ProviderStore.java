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
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.rcp.extdoc.IProvider;

public final class ProviderStore {

    private final Map<String, IProvider> providers = new HashMap<String, IProvider>();

    public ProviderStore() throws CoreException {
        final IExtensionRegistry reg = Platform.getExtensionRegistry();
        for (final IConfigurationElement element : reg
                .getConfigurationElementsFor("org.eclipse.recommenders.rcp.extdoc.provider")) {
            final IProvider provider = (IProvider) element.createExecutableExtension("class");
            providers.put(element.getAttribute("short_name"), provider);
        }
    }

    public Map<String, IProvider> getProviders() {
        return providers;
    }

    public IProvider getProvider(final String name) {
        return providers.get(name);
    }

}

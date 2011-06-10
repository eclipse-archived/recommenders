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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.rcp.extdoc.IProvider;

public final class ProviderStore {

    private final Map<String, IProvider> providers = new HashMap<String, IProvider>();
    private final Map<IProvider, Integer> priorities = new HashMap<IProvider, Integer>();

    public ProviderStore() throws CoreException {
        final IExtensionRegistry reg = Platform.getExtensionRegistry();
        for (final IConfigurationElement element : reg
                .getConfigurationElementsFor("org.eclipse.recommenders.rcp.extdoc.provider")) {
            final IProvider provider = (IProvider) element.createExecutableExtension("class");
            providers.put(element.getAttribute("short_name"), provider);
            priorities.put(provider, Integer.parseInt(element.getAttribute("priority")));
        }
    }

    public Set<Tuple<String, IProvider>> getProviders() {
        final Set<Tuple<String, IProvider>> set = new TreeSet<Tuple<String, IProvider>>(
                new Comparator<Tuple<String, IProvider>>() {
                    @Override
                    public int compare(final Tuple<String, IProvider> provider1,
                            final Tuple<String, IProvider> provider2) {
                        return priorities.get(provider2.getSecond()).compareTo(priorities.get(provider1.getSecond()));
                    }
                });
        for (final Entry<String, IProvider> provider : providers.entrySet()) {
            set.add(Tuple.create(provider.getKey(), provider.getValue()));
        }
        return set;
    }

}

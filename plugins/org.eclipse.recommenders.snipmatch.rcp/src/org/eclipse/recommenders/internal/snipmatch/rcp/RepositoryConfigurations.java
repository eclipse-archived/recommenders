/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static com.google.common.base.Optional.*;

import java.util.Collection;
import java.util.List;

import org.eclipse.recommenders.snipmatch.ISnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.ISnippetRepositoryProvider;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class RepositoryConfigurations {

    public static final String INNER_SEPARATOR = ":"; //$NON-NLS-1$
    public static final String OUTER_SEPARATOR = ";\n"; //$NON-NLS-1$

    public static ImmutableList<ISnippetRepositoryConfiguration> fromPreferenceString(String newValue,
            Collection<ISnippetRepositoryProvider> providers) {
        List<ISnippetRepositoryConfiguration> result = Lists.newArrayList();

        Iterable<String> configurations = Splitter.on(OUTER_SEPARATOR).omitEmptyStrings().split(newValue);

        for (String config : configurations) {
            int indexOfSeparator = config.indexOf(INNER_SEPARATOR);
            String identifier = config.substring(0, indexOfSeparator);
            String content = config.substring(indexOfSeparator + 1, config.length());
            ISnippetRepositoryConfiguration repo = createConfiguration(identifier, content, providers).orNull();
            if (repo != null) {
                result.add(repo);
            }
        }

        return ImmutableList.copyOf(result);
    }

    private static Optional<ISnippetRepositoryConfiguration> createConfiguration(String identifier, String json,
            Collection<ISnippetRepositoryProvider> providers) {
        ISnippetRepositoryProvider provider = findMatchingProvider(identifier, providers).orNull();
        if (provider != null) {
            return of(provider.fromPreferenceString(json));
        }
        return absent();
    }

    public static String toPreferenceString(Collection<ISnippetRepositoryConfiguration> configurations,
            Collection<ISnippetRepositoryProvider> providers) {
        StringBuilder sb = new StringBuilder();
        for (ISnippetRepositoryConfiguration config : configurations) {
            sb.append(toPreferenceString(config, providers)).append(OUTER_SEPARATOR);
        }
        return sb.toString();
    }

    public static String toPreferenceString(ISnippetRepositoryConfiguration config,
            Collection<ISnippetRepositoryProvider> providers) {
        ISnippetRepositoryProvider provider = findMatchingProvider(config.getClass().getSimpleName(), providers).get();
        if (provider != null) {
            return provider.toPreferenceString(config);
        }
        return ""; //$NON-NLS-1$
    }

    public static Optional<ISnippetRepositoryProvider> findMatchingProvider(ISnippetRepositoryConfiguration config,
            Collection<ISnippetRepositoryProvider> providers) {
        return findMatchingProvider(config.getClass().getSimpleName(), providers);
    }

    public static Optional<ISnippetRepositoryProvider> findMatchingProvider(String identifier,
            Collection<ISnippetRepositoryProvider> providers) {
        for (ISnippetRepositoryProvider provider : providers) {
            if (provider.isApplicable(identifier)) {
                return of(provider);
            }
        }
        return absent();
    }

}

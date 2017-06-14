/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class CompletionRcpPreferences extends AbstractPreferenceInitializer {

    private static final char DISABLED_FLAG = '!';
    private static final char SEPARATOR = ';';

    @Inject
    @Preference(Constants.PREF_SESSIONPROCESSORS)
    private String enabledSessionProcessorString;

    private final Set<SessionProcessorDescriptor> availableProcessors;

    public CompletionRcpPreferences() {
        this(readExtensionPoint());
    }

    @VisibleForTesting
    public CompletionRcpPreferences(Set<SessionProcessorDescriptor> availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    private static Set<SessionProcessorDescriptor> readExtensionPoint() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                Constants.EXT_POINT_SESSION_PROCESSORS);

        Set<SessionProcessorDescriptor> descriptors = new HashSet<>();
        for (final IConfigurationElement element : elements) {
            SessionProcessorDescriptor descriptor = new SessionProcessorDescriptor(element);
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    @Override
    public void initializeDefaultPreferences() {
        // Due to reentrant-injection trouble, this method is *not* called on the singleton provided by
        // CompletionRcpModule. Thus, treat it as if called in a static context; do not write/read instance fields.
        IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_NAME);
        store.setDefault(Constants.PREF_SESSIONPROCESSORS,
                toString(Maps.asMap(availableProcessors, new Function<SessionProcessorDescriptor, Boolean>() {

                    @Override
                    public Boolean apply(SessionProcessorDescriptor descriptor) {
                        return descriptor.isEnabledByDefault();
                    }
                })));
    }

    public Set<SessionProcessorDescriptor> getAvailableSessionProcessors() {
        return availableProcessors;
    }

    public Set<SessionProcessorDescriptor> getEnabledSessionProcessors() {
        return Maps.filterValues(fromString(availableProcessors, enabledSessionProcessorString),
                new Predicate<Boolean>() {

            @Override
            public boolean apply(Boolean input) {
                return input;
            }
        }).keySet();
    }

    public SessionProcessorDescriptor getSessionProcessorDescriptor(String id) {
        return find(availableProcessors, id);
    }

    public void setSessionProcessorEnabled(Collection<SessionProcessorDescriptor> enabledDescriptors,
            Collection<SessionProcessorDescriptor> disabledDescriptors) {
        Map<SessionProcessorDescriptor, Boolean> result = fromString(availableProcessors, enabledSessionProcessorString);

        for (SessionProcessorDescriptor enabledDescriptor : enabledDescriptors) {
            result.put(enabledDescriptor, true);
        }
        for (SessionProcessorDescriptor disabledDescriptor : disabledDescriptors) {
            result.put(disabledDescriptor, false);
        }

        IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Constants.BUNDLE_NAME);
        store.setValue(Constants.PREF_SESSIONPROCESSORS, toString(result));
    }

    public boolean isEnabled(SessionProcessorDescriptor processor) {
        Map<SessionProcessorDescriptor, Boolean> map = fromString(availableProcessors, enabledSessionProcessorString);
        return map.containsKey(processor) ? map.get(processor) : false;
    }

    private static String toString(Map<SessionProcessorDescriptor, Boolean> descriptors) {
        StringBuilder sb = new StringBuilder();

        Iterator<Entry<SessionProcessorDescriptor, Boolean>> it = descriptors.entrySet().iterator();
        while (it.hasNext()) {
            Entry<SessionProcessorDescriptor, Boolean> entry = it.next();
            SessionProcessorDescriptor descriptor = entry.getKey();
            Boolean enabled = entry.getValue();
            if (!enabled) {
                sb.append(DISABLED_FLAG);
            }
            sb.append(descriptor.getId());
            if (it.hasNext()) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    private static Map<SessionProcessorDescriptor, Boolean> fromString(
            Iterable<SessionProcessorDescriptor> descriptors, String string) {
        Map<SessionProcessorDescriptor, Boolean> result = new HashMap<>();
        for (SessionProcessorDescriptor descriptor : descriptors) {
            result.put(descriptor, true);
        }
        for (String id : StringUtils.split(string, SEPARATOR)) {
            final boolean enabled;
            if (id.charAt(0) == DISABLED_FLAG) {
                enabled = false;
                id = id.substring(1);
            } else {
                enabled = true;
            }

            SessionProcessorDescriptor found = find(descriptors, id);
            if (found != null) {
                result.put(found, enabled);
            }
        }
        return result;
    }

    private static SessionProcessorDescriptor find(Iterable<SessionProcessorDescriptor> descriptors, String id) {
        for (SessionProcessorDescriptor descriptor : descriptors) {
            if (descriptor.getId().equals(id)) {
                return descriptor;
            }
        }
        return null;
    }

    @VisibleForTesting
    public void setEnabledSessionProcessorString(String enabledSessionProcessorString) {
        this.enabledSessionProcessorString = enabledSessionProcessorString;
    }
}

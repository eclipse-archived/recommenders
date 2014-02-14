/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static com.google.common.base.Optional.fromNullable;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

public class SessionProcessorDescriptor implements Comparable<SessionProcessorDescriptor> {

    private static final Logger LOG = LoggerFactory.getLogger(SessionProcessorDescriptor.class);

    private static final String PREF_NODE_ID_SESSIONPROCESSORS = "org.eclipse.recommenders.completion.rcp.sessionprocessors"; //$NON-NLS-1$
    private static final String PREF_DISABLED = "disabled"; //$NON-NLS-1$
    private static final String EXT_POINT_SESSION_PROCESSORS = PREF_NODE_ID_SESSIONPROCESSORS;

    public static SessionProcessorDescriptor[] parseExtensions() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint(EXT_POINT_SESSION_PROCESSORS);
        Set<String> disabledProcessors = getDisabledProcessors();
        PriorityQueue<SessionProcessorDescriptor> queue = new PriorityQueue<SessionProcessorDescriptor>();
        try {
            for (IConfigurationElement elem : point.getConfigurationElements()) {
                try {
                    final String pluginId = elem.getContributor().getName();
                    String id = elem.getAttribute("id"); //$NON-NLS-1$
                    String name = elem.getAttribute("name"); //$NON-NLS-1$
                    String description = elem.getAttribute("description"); //$NON-NLS-1$
                    final String iconPath = elem.getAttribute("icon"); //$NON-NLS-1$
                    String priorityString = elem.getAttribute("priority"); //$NON-NLS-1$
                    String preferencePageId = elem.getAttribute("preferencePage"); //$NON-NLS-1$
                    int priority = priorityString == null ? 10 : Integer.parseInt(priorityString);
                    final Image icon = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, iconPath).createImage();
                    SessionProcessor processor = (SessionProcessor) elem.createExecutableExtension("class"); //$NON-NLS-1$
                    boolean enable = !disabledProcessors.contains(id);
                    SessionProcessorDescriptor d = new SessionProcessorDescriptor(id, name, description, icon,
                            priority, enable, preferencePageId, processor);
                    queue.add(d);
                } catch (Exception e) {
                    LOG.error("Exception during extension point parsing.", e); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            LOG.error("Exception during extension point parsing", e); //$NON-NLS-1$
        }
        SessionProcessorDescriptor[] res = queue.toArray(new SessionProcessorDescriptor[0]);
        return res;
    }

    private static Set<String> getDisabledProcessors() {
        String prefs = getSessionProcessorPreferences().get(PREF_DISABLED, ""); //$NON-NLS-1$
        Iterable<String> split = Splitter.on(';').omitEmptyStrings().split(prefs);
        return Sets.newHashSet(split);

    }

    private static void saveDisabledProcessors(Set<String> disabledProcessors) {
        String join = Joiner.on(';').skipNulls().join(disabledProcessors);
        IEclipsePreferences store = getSessionProcessorPreferences();
        store.put(PREF_DISABLED, join);
        try {
            store.flush();
        } catch (BackingStoreException e) {
            LOG.error("Failed to flush preferences", e); //$NON-NLS-1$
        }
    }

    private static IEclipsePreferences getSessionProcessorPreferences() {
        return InstanceScope.INSTANCE.getNode(PREF_NODE_ID_SESSIONPROCESSORS);
    }

    private String id;
    private String name;
    private String description;
    private Image icon;
    private int priority;
    private boolean enabled;
    private SessionProcessor processor;
    private String preferencePageId;

    public SessionProcessorDescriptor(String id, String name, String description, Image icon, int priority,
            boolean enabled, String preferencePageId, SessionProcessor processor) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.priority = priority;
        this.enabled = enabled;
        this.preferencePageId = preferencePageId;
        this.processor = processor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return defaultString(description, ""); //$NON-NLS-1$
    }

    public Image getIcon() {
        return icon;
    }

    public int getPriority() {
        return priority;
    }

    public SessionProcessor getProcessor() {
        return processor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enable) {
        enabled = enable;
        Set<String> disabledProcessors = getDisabledProcessors();
        if (enable) {
            disabledProcessors.remove(id);
        } else {
            disabledProcessors.add(id);
        }
        saveDisabledProcessors(disabledProcessors);
    }

    public Optional<String> getPreferencePage() {
        return fromNullable(preferencePageId);
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public int compareTo(SessionProcessorDescriptor o) {
        String other = o.priority + o.id;
        String self = priority + id;
        return self.compareTo(other);
    }

    public static final class EnabledSessionProcessorPredicate implements Predicate<SessionProcessorDescriptor> {
        @Override
        public boolean apply(SessionProcessorDescriptor p) {
            return p.isEnabled();
        }
    }
}

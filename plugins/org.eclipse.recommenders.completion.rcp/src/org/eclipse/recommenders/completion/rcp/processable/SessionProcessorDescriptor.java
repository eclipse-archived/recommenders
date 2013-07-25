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

import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SessionProcessorDescriptor implements Comparable<SessionProcessorDescriptor> {
    private static Logger log = LoggerFactory.getLogger(SessionProcessorDescriptor.class);

    private static final String PREF_NODE_ID_SESSIONPROCESSORS = "org.eclipse.recommenders.completion.rcp.sessionprocessors";
    private static final String DISABLED = "disabled";
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
                    String id = elem.getAttribute("id");
                    String name = elem.getAttribute("name");
                    final String iconPath = elem.getAttribute("icon");
                    String priorityString = elem.getAttribute("priority");
                    int priority = priorityString == null ? 10 : Integer.parseInt(priorityString);
                    final Image icon = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, iconPath).createImage();
                    SessionProcessor processor = (SessionProcessor) elem.createExecutableExtension("class");
                    boolean enable = !disabledProcessors.contains(id);
                    SessionProcessorDescriptor d = new SessionProcessorDescriptor(id, name, icon, priority, enable,
                            processor);
                    queue.add(d);
                } catch (Exception e) {
                    log.error("Exception during extension point parsing.", e);
                }
            }
        } catch (Exception e) {
            log.error("Exception during extension point parsing", e);
        }
        SessionProcessorDescriptor[] res = queue.toArray(new SessionProcessorDescriptor[0]);
        return res;
    }

    private static Set<String> getDisabledProcessors() {
        String prefs = getSessionProcessorPreferences().get(DISABLED, "");
        return Sets.newHashSet(StringUtils.split(prefs));

    }

    private static void saveDisabledProcessors(Set<String> disabledProcessors) {
        @SuppressWarnings("unchecked")
        String join = StringUtils.join(disabledProcessors);
        getSessionProcessorPreferences().put(DISABLED, join);
    }

    private static IEclipsePreferences getSessionProcessorPreferences() {
        return InstanceScope.INSTANCE.getNode(PREF_NODE_ID_SESSIONPROCESSORS);
    }

    private String id;
    private String name;
    private Image icon;
    private int priority;
    private boolean enabled;
    private SessionProcessor processor;

    public SessionProcessorDescriptor(String id, String name, Image icon, int priority, boolean enabled,
            SessionProcessor processor) {
        super();
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.priority = priority;
        this.enabled = enabled;
        this.processor = processor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    @Override
    public int compareTo(SessionProcessorDescriptor o) {
        String other = o.priority + o.id;
        String self = priority + id;
        return self.compareTo(other);
    }
}

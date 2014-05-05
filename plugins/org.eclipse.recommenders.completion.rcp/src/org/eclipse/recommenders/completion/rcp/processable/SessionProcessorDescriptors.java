/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe- Initial API
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static java.lang.Boolean.TRUE;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.internal.completion.rcp.Constants;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class SessionProcessorDescriptors {

    private static final Logger LOG = LoggerFactory.getLogger(SessionProcessorDescriptor.class);

    private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault"; //$NON-NLS-1$

    private static final char DISABLED_FLAG = '!';
    private static final char SEPARATOR = ';';

    public static List<SessionProcessorDescriptor> getRegisteredProcessors() {
        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                Constants.EXT_POINT_SESSION_PROCESSORS);

        final List<SessionProcessorDescriptor> descriptors = Lists.newLinkedList();
        for (final IConfigurationElement element : elements) {
            try {
                boolean enabled = Boolean.valueOf(Objects.firstNonNull(
                        element.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE), TRUE.toString()));

                final String pluginId = element.getContributor().getName();
                String id = element.getAttribute("id"); //$NON-NLS-1$
                String name = element.getAttribute("name"); //$NON-NLS-1$
                String description = element.getAttribute("description"); //$NON-NLS-1$
                final String iconPath = element.getAttribute("icon"); //$NON-NLS-1$
                String priorityString = element.getAttribute("priority"); //$NON-NLS-1$
                String preferencePageId = element.getAttribute("preferencePage"); //$NON-NLS-1$
                int priority = priorityString == null ? 10 : Integer.parseInt(priorityString);
                final Image icon = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, iconPath).createImage();
                SessionProcessor processor = (SessionProcessor) element.createExecutableExtension("class"); //$NON-NLS-1$
                SessionProcessorDescriptor d = new SessionProcessorDescriptor(id, name, description, icon, priority,
                        enabled, preferencePageId, processor);
                descriptors.add(d);
            } catch (Exception e) {
                LOG.error("Exception during extension point parsing.", e); //$NON-NLS-1$
            }
        }
        return descriptors;
    }

    public static String toString(List<SessionProcessorDescriptor> descriptors) {
        StringBuilder sb = new StringBuilder();

        Iterator<SessionProcessorDescriptor> it = descriptors.iterator();
        while (it.hasNext()) {
            SessionProcessorDescriptor descriptor = it.next();
            if (!descriptor.isEnabled()) {
                sb.append(DISABLED_FLAG);
            }
            sb.append(descriptor.getId());
            if (it.hasNext()) {
                sb.append(SEPARATOR);
            }
        }

        return sb.toString();
    }

    public static List<SessionProcessorDescriptor> fromString(String string, List<SessionProcessorDescriptor> available) {
        List<SessionProcessorDescriptor> result = Lists.newArrayList();
        for (String id : StringUtils.split(string, SEPARATOR)) {
            final boolean enabled;
            if (id.charAt(0) == DISABLED_FLAG) {
                enabled = false;
                id = id.substring(1);
            } else {
                enabled = true;
            }

            SessionProcessorDescriptor found = find(available, id);
            if (found != null) {
                found.setEnabled(enabled);
                result.add(found);
            }
        }

        for (SessionProcessorDescriptor descriptor : available) {
            if (find(result, descriptor.getId()) == null) {
                result.add(descriptor);
            }
        }

        return result;
    }

    private static SessionProcessorDescriptor find(List<SessionProcessorDescriptor> descriptors, String id) {
        for (SessionProcessorDescriptor descriptor : descriptors) {
            if (descriptor.getId().equals(id)) {
                return descriptor;
            }
        }
        return null;
    }
}

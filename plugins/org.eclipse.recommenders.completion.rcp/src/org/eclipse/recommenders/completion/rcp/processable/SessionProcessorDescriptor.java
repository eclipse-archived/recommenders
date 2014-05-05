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
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.defaultString;

import org.eclipse.swt.graphics.Image;

import com.google.common.base.Optional;

public class SessionProcessorDescriptor implements Comparable<SessionProcessorDescriptor> {

    private final String id;
    private final String name;
    private final String description;
    private final Image icon;
    private final int priority;
    private boolean enabled;
    private final SessionProcessor processor;
    private final String preferencePageId;

    public SessionProcessorDescriptor(String id, String name, String description, Image icon, int priority,
            boolean enabled, String preferencePageId, SessionProcessor processor) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(processor);
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
}

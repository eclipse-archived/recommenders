/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import static java.lang.Boolean.TRUE;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class AdvisorDescriptors {

    private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";
    private static final String DEFAULT_PRIORITY_ATTRIBUTE = "defaultPriority";

    private static final char DISABLED_FLAG = '!';
    private static final char SEPARATOR = ';';

    private static final String EXT_ID_PROVIDER = "org.eclipse.recommenders.models.rcp.advisors";

    public static List<AdvisorDescriptor> getRegisteredAdvisors() {
        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_ID_PROVIDER);
        Arrays.sort(elements, new Comparator<IConfigurationElement>() {

            @Override
            public int compare(IConfigurationElement lhs, IConfigurationElement rhs) {
                Integer lhsPriority = Integer.valueOf(lhs.getAttribute(DEFAULT_PRIORITY_ATTRIBUTE));
                Integer rhsPriority = Integer.valueOf(rhs.getAttribute(DEFAULT_PRIORITY_ATTRIBUTE));
                return lhsPriority.compareTo(rhsPriority);
            }
        });

        final List<AdvisorDescriptor> descriptors = Lists.newLinkedList();
        for (final IConfigurationElement element : elements) {
            boolean enabled = Boolean.valueOf(Objects.firstNonNull(element.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE),
                    TRUE.toString()));
            descriptors.add(new AdvisorDescriptor(element, enabled));
        }
        return descriptors;
    }

    /**
     * Re-creates a list of {@code AdvisorDescriptor}s stored by @{link #store} based on the {@code AdvisorDescriptor}s
     * still available. {@code AdvisorDescriptor}s that are no longer available but are still referenced by the loaded
     * string are not part of the result. {@code AdvisorDescriptor}s that are available but not referenced by the loaded
     * string are appended to the result.
     */
    public static List<AdvisorDescriptor> load(String string, List<AdvisorDescriptor> available) {
        List<AdvisorDescriptor> result = Lists.newArrayList();
        for (String id : StringUtils.split(string, SEPARATOR)) {
            final boolean enabled;
            if (id.charAt(0) == DISABLED_FLAG) {
                enabled = false;
                id = id.substring(1);
            } else {
                enabled = true;
            }

            AdvisorDescriptor found = find(available, id);
            if (found != null) {
                AdvisorDescriptor descriptor = new AdvisorDescriptor(found);
                descriptor.setEnabled(enabled);
                result.add(descriptor);
            }
        }

        for (AdvisorDescriptor descriptor : available) {
            if (find(result, descriptor.getId()) == null) {
                result.add(descriptor);
            }
        }

        return result;
    }

    public static String store(List<AdvisorDescriptor> descriptors) {
        StringBuilder sb = new StringBuilder();

        Iterator<AdvisorDescriptor> it = descriptors.iterator();
        while (it.hasNext()) {
            AdvisorDescriptor descriptor = it.next();
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

    private static AdvisorDescriptor find(List<AdvisorDescriptor> descriptors, String id) {
        for (AdvisorDescriptor descriptor : descriptors) {
            if (descriptor.getId().equals(id))
                return descriptor;
        }
        return null;
    }
}

/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasser Aziza - initial API and implementation.
 */
package org.eclipse.recommenders.internal.utils.rcp.preferences;

import static org.eclipse.recommenders.internal.utils.rcp.l10n.LogMessages.ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.recommenders.utils.Nullable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

public final class ContributionsReader {

    private ContributionsReader() {
        // Not meant to be instantiated
    }

    private static final String PREF_CONTRIBUTION_ID = "org.eclipse.recommenders.utils.rcp.linkContribution"; //$NON-NLS-1$
    private static final String CONTRIBUTION_ELEMENT = "linkContribution"; //$NON-NLS-1$

    private static final String PREF_PAGE_ID_ATTRIBUTE = "preferencePageId"; //$NON-NLS-1$
    private static final String LABEL_ATTRIBUTE = "label"; //$NON-NLS-1$
    private static final String COMMAND_ID_ATTRIBUTE = "commandId"; //$NON-NLS-1$
    private static final String PRIORITY_ATTRIBUTE = "priority"; //$NON-NLS-1$
    private static final String ICON_ATTRIBUTE = "icon"; //$NON-NLS-1$

    public static List<ContributionLink> readContributionLinks(String preferencePageId) {
        final IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(PREF_CONTRIBUTION_ID);
        return readContributionLinks(preferencePageId, configurationElements);
    }

    @VisibleForTesting
    static List<ContributionLink> readContributionLinks(String preferencePageId,
            @Nullable IConfigurationElement... configurationElements) {
        List<ContributionLink> links = new ArrayList<>();

        if (configurationElements == null) {
            return ImmutableList.of();
        }

        for (final IConfigurationElement configurationElement : configurationElements) {
            if (CONTRIBUTION_ELEMENT.equals(configurationElement.getName())) {
                final String pluginId = configurationElement.getContributor().getName();
                final String pageId = configurationElement.getAttribute(PREF_PAGE_ID_ATTRIBUTE);
                if (!preferencePageId.equals(pageId)) {
                    continue;
                }
                final String labelAttribute = configurationElement.getAttribute(LABEL_ATTRIBUTE);
                if (!isValidAttribute(labelAttribute)) {
                    Logs.log(ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE, LABEL_ATTRIBUTE, labelAttribute);
                    continue;
                }

                final String commandIdAttribute = configurationElement.getAttribute(COMMAND_ID_ATTRIBUTE);
                if (!isValidAttribute(commandIdAttribute)) {
                    Logs.log(ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE, COMMAND_ID_ATTRIBUTE, commandIdAttribute);
                    continue;
                }

                final String priorityAttribute = configurationElement.getAttribute(PRIORITY_ATTRIBUTE);
                final Integer priority;
                if (priorityAttribute != null) {
                    priority = Ints.tryParse(priorityAttribute);
                    if (priority == null) {
                        Logs.log(ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE, PRIORITY_ATTRIBUTE, priorityAttribute);
                        continue;
                    }
                } else {
                    priority = Integer.MAX_VALUE;
                }

                final String iconAttribute = configurationElement.getAttribute(ICON_ATTRIBUTE);
                final Image image;
                if (iconAttribute != null) {
                    ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId,
                            iconAttribute);
                    if (imageDescriptor != null) {
                        image = imageDescriptor.createImage();
                    } else {
                        Logs.log(ERROR_FAILED_TO_READ_EXTENSION_ATTRIBUTE, ICON_ATTRIBUTE, iconAttribute);
                        continue;
                    }
                } else {
                    image = null;
                }

                links.add(new ContributionLink(labelAttribute, commandIdAttribute, priority, image));
            }
        }
        Collections.sort(links);
        return ImmutableList.copyOf(links);

    }

    private static boolean isValidAttribute(String attribute) {
        return !Strings.isNullOrEmpty(attribute);
    }
}

/**
 * Copyright (c) 2014 Olav Lenz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class WizardDescriptors {

    private static Logger LOG = LoggerFactory.getLogger(WizardDescriptors.class);

    private static final String CONFIGURATION_WIZARD_NAME = "name"; //$NON-NLS-1$
    private static final String CONFIGURATION_WIZARD = "wizard"; //$NON-NLS-1$
    private static final String EXT_ID_CONFIGURATION_WIZARDS = "org.eclipse.recommenders.snipmatch.rcp.configurationwizards"; //$NON-NLS-1$

    public static List<WizardDescriptor> loadAvailableWizards() {
        List<WizardDescriptor> wizardDescriptors = Lists.newArrayList();
        try {
            final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                    EXT_ID_CONFIGURATION_WIZARDS);

            for (IConfigurationElement configurationElement : elements) {

                AbstractSnippetRepositoryWizard wizard;
                wizard = Checks.cast(configurationElement.createExecutableExtension(CONFIGURATION_WIZARD));
                String name = configurationElement.getAttribute(CONFIGURATION_WIZARD_NAME);

                wizardDescriptors.add(new WizardDescriptor(name, wizard));
            }
        } catch (CoreException e) {
            LOG.error(MessageFormat.format("Exception while reading extension point {}", EXT_ID_CONFIGURATION_WIZARDS), //$NON-NLS-1$
                    e);
        }
        return wizardDescriptors;
    }

    public static List<WizardDescriptor> filterApplicableWizardDescriptors(List<WizardDescriptor> descriptors,
            SnippetRepositoryConfiguration config) {
        List<WizardDescriptor> wizardDescriptors = Lists.newArrayList();
        for (WizardDescriptor descriptor : descriptors) {
            if (descriptor.getWizard().isApplicable(config)) {
                wizardDescriptors.add(descriptor);
            }
        }
        return wizardDescriptors;
    }

}

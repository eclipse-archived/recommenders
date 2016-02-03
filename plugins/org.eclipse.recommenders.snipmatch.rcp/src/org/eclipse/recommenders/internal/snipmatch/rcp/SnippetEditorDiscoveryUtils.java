/**
 * Copyright (c) 2013 Stefan Prisca.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Prisca - initial API and implementation
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.equinox.internal.p2.discovery.Catalog;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.compatibility.BundleDiscoveryStrategy;
import org.eclipse.equinox.internal.p2.discovery.compatibility.RemoteBundleDiscoveryStrategy;
import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogConfiguration;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.DiscoveryWizard;
import org.eclipse.jface.wizard.WizardDialog;

public final class SnippetEditorDiscoveryUtils {

    private SnippetEditorDiscoveryUtils() {
        // Not meant to be instantiated
    }

    private static final String SNIPMATCH_P2_DISCOVERY_URL = "http://download.eclipse.org/recommenders/discovery/2.x/directories/snipmatch.xml"; //$NON-NLS-1$

    @SuppressWarnings("restriction")
    public static void openDiscoveryDialog() {
        Catalog catalog = new Catalog();
        catalog.setEnvironment(DiscoveryCore.createEnvironment());
        catalog.setVerifyUpdateSiteAvailability(false);

        // look for descriptors from installed bundles
        catalog.getDiscoveryStrategies().add(new BundleDiscoveryStrategy());

        // look for remote descriptor
        RemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new RemoteBundleDiscoveryStrategy();
        remoteDiscoveryStrategy.setDirectoryUrl(SNIPMATCH_P2_DISCOVERY_URL);
        catalog.getDiscoveryStrategies().add(remoteDiscoveryStrategy);

        CatalogConfiguration configuration = new CatalogConfiguration();
        configuration.setShowTagFilter(false);

        DiscoveryWizard wizard = new DiscoveryWizard(catalog, configuration);
        WizardDialog dialog = new WizardDialog(WorkbenchUtil.getShell(), wizard);
        dialog.open();
    }
}

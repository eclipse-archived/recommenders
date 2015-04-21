/**
 * Copyright (c) 2014 Moritz Beller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.rcp.utils;

import java.util.Dictionary;

import org.eclipse.equinox.internal.p2.discovery.Catalog;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.compatibility.RemoteBundleDiscoveryStrategy;
import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogConfiguration;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.DiscoveryWizard;
import org.eclipse.jface.wizard.WizardDialog;

@SuppressWarnings("restriction")
public final class Dialogs {

    private Dialogs() {
        // Not meant to be instantiated
    }

    /** Opens a dialog wizard displaying new code recommenders extensions. */
    public static WizardDialog newExtensionsDiscoveryDialog(String discoveryUrl) {
        Catalog catalog = new Catalog();
        Dictionary<Object, Object> env = DiscoveryCore.createEnvironment();
        catalog.setEnvironment(env);
        catalog.setVerifyUpdateSiteAvailability(false);

        RemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new RemoteBundleDiscoveryStrategy();
        remoteDiscoveryStrategy.setDirectoryUrl(discoveryUrl);
        catalog.getDiscoveryStrategies().add(remoteDiscoveryStrategy);

        CatalogConfiguration configuration = new CatalogConfiguration();
        configuration.setShowTagFilter(false);

        DiscoveryWizard wizard = new DiscoveryWizard(catalog, configuration);
        return new WizardDialog(WorkbenchUtil.getShell(), wizard);
    }

}

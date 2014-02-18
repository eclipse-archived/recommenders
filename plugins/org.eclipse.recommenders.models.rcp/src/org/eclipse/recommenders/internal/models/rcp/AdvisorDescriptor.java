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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.recommenders.internal.rcp.RcpPlugin;
import org.eclipse.recommenders.models.IProjectCoordinateAdvisor;

public class AdvisorDescriptor {

    private final IConfigurationElement config;
    private boolean enabled;

    public AdvisorDescriptor(AdvisorDescriptor that) {
        this(that.config, that.enabled);
    }

    public AdvisorDescriptor(IConfigurationElement config, boolean enabled) {
        this.config = config;
        this.enabled = enabled;
    }

    public String getId() {
        return config.getAttribute("id"); //$NON-NLS-1$
    }

    public String getName() {
        return config.getAttribute("name"); //$NON-NLS-1$
    }

    public String getDescription() {
        return config.getAttribute("description"); //$NON-NLS-1$
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public IProjectCoordinateAdvisor createAdvisor() throws CoreException {
        try {
            return (IProjectCoordinateAdvisor) config.createExecutableExtension("class"); //$NON-NLS-1$
        } catch (CoreException e) {
            String pluginId = config.getContributor().getName();
            RcpPlugin.logError(e, Messages.LOG_ERROR_ADVISOR_INSTANTIATION,
                    pluginId, config.getAttribute("class")); //$NON-NLS-1$
            throw e;
        }
    }
}

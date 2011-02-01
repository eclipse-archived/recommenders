/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.commons.injection;

import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ExtensionFactory implements IExecutableExtension, IExecutableExtensionFactory {

    private IConfigurationElement config;
    private String propertyName;
    private Object data;

    @Override
    public void setInitializationData(final IConfigurationElement config, final String propertyName, final Object data)
            throws CoreException {

        this.config = config;
        this.propertyName = propertyName;
        this.data = data;
    }

    @Override
    public Object create() throws CoreException {
        final Object extension = loadExtension();
        initializeExtension(extension);
        return extension;
    }

    private Object loadExtension() throws CoreException {
        if (data instanceof String) {
            final Class<?> clazz = resolveClass((String) data);
            return InjectionService.getInstance().getInjector().getInstance(clazz);
        } else {
            throw new CoreException(new Status(IStatus.ERROR, config.getContributor().getName(),
                    "Class configuration missing"));
        }
    }

    private Class<?> resolveClass(final String className) throws CoreException {
        try {
            return ContributorFactoryOSGi.resolve(config.getContributor()).loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new CoreException(new Status(IStatus.ERROR, config.getContributor().getName(),
                    "Class could not be found", e));
        }
    }

    private void initializeExtension(final Object extension) throws CoreException {
        if (extension instanceof IExecutableExtension) {
            ((IExecutableExtension) extension).setInitializationData(config, propertyName, data);
        }
    }
}

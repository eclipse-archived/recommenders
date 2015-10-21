/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.internal.apidocs.rcp;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.eclipse.recommenders.internal.apidocs.rcp.l10n.LogMessages.ERROR_FAILED_TO_INSTANTIATE_PROVIDER;
import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.recommenders.apidocs.rcp.ApidocProvider;
import org.eclipse.recommenders.apidocs.rcp.ApidocProviderDescription;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

public class ApidocsRcpModule extends AbstractModule {

    private static final String EXT_ID_PROVIDER = "org.eclipse.recommenders.apidocs.rcp.providers"; //$NON-NLS-1$

    @Override
    protected void configure() {
        bind(ApidocsPreferences.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    @Extdoc
    IPreferenceStore providePreferenceStore() {
        return ApidocsRcpPlugin.getDefault().getPreferenceStore();
    }

    @Provides
    @Singleton
    List<ApidocProvider> provideProviders() {
        return instantiateProvidersFromRegistry();
    }

    private static List<ApidocProvider> instantiateProvidersFromRegistry() {
        final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_ID_PROVIDER);
        final List<ApidocProvider> providers = Lists.newLinkedList();

        for (final IConfigurationElement element : elements) {
            final Optional<ApidocProvider> opt = createProvider(element);
            if (opt.isPresent()) {
                providers.add(opt.get());
            }
        }

        Collections.sort(providers, new Comparator<ApidocProvider>() {

            @Override
            public int compare(ApidocProvider o1, ApidocProvider o2) {
                String n1 = o1.getDescription().getName();
                String n2 = o2.getDescription().getName();
                if (n1.equals("Javadoc")) { //$NON-NLS-1$
                    return -1;
                } else if (n2.equals("Javadoc")) { //$NON-NLS-1$
                    return 1;
                } else {
                    return n1.compareTo(n2);
                }
            }
        });
        return providers;
    }

    static Optional<ApidocProvider> createProvider(final IConfigurationElement element) {
        final String pluginId = element.getContributor().getName();
        try {
            final String imagePath = element.getAttribute("image"); //$NON-NLS-1$
            final String name = element.getAttribute("name"); //$NON-NLS-1$
            final Image image = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, imagePath).createImage();
            final ApidocProvider provider = (ApidocProvider) element.createExecutableExtension("class"); //$NON-NLS-1$
            final ApidocProviderDescription description = new ApidocProviderDescription(name, image);
            provider.setDescription(description);
            return Optional.of(provider);
        } catch (final Exception e) {
            log(ERROR_FAILED_TO_INSTANTIATE_PROVIDER, e, pluginId, element.getAttribute("class")); //$NON-NLS-1$
            return Optional.absent();
        }
    }

    @BindingAnnotation
    @Target({ METHOD, PARAMETER })
    @Retention(RUNTIME)
    public static @interface Extdoc {
    }
}

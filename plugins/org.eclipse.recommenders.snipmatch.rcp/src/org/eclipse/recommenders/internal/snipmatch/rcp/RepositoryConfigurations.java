/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.*;
import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.recommenders.snipmatch.model.DefaultSnippetRepositoryConfigurationProvider;
import org.eclipse.recommenders.snipmatch.model.SnipmatchModelPackage;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.snipmatch.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.utils.Logs;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public final class RepositoryConfigurations {

    private RepositoryConfigurations() {
        // Not meant to be instantiated
    }

    public static SnippetRepositoryConfigurations loadConfigurations(File file) {
        SnippetRepositoryConfigurations configurations = SnipmatchRcpModelFactory.eINSTANCE
                .createSnippetRepositoryConfigurations();

        if (!file.exists()) {
            return configurations;
        }

        try {
            Resource resource = provideResource(file);
            resource.load(Collections.EMPTY_MAP);
            if (!resource.getContents().isEmpty()) {
                configurations = (SnippetRepositoryConfigurations) resource.getContents().get(0);
            }
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_LOADING_REPO_CONFIGURATION, e, file);
        }

        return configurations;
    }

    private static Resource provideResource(File file) {
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("snipmatch", new XMIResourceFactoryImpl()); //$NON-NLS-1$

        ResourceSet resSet = new ResourceSetImpl();
        Resource resource = resSet.createResource(URI.createFileURI(file.getAbsolutePath()));
        return resource;
    }

    public static void storeConfigurations(SnippetRepositoryConfigurations configurations, File file) {
        Resource resource = provideResource(file);
        resource.getContents().add(configurations);

        try {
            resource.save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            Logs.log(LogMessages.ERROR_STORING_REPO_CONFIGURATION, e, file);
        }
    }

    protected static List<SnippetRepositoryConfiguration> fetchDefaultConfigurations() {
        List<SnippetRepositoryConfiguration> defaultConfigurations = Lists.newArrayList();

        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
                EXT_POINT_REGISTERED_EMF_PACKAGE);
        for (IConfigurationElement element : elements) {
            try {
                String uri = element.getAttribute(EXT_POINT_REGISTERED_EMF_PACKAGE_URI);
                if (uri == null) {
                    continue;
                }
                EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(uri);
                if (ePackage == null) {
                    continue;
                }
                List<EClass> subtypes = searchSubtypes(ePackage,
                        SnipmatchModelPackage.Literals.DEFAULT_SNIPPET_REPOSITORY_CONFIGURATION_PROVIDER);
                for (EClass eClass : subtypes) {
                    DefaultSnippetRepositoryConfigurationProvider configurationProvider = cast(EPackage.Registry.INSTANCE
                            .getEFactory(uri).create(eClass));
                    EList<SnippetRepositoryConfiguration> subDefaultConfigurations = configurationProvider
                            .getDefaultConfiguration();
                    for (SnippetRepositoryConfiguration config : subDefaultConfigurations) {
                        if (Strings.isNullOrEmpty(config.getId())) {
                            Logs.log(LogMessages.ERROR_DEFAULT_REPO_CONFIGURATION_WITHOUT_ID);
                            continue;
                        } else {
                            config.setDefaultConfiguration(true);
                            defaultConfigurations.add(config);
                        }
                    }
                }
            } catch (Exception e) {
                Logs.log(LogMessages.ERROR_LOADING_DEFAULT_REPO_CONFIGURATION, e);
            }
        }
        return defaultConfigurations;
    }

    private static List<EClass> searchSubtypes(EPackage ePackage, EClass eClass) {
        List<EClass> subTypes = Lists.newArrayList();
        for (EClassifier eClassifier : ePackage.getEClassifiers()) {
            if (eClassifier instanceof EClass) {
                EClass otherEClass = (EClass) eClassifier;
                if (eClass.isSuperTypeOf(otherEClass) && eClass != otherEClass) {
                    if (!(otherEClass.isAbstract() || otherEClass.isInterface())) {
                        subTypes.add((EClass) eClassifier);
                    }
                }
            }
        }
        return subTypes;
    }

}

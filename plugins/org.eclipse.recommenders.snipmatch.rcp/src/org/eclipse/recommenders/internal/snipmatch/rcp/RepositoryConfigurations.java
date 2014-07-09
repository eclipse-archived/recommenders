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

import static org.eclipse.recommenders.utils.Checks.cast;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.recommenders.rcp.model.SnipmatchRcpModelFactory;
import org.eclipse.recommenders.rcp.model.SnippetRepositoryConfigurations;
import org.eclipse.recommenders.snipmatch.model.DefaultSnippetRepositoryConfigurationProvider;
import org.eclipse.recommenders.snipmatch.model.SnipmatchModelPackage;
import org.eclipse.recommenders.snipmatch.model.SnippetRepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class RepositoryConfigurations {

    private static Logger LOG = LoggerFactory.getLogger(RepositoryConfigurations.class);

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
            LOG.error("Exception while loading repository configurations.", e); //$NON-NLS-1$
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
            LOG.error("Exception while storing repository configurations.", e); //$NON-NLS-1$
        }
    }

    protected static List<SnippetRepositoryConfiguration> fetchDefaultConfigurations() {
        List<SnippetRepositoryConfiguration> defaultConfigurations = Lists.newArrayList();

        Registry instance = EPackage.Registry.INSTANCE;
        for (String key : instance.keySet()) {
            EPackage ePackage = instance.getEPackage(key);
            List<EClass> subtypes = searchSubtypes(ePackage,
                    SnipmatchModelPackage.Literals.DEFAULT_SNIPPET_REPOSITORY_CONFIGURATION_PROVIDER);
            for (EClass eClass : subtypes) {
                DefaultSnippetRepositoryConfigurationProvider configurationProvider = cast(instance.getEFactory(key)
                        .create(eClass));
                defaultConfigurations.addAll(configurationProvider.getDefaultConfiguration());
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

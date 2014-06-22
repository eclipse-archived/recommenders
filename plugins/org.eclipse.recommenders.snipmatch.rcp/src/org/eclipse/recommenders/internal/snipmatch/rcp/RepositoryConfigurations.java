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
import org.eclipse.recommenders.injection.InjectionService;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.DefaultSnippetRepositoryConfigurationProvider;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnipmatchFactory;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnipmatchPackage;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfiguration;
import org.eclipse.recommenders.snipmatch.model.snipmatchmodel.SnippetRepositoryConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.name.Names;

public class RepositoryConfigurations {

    private static Logger LOG = LoggerFactory.getLogger(RepositoryConfigurations.class);

    public static final File LOCATION = InjectionService.getInstance().requestAnnotatedInstance(File.class,
            Names.named(SnipmatchRcpModule.REPOSITORY_CONFIGURATION_FILE));

    public static SnippetRepositoryConfigurations loadConfigurations() {
        Resource resource = provideResource();

        SnippetRepositoryConfigurations configurations;
        if (!resource.getContents().isEmpty()) {
            configurations = (SnippetRepositoryConfigurations) resource.getContents().get(0);
        } else {
            configurations = SnipmatchFactory.eINSTANCE.createSnippetRepositoryConfigurations();
        }

        return configurations;
    }

    private static Resource provideResource() {
        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put("snipmatch", new XMIResourceFactoryImpl()); //$NON-NLS-1$

        ResourceSet resSet = new ResourceSetImpl();
        Resource resource = resSet.createResource(URI.createFileURI(LOCATION.getAbsolutePath()));
        return resource;
    }

    public static void storeConfigurations(SnippetRepositoryConfigurations configurations) {
        Resource resource = provideResource();
        resource.getContents().add(configurations);

        try {
            resource.save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            LOG.error("Exception while storing repository configurations.", e); //$NON-NLS-1$
        }
    }

    public static List<SnippetRepositoryConfiguration> fetchDefaultConfigurations() {
        List<SnippetRepositoryConfiguration> defaultConfigurations = Lists.newArrayList();

        Registry instance = EPackage.Registry.INSTANCE;
        for (String key : instance.keySet()) {
            EPackage ePackage = instance.getEPackage(key);
            List<EClass> subtypes = searchSubtypes(ePackage,
                    SnipmatchPackage.Literals.DEFAULT_SNIPPET_REPOSITORY_CONFIGURATION_PROVIDER);
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

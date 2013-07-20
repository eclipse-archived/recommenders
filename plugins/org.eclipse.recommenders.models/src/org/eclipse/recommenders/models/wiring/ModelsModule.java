/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Olav Lenz - initial API and implementation
 */
package org.eclipse.recommenders.models.wiring;

import java.io.File;

import javax.inject.Singleton;

import org.eclipse.recommenders.internal.rcp.wiring.RecommendersModule.ModelRepositoryIndexLocation;
import org.eclipse.recommenders.models.dependencies.impl.FingerprintStrategy;
import org.eclipse.recommenders.models.dependencies.impl.JREExecutionEnvironmentStrategy;
import org.eclipse.recommenders.models.dependencies.impl.JREReleaseFileStrategy;
import org.eclipse.recommenders.models.dependencies.impl.MappingProvider;
import org.eclipse.recommenders.models.dependencies.impl.MavenPomPropertiesStrategy;
import org.eclipse.recommenders.models.dependencies.impl.OsgiManifestStrategy;
import org.eclipse.recommenders.models.dependencies.impl.SimpleIndexSearcher;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;

public class ModelsModule extends AbstractModule implements Module {

    @Override
    protected void configure() {
        //
    }

    @Singleton
    @Provides
    protected MappingProvider provideMappingProvider(@ModelRepositoryIndexLocation File localRepositoryFile) {
        MappingProvider mappingProvider = new MappingProvider();
        mappingProvider.addStrategy(new MavenPomPropertiesStrategy());
        mappingProvider.addStrategy(new JREExecutionEnvironmentStrategy());
        mappingProvider.addStrategy(new JREReleaseFileStrategy());
        mappingProvider.addStrategy(new OsgiManifestStrategy());
        mappingProvider.addStrategy(new FingerprintStrategy(new SimpleIndexSearcher(localRepositoryFile)));
        return mappingProvider;
    }

}

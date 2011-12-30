/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.extdocs.zip;

import org.eclipse.recommenders.mining.extdocs.AbstractExtdocGuiceModule;
import org.eclipse.recommenders.mining.extdocs.AlgorithmParameters;
import org.eclipse.recommenders.mining.extdocs.ICompilationUnitProvider;
import org.eclipse.recommenders.mining.extdocs.IExtdocDirectiveConsumer;
import org.eclipse.recommenders.mining.extdocs.ISuperclassProvider;
import org.eclipse.recommenders.mining.extdocs.couch.CouchDbDataAccess;
import org.eclipse.recommenders.mining.extdocs.couch.CouchExtdocDirectiveConsumer;
import org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule.Output;
import org.eclipse.recommenders.webclient.ClientConfiguration;
import org.eclipse.recommenders.webclient.WebServiceClient;

import com.google.inject.Provides;
import com.google.inject.Singleton;

public class ZipGuiceModule extends AbstractExtdocGuiceModule {

    public ZipGuiceModule(final AlgorithmParameters arguments) {
        super(arguments);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ICompilationUnitProvider.class).to(ZipCompilationUnitProvider.class);
        bind(ISuperclassProvider.class).to(ZipCompilationUnitProvider.class);
        bind(IExtdocDirectiveConsumer.class).to(CouchExtdocDirectiveConsumer.class);
    }

    @Provides
    @Singleton
    @Output
    protected CouchDbDataAccess provideOutputDbDataAccess(final AlgorithmParameters args) {
        final String url = arguments.getOutputHost().toExternalForm();
        final ClientConfiguration config = ClientConfiguration.create(url);
        return new CouchDbDataAccess(new WebServiceClient(config));
    }
}
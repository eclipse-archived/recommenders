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
package org.eclipse.recommenders.internal.server.console;

import static org.eclipse.recommenders.utils.Checks.ensureIsDirectory;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.commons.client.ClientConfiguration;
import org.eclipse.recommenders.commons.client.WebServiceClient;
import org.osgi.framework.Bundle;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

public class ConsoleGuiceModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @LocalCouchdb
    public WebServiceClient provideLocalCouchdbClient() {
        return new WebServiceClient(ClientConfiguration.create("http://localhost:5984"));

    }

    @Provides
    @LocalCouchdb
    public File provideLocalCouchdbConfigDataBasedir() throws IOException {
        final Bundle setupBundle = ensureIsNotNull(Platform.getBundle("org.eclipse.recommenders.server.setup"));
        final File bundleLocation = FileLocator.getBundleFile(setupBundle);
        ensureIsDirectory(bundleLocation);

        final File basedir = new File(bundleLocation.getAbsoluteFile(), "couchdb");
        ensureIsDirectory(basedir);
        return basedir;
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER, ElementType.METHOD })
    @Inherited
    @BindingAnnotation
    public static @interface LocalCouchdb {

    }
}

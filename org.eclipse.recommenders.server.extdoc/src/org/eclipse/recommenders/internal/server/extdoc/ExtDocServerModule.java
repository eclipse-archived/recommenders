/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.extdoc;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import org.eclipse.recommenders.server.extdoc.CallsServer;
import org.eclipse.recommenders.server.extdoc.CodeExamplesServer;
import org.eclipse.recommenders.server.extdoc.SubclassingServer;
import org.eclipse.recommenders.server.extdoc.WikiServer;

public final class ExtDocServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CallsServer.class).in(Scopes.SINGLETON);
        bind(CodeExamplesServer.class).in(Scopes.SINGLETON);
        bind(SubclassingServer.class).in(Scopes.SINGLETON);
        bind(WikiServer.class).in(Scopes.SINGLETON);
    }
}

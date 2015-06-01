/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.types.rcp;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class TypesRcpModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ITypesIndexService.class).to(TypesIndexService.class).in(Scopes.SINGLETON);
        bind(IIndexProvider.class).to(IndexProvider.class).in(Scopes.SINGLETON);
    }
}

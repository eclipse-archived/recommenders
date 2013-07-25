/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.calls.rcp;

import org.eclipse.recommenders.calls.ICallModelProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Scopes;

public class CallsRcpModule extends AbstractModule implements Module {

    public CallsRcpModule() {
    }

    @Override
    protected void configure() {
        bind(ICallModelProvider.class).to(RcpCallModelProvider.class).in(Scopes.SINGLETON);
    }
}

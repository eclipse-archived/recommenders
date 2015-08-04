/**
 * Copyright (c) 2015 Codetrails GmbH. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp;

import javax.inject.Inject;

import org.eclipse.recommenders.news.rcp.INewsService;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

    @Inject
    private INewsService service;
    @Inject
    private NewsRcpPreferences preferences;

    public Startup() {
        NewsRcpModule.initiateContext(this);
    }

    @Override
    public void earlyStartup() {
        if (preferences.isEnabled()) {
            service.start();
        }
    }

}

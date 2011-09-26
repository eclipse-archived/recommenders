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
package org.eclipse.recommenders.rcp.selfhosting;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConsoleCommands implements CommandProvider {

    private final Injector injector = Guice.createInjector(new CommandsGuiceModule());

    @Override
    public String getHelp() {
        return "generateModels";
    }

    public void _initCouchdb(final CommandInterpreter ci) throws Exception {
        final Callable<IStatus> cmd = injector.getInstance(InitalizeCouchdb.class);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    public void _generateModels(final CommandInterpreter ci) throws Exception {
        final Callable<IStatus> cmd = injector.getInstance(GenerateModels.class);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    public void _replicateDatabase(final CommandInterpreter ci) throws Exception {
        final String db = ensureIsNotNull(ci.nextArgument());
        final CopyDemoDatabase cmd = injector.getInstance(CopyDemoDatabase.class);
        cmd.setDatabaseName(db);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }
}

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

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConsoleCommands implements CommandProvider {

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @Inherited
    @BindingAnnotation
    public static @interface Usage {
        String value();
    }

    private final Injector injector = Guice.createInjector(new CommandsGuiceModule());

    @Override
    public String getHelp() {
        final StringBuilder sb = new StringBuilder();
        sb.append("---Code Recommenders - Server Management Commands---").append(LINE_SEPARATOR);

        for (final Method m : getClass().getDeclaredMethods()) {
            final String methodName = m.getName();
            final String commandPrefix = "_";
            if (!methodName.startsWith(commandPrefix)) {
                continue;
            }
            appendCommandName(sb, methodName);
            appendUsageInfoIfAvailable(sb, m);
        }
        return sb.toString();
    }

    private void appendCommandName(final StringBuilder sb, final String methodName) {
        sb.append("\t").append(removeStart(methodName, "_"));
    }

    private void appendUsageInfoIfAvailable(final StringBuilder sb, final Method m) {
        final Usage annotation = m.getAnnotation(Usage.class);
        if (annotation != null) {
            sb.append(" : ").append(annotation.value());
        }
        sb.append(LINE_SEPARATOR);
    }

    @Usage("no args.")
    public void _initCouchdb(final CommandInterpreter ci) throws Exception {
        final Callable<IStatus> cmd = injector.getInstance(InitalizeCouchdb.class);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    @Usage("no args.")
    public void _generateModels(final CommandInterpreter ci) throws Exception {
        final Callable<IStatus> cmd = injector.getInstance(GenerateModels.class);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    @Usage("replicateDatabase <dbName on demo server, e.g. 'extdoc', 'codesearch'>")
    public void _replicateDatabase(final CommandInterpreter ci) throws Exception {
        final String db = ensureIsNotNull(ci.nextArgument());
        final CopyDemoDatabase cmd = injector.getInstance(CopyDemoDatabase.class);
        cmd.setDatabaseName(db);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }
}

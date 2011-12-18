package org.eclipse.recommenders.internal.server.console;

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

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.recommenders.utils.rcp.StatusToString;

import com.google.common.collect.Maps;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class RecommendersCommandProvider implements CommandProvider {

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    @Inherited
    @BindingAnnotation
    public static @interface Usage {
        String value();
    }

    /**
     * command names to method implementing the command. We use reflection for executing the commands.
     * 
     * @see #_cr(CommandInterpreter) for details how reflection is used.
     */
    private static Map<String, Method> commands = Maps.newTreeMap();

    static {
        // populate command index
        for (final Method m : RecommendersCommandProvider.class.getDeclaredMethods()) {
            final Usage annotation = m.getAnnotation(Usage.class);
            if (annotation != null) {
                final String commandName = replace(m.getName(), "_", "-");
                commands.put(commandName, m);
            }
        }
    }

    private final Injector injector = Guice.createInjector(new ConsoleGuiceModule());

    public void _cr(final CommandInterpreter ci) throws Exception {
        final String subcommand = ci.nextArgument();
        final Method method = commands.get(subcommand);
        if (method != null) {
            method.invoke(this, ci);
        } else {
            ci.println("Unknown command. Use 'cr help' to get a list of all known commands.");
        }
    }

    @Override
    public String getHelp() {
        final StringBuilder sb = new StringBuilder();
        sb.append("---Code Recommenders - Server Management Commands---").append(LINE_SEPARATOR);

        for (final String command : commands.keySet()) {
            sb.append("\tcr ").append(command);
            final String help = getCommandUsageHelp(commands.get(command));
            sb.append(" : ").append(help);
            sb.append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private String getCommandUsageHelp(final Method method) {
        final Usage annotation = method.getAnnotation(Usage.class);
        return annotation.value();
    }

    @Usage("<no args>")
    @SuppressWarnings("unused")
    private void generate_models(final CommandInterpreter ci) throws Exception {
        final Callable<IStatus> cmd = injector.getInstance(GenerateModelsCommand.class);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    @Usage("<no args>")
    @SuppressWarnings("unused")
    private void initialize_database(final CommandInterpreter ci) throws Exception {
        final Callable<IStatus> cmd = injector.getInstance(InitalizeDatabaseCommand.class);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    @Usage("<dbName on demo server, e.g. 'extdoc', 'codesearch'>")
    @SuppressWarnings("unused")
    private void replicate_database(final CommandInterpreter ci) throws Exception {
        final String db = ensureIsNotNull(ci.nextArgument());
        final ReplicateDatabaseCommand cmd = injector.getInstance(ReplicateDatabaseCommand.class);
        cmd.setDatabaseName(db);
        final IStatus status = cmd.call();
        final String result = StatusToString.toString(status);
        ci.println(result);
    }

    @Usage("<no args>")
    @SuppressWarnings("unused")
    private void help(final CommandInterpreter ci) throws Exception {
        ci.println(getHelp());
    }
}

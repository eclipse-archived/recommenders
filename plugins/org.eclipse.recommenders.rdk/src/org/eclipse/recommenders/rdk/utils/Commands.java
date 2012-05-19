/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rdk.utils;

import static org.apache.felix.service.command.CommandProcessor.COMMAND_FUNCTION;
import static org.apache.felix.service.command.CommandProcessor.COMMAND_SCOPE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.Parameter;
import org.eclipse.recommenders.utils.annotations.Provisional;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Lists;

/**
 * Utility class for registering commands 
 */
@Provisional
public class Commands {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    public static @interface CommandProvider {

        String scope() default "cr";
    }

    /**
     * Registers the given command provider with the given context.
     * 
     * @see CommandProvider specifies the scope of the commands defined in a command provider.
     * @see Descriptor used to find commands in a command provider.
     * @see Parameter used to specify default values and argument prefixes of a command.
     */
    public static void registerAnnotatedCommand(BundleContext context, Object commandProvider) {
        Class<? extends Object> clazz = commandProvider.getClass();
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        findScope(clazz, props);
        findCommands(clazz, props);
        context.registerService(clazz.getName(), commandProvider, props);
    }

    private static void findCommands(Class<? extends Object> clazz, Dictionary<String, Object> props) {
        List<String> lst = Lists.newLinkedList();
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(Descriptor.class))
                continue;
            lst.add(m.getName());
        }
        String[] cmds = lst.toArray(new String[lst.size()]);
        props.put(COMMAND_FUNCTION, cmds);
    }

    private static void findScope(Class<? extends Object> clazz, Dictionary<String, Object> props) {
        CommandProvider ann = clazz.getAnnotation(CommandProvider.class);
        props.put(COMMAND_SCOPE, ann.scope());
    }
}

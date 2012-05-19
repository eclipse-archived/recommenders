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
package org.eclipse.recommenders.rdk;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.service.command.Descriptor;
import org.eclipse.recommenders.rdk.utils.Commands.CommandProvider;
import org.eclipse.recommenders.utils.annotations.Provisional;

import com.google.common.collect.Maps;

@CommandProvider
@Provisional
public class SettingsCommand {

    public static final String LOCAL_MODEL_REPO = "models.repo.local";
    public static final String REMOTE_MODEL_REPO = "models.repo.remote";

    // only one instance per system:
    private static Map<String, Object> map = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) map.get(key);
    }

    /**
     * Sets a value to the static code recommenders properties. Please note that value is not restricted
     * 
     * @param key
     * @param value
     */
    public void set(String key, Object value) {
        map.put(key, value);
    }

    @Descriptor("sets a property")
    public void setFile(String key, File value) {
        map.put(key, value);
    }

    @Descriptor("sets a property")
    public void setString(String key, String value) {
        map.put(key, value);
    }

    @Descriptor("sets a property")
    public void setInt(String key, int value) {
        map.put(key, value);
    }

    @Descriptor("sets a property")
    public void setBool(String key, boolean value) {
        map.put(key, value);
    }

    @Descriptor("returns a property")
    public Object getProp(String key) {
        return get(key);
    }

    @Descriptor("prints all defined properties")
    public void props() {
        System.out.printf("%d properties defined.\n\n", map.size());

        for (Entry<String, Object> e : map.entrySet()) {
            Object value = e.getValue();
            String type = value.getClass().getSimpleName();
            System.out.printf("\t%s: %s (%s)\n", e.getKey(), value, type);
        }
    }

    @Descriptor("prints all predefined properties")
    public void propshelp() throws IllegalArgumentException, IllegalAccessException {
        System.out.println("The following predefined properties exist.\n"
                + "Note that this list is necessarily incomplete and serves as hint only.\n");

        int expected = Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC;

        for (Field f : getClass().getDeclaredFields()) {
            if (expected == (expected & f.getModifiers())) {
                System.out.printf("\t'%s'\n", f.get(this));
            }
        }
    }
}

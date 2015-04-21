/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static org.eclipse.recommenders.utils.Logs.log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Optional;

public final class Reflections {

    private Reflections() {
        // Not meant to be instantiated
    }

    public static Optional<Field> getDeclaredField(@Nullable Class<?> declaringClass, @Nullable String name) {
        if (declaringClass == null || name == null) {
            return Optional.absent();
        }

        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            return Optional.of(field);
        } catch (Exception e) {
            log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, name);
            return Optional.absent();
        }
    }

    public static Optional<Method> getDeclaredMethod(@Nullable Class<?> declaringClass, @Nullable String name,
            @Nullable Class<?>... parameterTypes) {
        if (declaringClass == null || name == null || parameterTypes == null) {
            return Optional.absent();
        }

        try {
            Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (Exception e) {
            log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, name);
            return Optional.absent();
        }
    }

    public static Optional<Method> getDeclaredMethodWithAlternativeSignatures(@Nullable Class<?> declaringClass, @Nullable String name,
            @Nullable Class<?>[]... parameterTypesAlternatives) {
        if (declaringClass == null || name == null || parameterTypesAlternatives == null) {
            return Optional.absent();
        }

        for (Class<?>[] parameterTypesAlternative : parameterTypesAlternatives) {
            try {
                Method method = declaringClass.getDeclaredMethod(name, parameterTypesAlternative);
                method.setAccessible(true);
                return Optional.of(method);
            } catch (Exception e) {
                // Ignore and try next alternative.
            }
        }
        log(LogMessages.LOG_WARNING_REFLECTION_FAILED, name);
        return Optional.absent();
    }
}

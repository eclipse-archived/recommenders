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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Optional;

public final class Reflections {

    private Reflections() {
        // Not meant to be instantiated
    }

    public static Optional<Class<?>> loadClass(@Nullable ClassLoader loader, @Nullable String name) {
        return loadClass(false, loader, name);
    }

    public static Optional<Class<?>> loadClass(boolean isFunctionalityLimitedOnFailure, @Nullable ClassLoader loader,
            @Nullable String name) {
        if (loader == null || name == null) {
            return Optional.absent();
        }

        try {
            Class<?> clazz = loader.loadClass(name);
            return Optional.<Class<?>>of(clazz);
        } catch (ClassNotFoundException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, name);
            }
            return Optional.absent();
        }
    }

    public static Optional<Field> getDeclaredField(@Nullable Class<?> declaringClass, @Nullable String name) {
        return getDeclaredField(false, declaringClass, name);
    }

    public static Optional<Field> getDeclaredField(boolean isFunctionalityLimitedOnFailure,
            @Nullable Class<?> declaringClass, @Nullable String name) {
        if (declaringClass == null || name == null) {
            return Optional.absent();
        }

        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, name);
            }
            return Optional.absent();
        } catch (SecurityException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, name);
            } else {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, name);
            }
            return Optional.absent();
        }
    }

    public static <T> Optional<Constructor<T>> getDeclaredConstructor(@Nullable Class<T> declaringClass,
            @Nullable Class<?>... parameterTypes) {
        return getDeclaredConstructor(false, declaringClass, parameterTypes);
    }

    public static <T> Optional<Constructor<T>> getDeclaredConstructor(boolean isFunctionalityLimitedOnFailure,
            @Nullable Class<T> declaringClass, @Nullable Class<?>... parameterTypes) {
        if (declaringClass == null || parameterTypes == null) {
            return Optional.absent();
        }

        try {
            Constructor<T> constructor = declaringClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return Optional.of(constructor);
        } catch (NoSuchMethodException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, declaringClass);
            }
            return Optional.absent();
        } catch (SecurityException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, declaringClass);
            } else {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, declaringClass);
            }
            return Optional.absent();
        }
    }

    public static Optional<Method> getDeclaredMethod(@Nullable Class<?> declaringClass, @Nullable String name,
            @Nullable Class<?>... parameterTypes) {
        return getDeclaredMethod(false, declaringClass, name, parameterTypes);
    }

    public static Optional<Method> getDeclaredMethod(boolean isFunctionalityLimitedOnFailure,
            @Nullable Class<?> declaringClass, @Nullable String name, @Nullable Class<?>... parameterTypes) {
        if (declaringClass == null || name == null || parameterTypes == null) {
            return Optional.absent();
        }

        try {
            Method method = declaringClass.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (NoSuchMethodException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, name);
            }
            return Optional.absent();
        } catch (SecurityException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, e, name);
            } else {
                log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, name);
            }
            return Optional.absent();
        }
    }

    public static Optional<Method> getDeclaredMethodWithAlternativeSignatures(@Nullable Class<?> declaringClass,
            @Nullable String name, @Nullable Class<?>[]... parameterTypesAlternatives) {
        return getDeclaredMethodWithAlternativeSignatures(false, declaringClass, name, parameterTypesAlternatives);
    }

    public static Optional<Method> getDeclaredMethodWithAlternativeSignatures(boolean isFunctionalityLimitedOnFailure,
            @Nullable Class<?> declaringClass, @Nullable String name,
            @Nullable Class<?>[]... parameterTypesAlternatives) {
        if (declaringClass == null || name == null || parameterTypesAlternatives == null) {
            return Optional.absent();
        }

        for (Class<?>[] parameterTypesAlternative : parameterTypesAlternatives) {
            Optional<Method> declaredMethod = getDeclaredMethod(false, declaringClass, name, parameterTypesAlternative);
            if (declaredMethod.isPresent()) {
                return declaredMethod;
            }
        }

        if (isFunctionalityLimitedOnFailure) {
            log(LogMessages.LOG_WARNING_REFLECTION_FAILED_LIMITED_FUNCTIONALITY, name);
        }
        return Optional.absent();
    }
}

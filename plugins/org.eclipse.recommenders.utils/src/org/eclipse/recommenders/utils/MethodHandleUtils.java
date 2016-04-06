/**
 * Copyright (c) 2016 Codetrails GmbH.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import com.google.common.base.Optional;

public final class MethodHandleUtils {

    private MethodHandleUtils() {
        // Not meant to be instantiated
    }

    public static Optional<MethodHandle> getSuperMethodHandle(Lookup lookupCapability, String methodName,
            Class<?> returnType, Class<?>... argumentTypes) {
        return getSuperMethodHandle(false, lookupCapability, methodName, returnType, argumentTypes);
    }

    public static Optional<MethodHandle> getSuperMethodHandle(boolean isFunctionalityLimitedOnFailure,
            Lookup lookupCapability, String methodName, Class<?> returnType, Class<?>... argumentTypes) {
        Class<?> callerClass = lookupCapability.lookupClass();
        Class<?> superclass = callerClass.getSuperclass();
        try {
            return Optional.of(lookupCapability.findSpecial(superclass, methodName,
                    MethodType.methodType(returnType, argumentTypes), callerClass));
        } catch (NoSuchMethodException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_FAILED_TO_ACCESS_METHOD_REFLECTIVELY_LIMITED_FUNCTIONALITY, e, methodName,
                        superclass);
            }
            return Optional.absent();
        } catch (IllegalAccessException e) {
            if (isFunctionalityLimitedOnFailure) {
                log(LogMessages.LOG_WARNING_FAILED_TO_ACCESS_METHOD_REFLECTIVELY_LIMITED_FUNCTIONALITY, e, methodName,
                        superclass);
            } else {
                log(LogMessages.LOG_WARNING_FAILED_TO_ACCESS_METHOD_REFLECTIVELY, e, methodName, superclass);
            }
            return Optional.absent();
        }
    }
}

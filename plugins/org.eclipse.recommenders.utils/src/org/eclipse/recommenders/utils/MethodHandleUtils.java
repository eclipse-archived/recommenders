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
        try {
            Class<?> callerClass = lookupCapability.lookupClass();
            Class<?> superclass = callerClass.getSuperclass();
            return Optional.of(lookupCapability.findSpecial(superclass, methodName,
                    MethodType.methodType(returnType, argumentTypes), callerClass));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            log(LogMessages.LOG_WARNING_REFLECTION_FAILED, e, methodName);
            return Optional.absent();
        }
    }
}

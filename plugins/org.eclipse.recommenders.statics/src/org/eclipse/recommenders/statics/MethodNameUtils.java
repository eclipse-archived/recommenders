/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.statics;

import com.google.common.base.Optional;

public final class MethodNameUtils {

    private MethodNameUtils() {
    }

    public static Optional<String> extractVerb(String methodName) {
        if (isInternalName(methodName)) {
            return Optional.absent();
        }

        int start = 0;

        while (start < methodName.length() && isIgnoreablePrefix(methodName.charAt(start))) {
            start++;
        }

        int end = start;

        if (start < methodName.length() && Character.isUpperCase(methodName.charAt(start))) {
            end++;
        }

        while (end < methodName.length() && !isWordDelimiter(methodName.charAt(end))) {
            end++;
        }

        if (end - start > 1) {
            return Optional.of(methodName.substring(start, end).toLowerCase());
        } else {
            return Optional.absent();
        }
    }

    private static boolean isIgnoreablePrefix(char c) {
        return c == '_';
    }

    private static boolean isWordDelimiter(char c) {
        return Character.isDigit(c) || Character.isUpperCase(c) || c == '_';
    }

    private static boolean isInternalName(String methodName) {
        return methodName.contains("$");
    }
}

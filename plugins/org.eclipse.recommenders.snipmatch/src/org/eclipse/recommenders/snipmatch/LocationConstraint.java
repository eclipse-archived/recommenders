/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.snipmatch;

import java.util.Map;

import com.google.common.collect.Maps;

public enum LocationConstraint {

    NONE(-1, "none"),
    FILE(0, "file"),
    JAVA(1, "java"),
    JAVA_STATEMENTS(2, "java-statements"),
    JAVA_TYPE_MEMBERS(3, "java-members"),
    JAVADOC(4, "javadoc");

    private final int index;
    private final String visibleName;

    private static Map<Integer, LocationConstraint> map = Maps.newHashMap();

    static {
        for (LocationConstraint constraint : LocationConstraint.values()) {
            map.put(constraint.getIndex(), constraint);
        }
    }

    private LocationConstraint(int index, String visibleName) {
        this.index = index;
        this.visibleName = visibleName;
    }

    @Override
    public String toString() {
        return visibleName;
    }

    public int getIndex() {
        return index;
    }

    public static LocationConstraint valueOf(int index) {
        return map.get(index);
    }
}

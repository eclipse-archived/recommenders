/**
 * Copyright (c) 2018 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.utils.rcp;

import java.text.NumberFormat;

public final class Formatting {

    private static final NumberFormat PERMILLE_FORMAT;
    private static final NumberFormat PERCENT_FORMAT;

    static {
        PERMILLE_FORMAT = NumberFormat.getInstance();
        PERMILLE_FORMAT.setMaximumFractionDigits(2);

        PERCENT_FORMAT = NumberFormat.getInstance();
        PERCENT_FORMAT.setMaximumFractionDigits(0);
    }

    private Formatting() {
    }

    // Need to synchronize, as NumberFormat not thread-safe.
    public static synchronized String toPercentage(double probability) {
        NumberFormat format = Math.abs(probability) < 0.01d ? PERMILLE_FORMAT : PERCENT_FORMAT;
        return format.format(probability * 100) + '%';
    }
}

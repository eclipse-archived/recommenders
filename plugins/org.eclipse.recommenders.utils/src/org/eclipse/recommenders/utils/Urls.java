/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.utils;

import static org.apache.commons.lang3.StringUtils.removeEnd;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Throwables;

public final class Urls {

    public static String mangle(URL url) {
        return mangle(url.toExternalForm());
    }

    public static String mangle(String url) {
        return removeEnd(url.replaceAll("\\W", "_"), "_");
    }

    public static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw Throwables.propagate(e);
        }
    }

    private Urls() {
    }
}

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
package org.eclipse.recommenders.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Globs {

    public static Pattern compileRegex(String glob) {
        String regex = toRegex(glob);
        return Pattern.compile(regex);
    }

    private static String toRegex(String glob) {
        StringBuilder sb = new StringBuilder("^");
        for (char c : glob.toCharArray()) {
            switch (c) {
            case '*':
                sb.append(".*");
                break;
            case '?':
                sb.append('.');
                break;
            case '.':
                sb.append("\\.");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            default:
                sb.append(c);
            }
        }
        sb.append('$');
        return sb.toString();
    }

    public static boolean matches(String text, Pattern glob) {
        Matcher matcher = glob.matcher(text);
        return matcher.matches();
    }

    public static boolean matches(String text, String glob) {
        Pattern pattern = compileRegex(glob);
        return matches(text, pattern);
    }
}

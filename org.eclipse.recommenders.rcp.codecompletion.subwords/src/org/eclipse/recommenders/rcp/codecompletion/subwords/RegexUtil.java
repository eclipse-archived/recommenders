/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 *    Paul-Emmanuel Faidherbe - Completion generalization
 */
package org.eclipse.recommenders.rcp.codecompletion.subwords;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegexUtil {
	
	public static boolean checkStringMatchesPrefixPattern(final String prefix, String toTest) {
		final Pattern pattern = createRegexPatternFromPrefix(prefix);
		toTest = getTokensUntilFirstOpeningBracket(toTest);
		final Matcher m = pattern.matcher(toTest);
		final boolean matches = m.matches();
		return matches;
	}

    public static Pattern createRegexPatternFromPrefix(final String prefixToken) {

        final StringBuilder sb = new StringBuilder();
        sb.append(".*");
        for (final char c : prefixToken.toCharArray()) {
            if (Character.isUpperCase(c)) {
                // if upper case than match words containing this uppercase
                // letter only - the developer might have a clue what she is
                // looking for...
                sb.append(c);
            } else {
                // if not just search for any proposal containing this letter in
                // upper case OR lower case.
                final char lowerCase = Character.toUpperCase(c);
                sb.append("[").append(lowerCase).append(c).append("]");
            }
            sb.append(".*");
        }
        final String regex = sb.toString();
        final Pattern p = Pattern.compile(regex);
        return p;
    }

    public static String getTokensUntilFirstOpeningBracket(final char[] completion) {
        return getTokensUntilFirstOpeningBracket(String.valueOf(completion));
    }

    public static String getTokensUntilFirstOpeningBracket(final String completion) {
        return StringUtils.substringBefore(completion, "(");
    }

}

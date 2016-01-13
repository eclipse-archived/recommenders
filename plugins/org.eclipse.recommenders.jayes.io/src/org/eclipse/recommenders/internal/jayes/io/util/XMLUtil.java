/**
 * Copyright (c) 2011 Michael Kutschke.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Michael Kutschke - initial API and implementation.
 */
package org.eclipse.recommenders.internal.jayes.io.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XMLUtil {

    private XMLUtil() {
        // Not meant to be instantiated
    }

    /**
     * this method expects the attributes in pairwise name, value form e.g. </br>
     * attributes = [ "id", "12345", "size", "15" ]
     */
    public static void surround(int offset, StringBuilder bldr, String surroundingTag, String... attributes) {
        // TODO addTab
        bldr.insert(offset, '>');

        for (int i = 0; i < attributes.length; i += 2) { // insert in reverted
                                                         // order
            bldr.insert(offset, "\" ");
            bldr.insert(offset, attributes[i + 1]);
            bldr.insert(offset, "=\"");
            bldr.insert(offset, attributes[i]);
        }

        bldr.insert(offset, ' ');
        bldr.insert(offset, surroundingTag);
        bldr.insert(offset, '<');

        bldr.append("</");
        bldr.append(surroundingTag);
        bldr.append('>');
    }

    /**
     * adds a tab to every line
     *
     * @param text
     * @return
     */
    public static String addTab(String text) {
        return text.replaceAll("\n", "\n\t");
    }

    public static void emptyTag(StringBuilder stringBuilder, String tagname, String... attributes) {
        stringBuilder.append('<');
        stringBuilder.append(tagname);
        stringBuilder.append(' ');

        for (int i = 0; i < attributes.length; i += 2) {
            stringBuilder.append(attributes[i]);
            stringBuilder.append("=\"");
            stringBuilder.append(attributes[i + 1]);
            stringBuilder.append("\" ");
        }

        stringBuilder.append("/>");
    }

    public static String escape(String text) {
        String strPattern = "[^a-zA-Z0-9]+";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher m = pattern.matcher(text);
        StringBuffer buf = new StringBuffer(text.length());
        while (m.find()) {
            String replacement = toUTF8Hex(m.group());
            m.appendReplacement(buf, replacement);
        }
        m.appendTail(buf);
        return buf.toString();
    }

    private static String toUTF8Hex(String string) {
        try {
            byte[] bytes = string.getBytes("UTF-8");
            StringBuilder bldr = new StringBuilder(bytes.length * 3);
            for (byte bt : bytes) {
                bldr.append('_');
                bldr.append(Integer.toHexString(bt & 0xFF).toUpperCase(Locale.US));
            }
            return bldr.toString();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 encoding unsupported", e);
        }
    }

    public static String unescape(String text) {
        String strPattern = "(_[0-9A-F][0-9A-F])+";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher m = pattern.matcher(text);
        StringBuffer buf = new StringBuffer(text.length());
        while (m.find()) {
            String found = m.group();
            String percentEncoded = found.replace('_', '%');
            String replacement = found;
            try {
                replacement = URLDecoder.decode(percentEncoded, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 encoding unsupported", e);
            }
            m.appendReplacement(buf, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(buf);
        return buf.toString();
    }
}

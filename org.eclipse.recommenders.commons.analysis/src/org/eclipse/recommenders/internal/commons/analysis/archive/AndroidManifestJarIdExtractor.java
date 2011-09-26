/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - Completion proposals relevance benchmark
 */
package org.eclipse.recommenders.internal.commons.analysis.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.apache.commons.io.input.NullInputStream;
import org.eclipse.recommenders.commons.utils.Version;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;

import com.google.common.io.ByteStreams;

public class AndroidManifestJarIdExtractor extends JarIdExtractor {

    private static final String androidManifestFileName = "AndroidManifest.xml";
    private static final String versionCodeAttributeName = "versionCode";
    private static final String packageAttributeName = "package";
    private static final String manifestTagName = "manifest";

    @Override
    public void extract(final JarFile jarFile) throws Exception {
        final byte[] content = readBytes(jarFile);
        if (content.length == 0) {
            // do nothing.
        } else if (isCompressed(content)) {
            parseCompressedFile(new ByteArrayInputStream(content));
        } else {
            parseFromString(new String(content));
        }
    }

    private void parseFromString(final String string) {
        extractName(string);
        extractVersionCode(string);
    }

    private void extractName(final String xmlContent) {
        final String name = getGroup(packageAttributeName + "=\"(.+)\"", xmlContent);
        setName(name);
    }

    private void extractVersionCode(final String xmlContent) {
        final String versionCode = getGroup("android:versionCode=\"([0-9]+)\"", xmlContent);
        setVersion(Version.create(Integer.valueOf(versionCode), 0));
    }

    private byte[] readBytes(final JarFile jarFile) throws IOException {
        final InputStream is = getInputStream(jarFile);
        return ByteStreams.toByteArray(is);
    }

    private InputStream getInputStream(final JarFile jarFile) throws IOException {
        final ZipEntry entry = jarFile.getEntry(androidManifestFileName);
        return entry == null ? new NullInputStream(0) : jarFile.getInputStream(entry);
    }

    private void parseCompressedFile(final InputStream is) {
        final AXmlResourceParser parser = new AXmlResourceParser();
        try {
            parser.setInput(is, null);
        } catch (final XmlPullParserException e1) {
            throw new RuntimeException(e1);
        }
        int elementType;
        try {
            while ((elementType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (elementType != XmlPullParser.START_TAG) {
                    continue;
                }
                final String tag = parser.getName();
                if (!tag.equals(manifestTagName)) {
                    continue;
                }

                final String packageName = getAttributeValue(packageAttributeName, parser);
                setName(packageName);

                final String version = getAttributeValue(versionCodeAttributeName, parser);
                setVersion(Version.create(Integer.valueOf(version), 0));
            }
        } catch (final XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getAttributeValue(final String attributeName, final AXmlResourceParser parser) {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeName(i).equals(attributeName)) {
                return getAttributeValue(parser, i);
            }
        }
        return null;
    }

    private String getAttributeValue(final AXmlResourceParser parser, final int index) {
        final int type = parser.getAttributeValueType(index);
        if (type == TypedValue.TYPE_STRING) {
            return parser.getAttributeValue(index);
        }
        if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT) {
            return String.valueOf(parser.getAttributeValueData(index));
        }
        return null;
    }

    private boolean isCompressed(final byte[] xml) {
        final String xmlDocumentStart = "<?xml";
        return !containsSequence(xml, xmlDocumentStart.getBytes());
    }

    private boolean containsSequence(final byte[] array, final byte[] sequence) {
        for (int offset = 0; offset + sequence.length < array.length; offset++) {
            if (beginsWithSequence(array, sequence, offset)) {
                return true;
            }
        }
        return false;
    }

    private boolean beginsWithSequence(final byte[] array, final byte[] sequence, final int offset) {
        for (int x = 0; x < sequence.length; x++) {
            if (!(array[offset + x] == sequence[x])) {
                return false;
            }
        }
        return true;
    }

    private String getGroup(final String regExp, final String inputString) {
        final Pattern p = Pattern.compile(regExp);
        final Matcher m = p.matcher(inputString);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

}

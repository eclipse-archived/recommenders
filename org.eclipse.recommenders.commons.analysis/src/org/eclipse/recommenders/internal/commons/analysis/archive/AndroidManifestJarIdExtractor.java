/**
 * Copyright (c) 2011 Darmstadt University of Technology.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.eclipse.recommenders.commons.utils.Version;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;

public class AndroidManifestJarIdExtractor extends JarIdExtractor {

    private static final String androidManifestFileName = "AndroidManifest.xml";
    private static final String versionCodeAttributeName = "versionCode";
    private static final String packageAttributeName = "package";
    private static final String manifestTagName = "manifest";

    @Override
    public void extract(JarFile jarFile) throws Exception {
        byte[] content = readBytes(jarFile);
        if (isCompressed(content))
            parseCompressedFile(new ByteArrayInputStream(content));
        else {
            parseFromString(new String(content));
        }
    }

    private void parseFromString(String string) {
        extractName(string);
        extractVersionCode(string);
    }

    private void extractName(String xmlContent) {
        String name = getGroup(packageAttributeName + "=\"(.+)\"", xmlContent);
        setName(name);
    }

    private void extractVersionCode(String xmlContent) {
        String versionCode = getGroup("android:versionCode=\"([0-9]+)\"", xmlContent);
        setVersion(Version.create(Integer.valueOf(versionCode), 0));
    }

    private byte[] readBytes(JarFile jarFile) throws IOException {
        InputStream is = getInputStream(jarFile);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeContentToOutputStream(is, out);

        return out.toByteArray();
    }

    private void writeContentToOutputStream(InputStream is, OutputStream out) throws IOException {
        byte[] xml = new byte[1024];
        int available;
        while ((available = is.read(xml)) > 0) {
            out.write(xml, 0, available);
        }
    }

    private InputStream getInputStream(JarFile jarFile) throws IOException {
        ZipEntry entry = jarFile.getEntry(androidManifestFileName);
        return jarFile.getInputStream(entry);
    }

    private void parseCompressedFile(InputStream is) {
        AXmlResourceParser parser = new AXmlResourceParser();
        try {
            parser.setInput(is, null);
        } catch (XmlPullParserException e1) {
            throw new RuntimeException(e1);
        }
        int elementType;
        try {
            while ((elementType = parser.next()) != XmlPullParser.END_DOCUMENT) {
                if (elementType != XmlPullParser.START_TAG)
                    continue;
                String tag = parser.getName();
                if (!tag.equals(manifestTagName)) {
                    continue;
                }

                String packageName = getAttributeValue(packageAttributeName, parser);
                setName(packageName);

                String version = getAttributeValue(versionCodeAttributeName, parser);
                setVersion(Version.create(Integer.valueOf(version), 0));
            }
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getAttributeValue(String attributeName, AXmlResourceParser parser) {
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            if (parser.getAttributeName(i).equals(attributeName)) {
                return getAttributeValue(parser, i);
            }
        }
        return null;
    }

    private String getAttributeValue(AXmlResourceParser parser, int index) {
        int type = parser.getAttributeValueType(index);
        if (type == TypedValue.TYPE_STRING)
            return parser.getAttributeValue(index);
        if (type >= TypedValue.TYPE_FIRST_INT && type <= TypedValue.TYPE_LAST_INT) {
            return String.valueOf(parser.getAttributeValueData(index));
        }
        return null;
    }

    private boolean isCompressed(byte[] xml) {
        String xmlDocumentStart = "<?xml";
        return !containsSequence(xml, xmlDocumentStart.getBytes());
    }

    private boolean containsSequence(byte[] array, byte[] sequence) {
        for (int offset = 0; offset + sequence.length < array.length; offset++) {
            if (beginsWithSequence(array, sequence, offset))
                return true;
        }
        return false;
    }

    private boolean beginsWithSequence(byte[] array, byte[] sequence, int offset) {
        for (int x = 0; x < sequence.length; x++) {
            if (!(array[offset + x] == sequence[x])) {
                return false;
            }
        }
        return true;
    }

    private String getGroup(String regExp, String inputString) {
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(inputString);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

}

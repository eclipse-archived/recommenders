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

import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.Files.newInputStreamSupplier;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import static org.apache.commons.io.filefilter.FileFileFilter.FILE;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.utils.annotations.Nullable;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IPackageName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

public class Zips {

    public static ZipFile NULL() {
        try {
            File tmp = File.createTempFile("recommenders_null_zip", ".zip");
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmp));
            zos.putNextEntry(new ZipEntry("/"));
            zos.closeEntry();
            zos.close();
            return new ZipFile(tmp) {
                @Override
                public void close() throws IOException {
                    super.close();
                }
            };
        } catch (Exception e) {
            return null;
        }
    }

    public static void unzip(File zipFile, File destFolder) throws IOException {
        ZipInputStream zis = null;
        try {
            InputSupplier<FileInputStream> fis = Files.newInputStreamSupplier(zipFile);
            zis = new ZipInputStream(fis.getInput());
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    final File file = new File(destFolder, entry.getName());
                    Files.createParentDirs(file);
                    Files.write(ByteStreams.toByteArray(zis), file);
                }
            }
        } finally {
            Closeables.closeQuietly(zis);
        }
    }

    public static void zip(File directory, File out) throws IOException {
        ZipOutputStream zos = null;
        try {
            OutputSupplier<FileOutputStream> s = Files.newOutputStreamSupplier(out);
            zos = new ZipOutputStream(s.getOutput());
            for (File f : FileUtils.listFiles(directory, FILE, DIRECTORY)) {
                String path = removeStart(f.getPath(), directory.getAbsolutePath() + File.separator);
                ZipEntry e = new ZipEntry(path);
                zos.putNextEntry(e);
                byte[] data = Files.toByteArray(f);
                zos.write(data);
                zos.closeEntry();
            }
        } finally {
            Closeables.close(zos, false);
        }
    }

    /**
     * Creates a standard path for the given type name. Package names are replaced by "/". The final name is constructed
     * as follows: "&lt;package&lg;/&lt;className&lg;&lt;extension&lg;
     * <p>
     * Note: if the path should contain a "." before the extension, it needs to be specified in the extension (e.g., by
     * specifying ".json" instead of just using "json").
     */
    public static String path(ITypeName type, @Nullable String suffix) {
        String name = StringUtils.removeStart(type.getIdentifier(), "L");
        return suffix == null ? name : name + suffix;
    }

    /**
     * Returns a path representing this package. The path is actually the package identifier itself.
     * 
     * @see IPackageName#getIdentifier()
     */
    public static String path(IPackageName pkg, @Nullable String suffix) {
        String name = pkg.getIdentifier();
        return suffix == null ? name : name + suffix;
    }

    public static ITypeName type(ZipEntry entry, @Nullable String suffix) {
        String name = StringUtils.removeEnd(entry.getName(), suffix);
        return VmTypeName.get("L" + name);
    }

    public static IMethodName method(ZipEntry e, String suffix) {
        String name = "L" + StringUtils.substringBefore(e.getName(), suffix);
        int start = name.lastIndexOf('/');
        char[] chars = name.toCharArray();
        chars[start] = '.';
        for (int i = start + 1; i < chars.length; i++) {
            if (chars[i] == '.') {
                chars[i] = '/';
            }
        }
        return VmMethodName.get(new String(chars));
    }

    public static String path(IMethodName method, @Nullable String suffix) {
        ITypeName type = method.getDeclaringType();
        String name = path(type, null) + "/" + method.getSignature().replaceAll("/", ".");
        return suffix == null ? name : name + suffix;
    }

    /**
     * Appends the given data to the zip output stream using the specified path.
     */
    public static void append(ZipOutputStream zos, String path, String data) throws IOException {
        ZipEntry e = new ZipEntry(path);
        zos.putNextEntry(e);
        zos.write(data.getBytes(Charsets.UTF_8));
        zos.closeEntry();
    }

    /**
     * Reads the give file into memory. This method may be used by zip based recommenders to speed up data access.
     */
    public static byte[] readFully(File file) throws IOException {
        return toByteArray(newInputStreamSupplier(file));
    }

    /**
     * Closes the give zip. Exceptions are printed to System.err.
     */
    public static boolean closeQuietly(ZipFile z) {
        if (z == null) {
            return true;
        }
        try {
            z.close();
            return true;
        } catch (IOException e) {
            System.err.printf("Failed to close zip '%s'. Caught exception printed below.\n", z.getName());
            e.printStackTrace();
            return false;
        }
    }
}

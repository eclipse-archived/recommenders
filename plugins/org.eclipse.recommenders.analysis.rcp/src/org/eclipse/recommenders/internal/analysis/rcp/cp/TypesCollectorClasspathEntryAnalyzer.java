/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.analysis.rcp.cp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.recommenders.internal.analysis.ClasspathEntry;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeReference;
import org.eclipse.recommenders.utils.Fingerprints;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

import com.google.common.collect.Sets;

public class TypesCollectorClasspathEntryAnalyzer implements IClasspathEntryAnalyzer {

    @Override
    public void analyze(final IClasspathEntry jdtEntry, final ClasspathEntry recEntry) {
        final Set<TypeReference> types = Sets.newTreeSet();

        try {
            final JarFile jar = new JarFile(recEntry.location);
            System.out.println("scanning " + recEntry.location);
            for (final ZipEntry e : Collections.list(jar.entries())) {
                if (!isClassFile(e)) {
                    continue;
                }
                final String typeNameLiteral = "L" + StringUtils.substringBeforeLast(e.getName(), ".");
                final ITypeName typeName = VmTypeName.get(typeNameLiteral);
                final InputStream is = jar.getInputStream(e);
                final String sha1 = Fingerprints.sha1(is);
                types.add(TypeReference.create(typeName, sha1));
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!types.isEmpty()) {
            recEntry.types = types;
        }
    }

    private boolean isClassFile(final ZipEntry e) {
        return e.getName().endsWith(".class");
    }

}

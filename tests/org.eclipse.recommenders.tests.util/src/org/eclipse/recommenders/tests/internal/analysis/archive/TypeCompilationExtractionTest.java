/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.internal.analysis.archive;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.jar.JarFile;

import junit.framework.Assert;

import org.eclipse.recommenders.tests.JarFileMockBuilder;
import org.eclipse.recommenders.utils.archive.ClassId;
import org.eclipse.recommenders.utils.archive.ClassIdExtractor;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;

public class TypeCompilationExtractionTest {

    @Test
    public void testTypeExtraction() throws Exception {
        final JarFileMockBuilder builder = new JarFileMockBuilder();
        builder.addEntry("package/ClassName.class", new ByteArrayInputStream("hash".getBytes()));
        final JarFile jarFile = builder.build();

        final ClassIdExtractor extractor = new ClassIdExtractor();
        extractor.extract(jarFile);

        Assert.assertEquals(1, extractor.getClassIds().size());
        final Iterator<ClassId> iterator = extractor.getClassIds().iterator();
        final ClassId typeCompilation = iterator.next();
        Assert.assertEquals(VmTypeName.get("Lpackage/ClassName"), typeCompilation.typeName);
        Assert.assertEquals("2346ad27d7568ba9896f1b7da6b5991251debdf2", typeCompilation.fingerprint);
    }

    @Test
    public void testNonTypes() throws Exception {
        final JarFileMockBuilder builder = new JarFileMockBuilder();
        builder.addEntry("package/ClassName.java", new ByteArrayInputStream("hash".getBytes()));
        builder.addEntry("package/SomeName.jpg", new ByteArrayInputStream("hash".getBytes()));
        builder.addEntry("package/SomeName", new ByteArrayInputStream("hash".getBytes()));

        final ClassIdExtractor extractor = new ClassIdExtractor();
        extractor.extract(builder.build());
        Assert.assertEquals(0, extractor.getClassIds().size());
    }
}

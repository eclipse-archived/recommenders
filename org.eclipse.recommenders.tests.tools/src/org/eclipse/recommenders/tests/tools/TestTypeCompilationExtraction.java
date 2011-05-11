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
package org.eclipse.recommenders.tests.tools;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import junit.framework.Assert;

import org.eclipse.recommenders.tools.TypeCompilation;
import org.eclipse.recommenders.tools.TypeCompilationExtractor;
import org.junit.Test;

public class TestTypeCompilationExtraction {

    @Test
    public void testTypeExtraction() throws Exception {
        final TypeCompilationExtractor extractor = new TypeCompilationExtractor();
        extractor.extract("package/ClassName.class", new ByteArrayInputStream("hash".getBytes()));

        Assert.assertEquals(1, extractor.getTypes().size());
        final Iterator<TypeCompilation> iterator = extractor.getTypes().iterator();
        final TypeCompilation typeCompilation = iterator.next();
        Assert.assertEquals("package.ClassName", typeCompilation.typeName);
        Assert.assertEquals("2346ad27d7568ba9896f1b7da6b5991251debdf2", typeCompilation.fingerprint);
    }

    @Test
    public void testNonTypes() throws Exception {
        final TypeCompilationExtractor extractor = new TypeCompilationExtractor();
        extractor.extract("package/ClassName.java", new ByteArrayInputStream("hash".getBytes()));
        extractor.extract("package/SomeName.jpg", new ByteArrayInputStream("hash".getBytes()));
        extractor.extract("package/SomeName", new ByteArrayInputStream("hash".getBytes()));

        Assert.assertEquals(0, extractor.getTypes().size());
    }
}

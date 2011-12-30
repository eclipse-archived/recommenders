/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.internal.analysis;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.recommenders.internal.analysis.analyzers.ZipCompilationUnitConsumer;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.fixture.IAnalysisFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipCompilationUnitConsumerTest {

    private ZipCompilationUnitConsumer sut;

    @Before
    public void before() throws IOException {
        final IAnalysisFixture fixture = mock(IAnalysisFixture.class);
        when(fixture.getName()).thenReturn("dummy");
        sut = new ZipCompilationUnitConsumer(fixture);
    }

    @After
    public void after() {
        sut.close();
        sut.getDestination().deleteOnExit();
    }

    @Test
    public void testConsume() {
        final CompilationUnit cu = CompilationUnit.create();
        cu.name = "Lmy/class/X";
        sut.consume(cu);
        sut.close();
        assertTrue(sut.getDestination().length() > 5);
    }
}

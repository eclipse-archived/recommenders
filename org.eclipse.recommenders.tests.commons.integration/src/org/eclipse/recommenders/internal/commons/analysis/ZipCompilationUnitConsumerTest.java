package org.eclipse.recommenders.internal.commons.analysis;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.recommenders.internal.commons.analysis.analyzers.ZipCompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipCompilationUnitConsumerTest {

    private ZipCompilationUnitConsumer sut;

    @Before
    public void before() throws IOException {
        sut = new ZipCompilationUnitConsumer();
    }

    @After
    public void after() {
        sut.close();
        sut.getDestination().deleteOnExit();
    }

    @Test
    public void testConsume() {
        CompilationUnit cu = CompilationUnit.create();
        cu.name = "Lmy/class/X";
        sut.consume(cu);
        sut.close();
        assertTrue(sut.getDestination().length() > 5);
    }
}

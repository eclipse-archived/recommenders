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
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import org.eclipse.recommenders.internal.commons.analysis.analyzers.SimpleCompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.modules.SimpleCompilationUnitConsumerPluginModule;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.junit.Test;

public class SimpleCompilationUnitConsumerTest {

    SimpleCompilationUnitConsumer sut = new SimpleCompilationUnitConsumer();

    @Test
    public void testConsume() {
        sut.consume(CompilationUnit.create());
    }

    @Test
    public void testClose() {
        sut.close();
    }

    @Test(expected = Exception.class)
    public void testModule() {
        new SimpleCompilationUnitConsumerPluginModule().configure();
    }
}

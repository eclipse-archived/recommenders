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

import org.eclipse.recommenders.commons.utils.gson.GsonUtil;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;

public class SimpleCompilationUnitConsumer implements ICompilationUnitConsumer {
    int count = 1;

    @Override
    public void consume(final CompilationUnit compilationUnit) {
        System.out.println("--> consumed " + compilationUnit.name);
        GsonUtil.serialize(compilationUnit, System.out);
    }

    @Override
    public void close() {
        // no action needed
    }
}

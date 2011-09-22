/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.server.udc;

import java.util.List;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;

public class UpdateQuery {

    public CompilationUnit[] docs;

    public UpdateQuery(final CompilationUnit[] docs) {
        this.docs = docs;
    }

    public UpdateQuery(final List<CompilationUnit> docs) {
        this.docs = new CompilationUnit[docs.size()];
        int i = 0;
        for (final CompilationUnit unit : docs) {
            this.docs[i] = unit;
            i++;
        }
    }

}

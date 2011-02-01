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
package org.eclipse.recommenders.internal.commons.analysis.newsites;

import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

public class NewSiteReferenceForMethodReturn extends NewSiteReference {
    public static NewSiteReferenceForMethodReturn create(final int programCounter, final TypeReference declaredType,
            final MethodReference method) {
        return new NewSiteReferenceForMethodReturn(programCounter, declaredType, method);
    }

    public final MethodReference def;

    public NewSiteReferenceForMethodReturn(final int programCounter, final TypeReference declaredType,
            final MethodReference def) {
        super(programCounter, declaredType);
        this.def = def;
    }
}

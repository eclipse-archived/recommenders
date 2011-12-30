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
package org.eclipse.recommenders.internal.analysis.newsites;

import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

public class NewSiteReferenceForParameter extends NewSiteReference {
    public static NewSiteReferenceForParameter create(final int programCounter, final TypeReference declaredType,
            final MethodReference method, final int argumentIndex) {
        return new NewSiteReferenceForParameter(programCounter, declaredType, method, argumentIndex);
    }

    public final MethodReference method;

    public final int argumentIndex;

    public NewSiteReferenceForParameter(final int programCounter, final TypeReference declaredType,
            final MethodReference method, final int argumentIndex) {
        super(programCounter, declaredType);
        this.method = method;
        this.argumentIndex = argumentIndex;
    }
}

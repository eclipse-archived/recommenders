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
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;

public class NewSiteReferenceForField extends NewSiteReference {
    public static NewSiteReferenceForField create(final int programCounter, final TypeReference declaredType,
            final FieldReference field) {
        return new NewSiteReferenceForField(programCounter, declaredType, field);
    }

    public final FieldReference field;

    public NewSiteReferenceForField(final int programCounter, final TypeReference declaredType,
            final FieldReference field) {
        super(programCounter, declaredType);
        this.field = field;
    }
}

/**
 * Copyright (c) 2011 Sven Amann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.utils.names;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.eclipse.recommenders.utils.annotations.Provisional;
import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.collect.MapMaker;

@Provisional
public class VmAnnotation implements IAnnotation {

    private static Map<ITypeName /* annotationType */, VmAnnotation> index = new MapMaker().weakValues().makeMap();

    private ITypeName annotationType;

    public static synchronized IAnnotation get(ITypeName annotationType) {
        VmAnnotation res = index.get(annotationType);
        if (res == null) {
            res = new VmAnnotation(annotationType);
            index.put(annotationType, res);
        }
        return res;
    }

    @Testing("Outside of tests, VmAnnotations should be canonicalized through VmAnnotation#get(ITypeName)")
    protected VmAnnotation(ITypeName annotationType) {
        ensureIsNotNull(annotationType);
        this.annotationType = annotationType;
    }

    @Override
    public ITypeName getAnnotationType() {
        return annotationType;
    }

    @Override
    public String toString() {
        return "@" + annotationType.getIdentifier();
    }
}

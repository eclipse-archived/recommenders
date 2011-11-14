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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.recommenders.internal.analysis.utils.WalaNameUtils;
import org.junit.Test;

import com.ibm.wala.types.TypeName;

public class WalaNameUtilsTest {

    @Test
    public void testJava2wala_String() {
        TypeName res = WalaNameUtils.java2walaTypeName(String.class);
        assertEquals(TypeName.findOrCreate("Ljava/lang/String"), res);
    }

    @Test
    public void testJava2wala_Map$Entry() {
        TypeName res = WalaNameUtils.java2walaTypeName(Map.Entry.class);
        assertEquals(TypeName.findOrCreate("Ljava/util/Map$Entry"), res);
    }
}

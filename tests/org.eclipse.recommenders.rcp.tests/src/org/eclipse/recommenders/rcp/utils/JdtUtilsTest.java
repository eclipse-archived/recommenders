/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.utils;

import static com.google.common.base.Optional.absent;
import static org.eclipse.recommenders.rcp.utils.JdtUtils.findSuperclassName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Test;

public class JdtUtilsTest {

    @Test
    public void testToVMTypeName() throws JavaModelException {
        IType type = mock(IType.class);
        when(type.getSuperclassTypeSignature()).thenReturn(null, "Ljava.lang.Object;", "Ljava.util.List<String>;",
                "illegal.Value;");
        assertEquals(absent(), findSuperclassName(type));
        assertEquals("Ljava/lang/Object", findSuperclassName(type).get().getIdentifier());
        assertEquals("Ljava/util/List", findSuperclassName(type).get().getIdentifier());

        assertEquals(absent(), findSuperclassName(type));

        when(type.getSuperclassTypeSignature()).thenThrow(new RuntimeException());
        assertEquals(absent(), findSuperclassName(type));
    }

}

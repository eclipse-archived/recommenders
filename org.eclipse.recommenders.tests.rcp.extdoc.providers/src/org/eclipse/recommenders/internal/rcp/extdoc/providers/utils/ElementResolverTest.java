/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.utils.names.IFieldName;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.tests.commons.extdoc.TestTypeUtils;

import org.junit.Assert;
import org.junit.Test;

public final class ElementResolverTest {

    @Test
    public void testResolveName() {
        Assert.assertEquals(TestTypeUtils.getDefaultMethod(),
                ElementResolver.resolveName(TestTypeUtils.getDefaultJavaMethod()));
        Assert.assertEquals(TestTypeUtils.getDefaultType(),
                ElementResolver.resolveName(TestTypeUtils.getDefaultJavaType()));
    }

    @Test
    public void testToRecMethod() {
        final IMethodName method = ElementResolver.toRecMethod(TestTypeUtils.getDefaultJavaMethod());
        Assert.assertEquals(TestTypeUtils.getDefaultMethod(), method);
    }

    @Test
    public void testToJdtType() {
        final IType type = ElementResolver.toJdtType(TestTypeUtils.getDefaultType());
        Assert.assertEquals(TestTypeUtils.getDefaultJavaType(), type);
    }

    @Test
    public void testToRecType() {
        final ITypeName type = ElementResolver.toRecType(TestTypeUtils.getDefaultJavaType());
        Assert.assertEquals(TestTypeUtils.getDefaultType(), type);
    }

    @Test
    public void testToRecField() {
        final IFieldName field = ElementResolver.toRecField(TestTypeUtils.getDefaultField(),
                TestTypeUtils.getDefaultType());
        Assert.assertEquals(TestTypeUtils.getDefaultType(), field.getFieldType());
    }

}

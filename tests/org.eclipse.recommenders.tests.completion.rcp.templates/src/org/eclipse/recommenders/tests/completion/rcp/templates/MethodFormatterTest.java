/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp.templates;

import junit.framework.Assert;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.completion.rcp.templates.code.MethodFormatter;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Unit tests for covering the {@link MethodFormatter} class.
 */
public final class MethodFormatterTest {

    @Test
    public void testFormat() throws JavaModelException {
        final IMethodName methodName = TestUtils.getDefaultConstructorCall().getInvokedMethod();
        final String code = getMethodFormatterMock().format(methodName);
        final String expected = "${constructedType}(${intTest:link(0)}, ${arg:link(false, true)}, ${arg2}, ${arg3:var(org.eclipse.swt.widgets.Button)})";
        Assert.assertEquals(expected, code);
    }

    protected static MethodFormatter getMethodFormatterMock() {
        final IMethod jdtMethod = Mockito.mock(IMethod.class);
        try {
            Mockito.when(jdtMethod.getParameterNames()).thenReturn(new String[] { "intTest", "arg0", "arg1", "arg2", });
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
        Mockito.when(jdtMethod.getParameterTypes()).thenReturn(
                new String[] { "I", "Z", "Ljava/lang/String;", "Lorg/eclipse/swt/widgets/Button;", });

        final JavaElementResolver resolver = Mockito.mock(JavaElementResolver.class);
        Mockito.when(resolver.toJdtMethod(Matchers.any(IMethodName.class))).thenReturn(jdtMethod);
        return new MethodFormatter(resolver);
    }

}

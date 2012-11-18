/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.tests.completion.rcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.CompilerBindings;
import org.junit.Test;

import com.google.common.base.Optional;

public class CompilerBindingsTest {

    @Test
    public void testCanParseSimpleType() {
        final ReferenceBinding mock = createTypeBinding("Ltest/Class;");

        final Optional<ITypeName> actual = CompilerBindings.toTypeName(mock);
        assertTrue(actual.isPresent());
    }

    @Test
    public void testCanParsePrimitiveType() {
        final ReferenceBinding mock = createTypeBinding("J");

        final Optional<ITypeName> actual = CompilerBindings.toTypeName(mock);
        assertEquals(VmTypeName.LONG, actual.get());
    }

    @Test
    public void testCanParseMethodWithOneArgAndPrimitiveReturnType() {
        final MethodBinding mock = createMethodBinding("Ljava/lang/Object;.equals(Ljava/lang/Object;)Z");

        final Optional<IMethodName> actual = CompilerBindings.toMethodName(mock);
        assertEquals(VmMethodName.get("Ljava/lang/Object.equals(Ljava/lang/Object;)Z"), actual.get());
    }

    @Test
    public void testCanParseMethodWithoutArgsAndReturnType() {
        final MethodBinding mock = createMethodBinding("Lmy/package/Obj;.method()V");

        final Optional<IMethodName> actual = CompilerBindings.toMethodName(mock);
        assertEquals(VmMethodName.get("Lmy/package/Obj.method()V"), actual.get());
    }

    @Test
    public void testCanParseMethodWithPrimitiveArgsAndSimpleReturnType() {
        final MethodBinding mock = createMethodBinding("Lsome/Impl;.callee(IJZ)Lmy/arg/Type;");

        final Optional<IMethodName> actual = CompilerBindings.toMethodName(mock);
        assertEquals(VmMethodName.get("Lsome/Impl.callee(IJZ)Lmy/arg/Type;"), actual.get());
    }

    @Test
    public void testCanParseMethodWithPrimitiveAndSimpleTypeArgs() {
        final MethodBinding mock = createMethodBinding("Land/a/Last;.one(SLArg;C)V");

        final Optional<IMethodName> actual = CompilerBindings.toMethodName(mock);
        assertEquals(VmMethodName.get("Land/a/Last.one(SLArg;C)V"), actual.get());
    }

    @Test
    public void testCanParseConstructorWithoutArgs() {
        final MethodBinding mock = createMethodBinding("Ljava/lang/Object;.()V");

        final Optional<IMethodName> actual = CompilerBindings.toMethodName(mock);
        assertEquals(VmMethodName.get("Ljava/lang/Object.<init>()V"), actual.get());
    }

    @Test
    public void testCanParseConstructorWithArgs() {
        final MethodBinding mock = createMethodBinding("Lanother/Impl;.(ILsome/Instance;)V");

        final Optional<IMethodName> actual = CompilerBindings.toMethodName(mock);
        assertEquals(VmMethodName.get("Lanother/Impl.<init>(ILsome/Instance;)V"), actual.get());
    }

    private ReferenceBinding createTypeBinding(final String type) {
        final ReferenceBinding mock = mock(ReferenceBinding.class);
        when(mock.computeUniqueKey()).thenReturn(type.toCharArray());
        return mock;
    }

    private MethodBinding createMethodBinding(final String method) {
        final MethodBinding mock = mock(MethodBinding.class);
        when(mock.computeUniqueKey()).thenReturn(method.toCharArray());
        return mock;
    }
}

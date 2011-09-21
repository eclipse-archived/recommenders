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
package org.eclipse.recommenders.mining.extdoc;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;

import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.mining.extdocs.ClassOverrideDirectivesGenerator;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.junit.Test;

import com.google.common.collect.Maps;

public class ClassOverridesDirectivesGeneratorTest {

    ITypeName superclass = VmTypeName.get("LSuperclass");

    IMethodName m1 = VmMethodName.get(superclass.getIdentifier(), "m1()V");
    IMethodName m2 = VmMethodName.get(superclass.getIdentifier(), "m2()V");
    IMethodName m3 = VmMethodName.get(superclass.getIdentifier(), "m3()V");

    @Test
    public void testGenerator() {
        final ClassOverrideDirectivesGenerator sut = new ClassOverrideDirectivesGenerator(0.05);
        final CompilationUnit cu1 = create("Ls1", m1, m2);
        final CompilationUnit cu2 = create("Ls2", m1, m3);
        final ClassOverrideDirectives actual = sut.generate(superclass, asList(cu1, cu2));
        assertThat(actual.getNumberOfSubclasses(), is(2));

        final HashMap<IMethodName, Integer> expected = Maps.newHashMap();
        expected.put(m1, 2);
        expected.put(m2, 1);
        expected.put(m3, 1);
        assertThat(expected, is(actual.getOverrides()));

    }

    @Test
    public void testGeneratorWithMinimumThreshold() {
        final ClassOverrideDirectivesGenerator sut = new ClassOverrideDirectivesGenerator(0.7);
        final CompilationUnit cu1 = create("Ls1", m1, m2);
        final CompilationUnit cu2 = create("Ls2", m1, m3);
        final ClassOverrideDirectives actual = sut.generate(superclass, asList(cu1, cu2));
        assertThat(actual.getNumberOfSubclasses(), is(2));

        final HashMap<IMethodName, Integer> expected = Maps.newHashMap();
        expected.put(m1, 2);
        assertThat(expected, is(actual.getOverrides()));

    }

    private CompilationUnit create(final String subclassName, final IMethodName... overriddenMethods) {
        final CompilationUnit res = CompilationUnit.create();
        final VmTypeName subclass = VmTypeName.get(subclassName);
        res.primaryType = TypeDeclaration.create(subclass, superclass);
        for (final IMethodName overriddenMethod : overriddenMethods) {
            final VmMethodName methodName = VmMethodName.rebase(subclass, overriddenMethod);
            final MethodDeclaration m = MethodDeclaration.create(methodName);
            m.superDeclaration = overriddenMethod;
            res.primaryType.methods.add(m);
        }
        return res;
    }
}

/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.mining.extdoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.MethodPattern;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.mining.extdocs.ClassOverridePatternsGenerator;
import org.eclipse.recommenders.mining.extdocs.OverridesClusterer;
import org.eclipse.recommenders.utils.Bag;
import org.eclipse.recommenders.utils.HashBag;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ClassOverridePatternsGeneratorTest {

    private static final ITypeName TYPE_SUPERCLASS = VmTypeName.get("Lorg/eclipse/swt/widgets/Dialog");
    private static final ITypeName TYPE_SUBCLASS = VmTypeName.get("Lorg/eclipse/test/custom/MyDialog");
    private static final IMethodName METHOD_CLONE = VmMethodName.get(TYPE_SUBCLASS.getIdentifier(), "clone()V");
    private static final IMethodName METHOD_TOSTRING = VmMethodName.get(TYPE_SUBCLASS.getIdentifier(),
            "toString()Ljava/lang/String;");

    @Test
    public void testHappyPath() {
        final OverridesClusterer clusterer = createDefaultClusterer();
        final ClassOverridePatternsGenerator sut = createSut(clusterer);
        final List<CompilationUnit> cus = Lists.newArrayList(createCu(TYPE_SUPERCLASS, METHOD_CLONE));
        sut.generate(TYPE_SUPERCLASS, cus);

        final Bag<Set<IMethodName>> patternBag = HashBag.newHashBag();
        patternBag.add(getPatternSet(METHOD_CLONE));
        Mockito.verify(clusterer).cluster(patternBag);
    }

    @Test
    public void testMultiplePatternOccurence() {
        final OverridesClusterer clusterer = createDefaultClusterer();
        final ClassOverridePatternsGenerator sut = createSut(clusterer);
        final List<CompilationUnit> cus = Lists.newArrayList(createCu(TYPE_SUPERCLASS, METHOD_CLONE, METHOD_TOSTRING),
                createCu(TYPE_SUPERCLASS, METHOD_CLONE, METHOD_TOSTRING), createCu(TYPE_SUPERCLASS, METHOD_CLONE));
        sut.generate(TYPE_SUPERCLASS, cus);

        final Bag<Set<IMethodName>> patternBag = HashBag.newHashBag();
        patternBag.add(getPatternSet(METHOD_CLONE, METHOD_TOSTRING));
        patternBag.add(getPatternSet(METHOD_CLONE, METHOD_TOSTRING));
        patternBag.add(getPatternSet(METHOD_CLONE));
        Mockito.verify(clusterer).cluster(patternBag);
    }

    @Test
    public void testOverridePatternGenerationOnNoPatterns() {
        final OverridesClusterer clusterer = createDefaultClusterer();
        final ClassOverridePatternsGenerator sut = createSut(clusterer);

        final Optional<ClassOverridePatterns> patterns = sut.generate(TYPE_SUPERCLASS,
                new LinkedList<CompilationUnit>());
        assertFalse(patterns.isPresent());
    }

    @Test
    public void testOverridePatternGenerationWithPatterns() {
        final List<MethodPattern> resultList = Lists.newArrayList();
        final Map<IMethodName, Double> pattern = Maps.newHashMap();
        pattern.put(METHOD_CLONE, 0.7);
        pattern.put(METHOD_TOSTRING, 0.5);
        resultList.add(MethodPattern.create(2, pattern));

        final OverridesClusterer clusterer = createClusterer(resultList);
        final ClassOverridePatternsGenerator sut = createSut(clusterer);
        final Optional<ClassOverridePatterns> patterns = sut.generate(TYPE_SUPERCLASS,
                new LinkedList<CompilationUnit>());

        assertTrue(patterns.isPresent());
        final MethodPattern[] methodPatterns = patterns.get().getPatterns();
        assertEquals(1, methodPatterns.length);
        assertEquals(MethodPattern.create(2, pattern), methodPatterns[0]);
    }

    private HashSet<IMethodName> getPatternSet(final IMethodName... methodNames) {
        final HashSet<IMethodName> res = Sets.newHashSet();
        for (final IMethodName methodName : methodNames) {
            res.add(VmMethodName.rebase(TYPE_SUPERCLASS, methodName));
        }
        return res;
    }

    private CompilationUnit createCu(final ITypeName superclass, final IMethodName... methodNames) {
        final CompilationUnit cu = CompilationUnit.create();
        cu.primaryType = TypeDeclaration.create(TYPE_SUBCLASS, superclass);
        cu.primaryType.superclass = superclass;
        for (final IMethodName methodName : methodNames) {
            final MethodDeclaration methodDecl = MethodDeclaration.create(methodName);
            methodDecl.superDeclaration = VmMethodName.rebase(superclass, methodName);
            cu.primaryType.methods.add(methodDecl);
        }
        return cu;
    }

    private OverridesClusterer createDefaultClusterer() {
        final List<MethodPattern> resultList = Lists.newLinkedList();
        return createClusterer(resultList);
    }

    private OverridesClusterer createClusterer(final List<MethodPattern> resultList) {
        final OverridesClusterer clusterer = Mockito.mock(OverridesClusterer.class);
        final Bag<Set<IMethodName>> patternBag = Mockito.any();
        Mockito.when(clusterer.cluster(patternBag)).thenReturn(resultList);
        return clusterer;
    }

    private ClassOverridePatternsGenerator createSut(final OverridesClusterer clusterer) {
        return new ClassOverridePatternsGenerator(clusterer);
    }
}

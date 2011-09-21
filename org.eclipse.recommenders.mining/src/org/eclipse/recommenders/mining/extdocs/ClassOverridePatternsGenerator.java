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
package org.eclipse.recommenders.mining.extdocs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.commons.utils.Bag;
import org.eclipse.recommenders.commons.utils.HashBag;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.server.extdoc.types.ClassOverridePatterns;
import org.eclipse.recommenders.server.extdoc.types.MethodPattern;

import com.google.inject.Inject;

public class ClassOverridePatternsGenerator {

    private Bag<Set<IMethodName>> rawPatterns;
    private final OverridesClusterer clusterer;

    @Inject
    public ClassOverridePatternsGenerator(final OverridesClusterer clusterer) {
        this.clusterer = clusterer;
    }

    public Option<ClassOverridePatterns> generate(final ITypeName superclass, final Iterable<CompilationUnit> cus) {
        collectRawPatterns(cus);
        final List<MethodPattern> methodPatterns = clusterer.cluster(rawPatterns);
        return createOverridePatterns(superclass, methodPatterns);
    }

    private void collectRawPatterns(final Iterable<CompilationUnit> cus) {
        rawPatterns = HashBag.newHashBag();
        for (final CompilationUnit cu : cus) {
            collectRawPatterns(cu);
        }
    }

    private void collectRawPatterns(final CompilationUnit cu) {
        final Set<IMethodName> overrides = new HashSet<IMethodName>();
        for (final MethodDeclaration method : cu.primaryType.methods) {
            if (method.superDeclaration != null) {
                overrides.add(method.superDeclaration);
            }
        }
        if (overrides.size() > 0) {
            rawPatterns.add(overrides);
        }
    }

    private Option<ClassOverridePatterns> createOverridePatterns(final ITypeName superclass,
            final List<MethodPattern> methodPatterns) {
        if (methodPatterns.isEmpty()) {
            return Option.none();
        }
        final MethodPattern[] array = methodPatterns.toArray(new MethodPattern[0]);
        final ClassOverridePatterns p = ClassOverridePatterns.create(superclass, array);
        return Option.wrap(p);
    }
}

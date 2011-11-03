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
package org.eclipse.recommenders.mining.extdocs;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.TreeBag.newTreeBag;

import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.recommenders.commons.utils.TreeBag;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ClassOverrideDirectivesGenerator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final double minOverridesProbability;
    private TreeBag<IMethodName> overriddenMethods;
    private ITypeName superclass;
    private int numberOfSubclasses;

    public ClassOverrideDirectivesGenerator(final double minOverridesProbability) {
        this.minOverridesProbability = minOverridesProbability;
    }

    public Option<ClassOverrideDirectives> generate(final ITypeName superclass, final Iterable<CompilationUnit> cus) {
        this.superclass = superclass;
        this.overriddenMethods = newTreeBag();
        numberOfSubclasses = 0;

        log.debug("Generating class overrides directives for '{}'. ", superclass);
        for (final CompilationUnit cu : cus) {
            numberOfSubclasses++;
            visitOverriddenMethods(cu);
        }
        filterInfrequentMethods();
        return toDirective();
    }

    private void visitOverriddenMethods(final CompilationUnit cu) {
        final TypeDeclaration type = cu.primaryType;

        for (final MethodDeclaration method : type.methods) {
        	ensureIsNotNull(method.name, "method name is null");
            if (!method.name.isInit() && method.superDeclaration != null) {
                overriddenMethods.add(method.superDeclaration);
            }
        }
    }

    private Option<ClassOverrideDirectives> toDirective() {
        final ClassOverrideDirectives res = ClassOverrideDirectives.create(superclass, numberOfSubclasses,
                overriddenMethods.asMap());
        try {
            res.validate();
        } catch (final Exception e) {
            log.debug("class overrides directives generation failed for '{}'", superclass);
            return Option.none();
        }
        return Option.wrap(res);
    }

    private void filterInfrequentMethods() {
        for (final IMethodName method : Sets.newHashSet(overriddenMethods.elements())) {
            final int timesObserved = overriddenMethods.count(method);
            final double probability = timesObserved / (double) numberOfSubclasses;
            if (probability < minOverridesProbability) {
                overriddenMethods.removeAll(method);
            }
        }
    }

}

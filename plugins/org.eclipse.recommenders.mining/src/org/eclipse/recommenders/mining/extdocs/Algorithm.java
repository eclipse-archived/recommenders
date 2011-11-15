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

package org.eclipse.recommenders.mining.extdocs;

import java.util.List;
import java.util.Set;

import org.eclipse.recommenders.extdoc.transport.types.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.transport.types.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.transport.types.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.utils.Option;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;

public class Algorithm implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ISuperclassProvider superclassProvider;
    private final ICompilationUnitProvider cuProvider;
    private final IExtdocDirectiveConsumer consumer;
    private final ClassOverrideDirectivesGenerator directivesGenerator;
    private final ClassOverridePatternsGenerator patternsGenerator;
    private final MethodSelfcallDirectivesGenerator methodSelfcallGenerator;

    @Inject
    public Algorithm(final ISuperclassProvider superclassProvider, final ICompilationUnitProvider cuProvider,
            final IExtdocDirectiveConsumer consumer, final ClassOverrideDirectivesGenerator directivesGenerator,
            final ClassOverridePatternsGenerator patternsGenerator,
            final MethodSelfcallDirectivesGenerator methodSelfcallGenerator) {
        this.superclassProvider = superclassProvider;
        this.cuProvider = cuProvider;
        this.consumer = consumer;
        this.directivesGenerator = directivesGenerator;
        this.patternsGenerator = patternsGenerator;
        this.methodSelfcallGenerator = methodSelfcallGenerator;
    }

    @Override
    public void run() {
        log.info("Running Extdocs model generation (class-overrides-directives, class-overrides-patterns and method self-calls. No class self-calls yet.)");
        methodSelfcallGenerator.initialize();

        for (final ITypeName superclass : getSuperclasses()) {
            log.info("Running extdoc analysis on {}.", superclass);
            final Iterable<CompilationUnit> cus = cuProvider.getCompilationUnits(superclass);
            try {
                generateOverrideDirectives(superclass, cus);
                generateOverridePatterns(superclass, cus);
                methodSelfcallGenerator.analyzeCompilationUnits(cus);

                int i = 0;
                for (CompilationUnit cu : cus) {
                    i++;
                }
                log.info("{} done, {} compilation units processed.", superclass, i);
            } catch (final JsonParseException e) {
                log.warn("Entry already exists: %s", e.getMessage());
            } catch (final RuntimeException e) {
                final String msg = String.format("Exception during analyzing %s: %s", superclass.getClassName(),
                        e.getMessage());
                log.error(msg, e);
            }
        }

        generateMethodSelfcalls();
        log.info("Finished extdoc model generation.");
    }

    private Set<ITypeName> getSuperclasses() {
        log.debug("requesting superclasses...");
        Set<ITypeName> superclasses = superclassProvider.getSuperclasses();
        return superclasses;
    }

    private void generateOverrideDirectives(ITypeName superclass, Iterable<CompilationUnit> cus) {
        log.debug("generating override directives...");
        Option<ClassOverrideDirectives> optDirective = directivesGenerator.generate(superclass, cus);
        if (optDirective.hasValue()) {
            consumer.consume(optDirective.get());
        }
    }

    private void generateOverridePatterns(ITypeName superclass, Iterable<CompilationUnit> cus) {
        log.debug("generating override patterns...");
        Option<ClassOverridePatterns> optPattern = patternsGenerator.generate(superclass, cus);
        if (optPattern.hasValue()) {
            consumer.consume(optPattern.get());
        }
    }

    private void generateMethodSelfcalls() {
        log.debug("generating method selfcalls...");
        final List<MethodSelfcallDirectives> methodSelfcalls = methodSelfcallGenerator.generate();
        for (final MethodSelfcallDirectives methodSelfcallDirectives : methodSelfcalls) {
            consumer.consume(methodSelfcallDirectives);
        }
    }
}
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

import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
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
        for (final ITypeName superclass : superclassProvider.getSuperclasses()) {
            log.debug("Running extdoc analysis on {}.", superclass);
            final Iterable<CompilationUnit> cus = cuProvider.getCompilationUnits(superclass);
            try {
                final Optional<ClassOverrideDirectives> optDirective = directivesGenerator.generate(superclass, cus);
                if (optDirective.isPresent()) {
                    consumer.consume(optDirective.get());
                }
                final Optional<ClassOverridePatterns> optPattern = patternsGenerator.generate(superclass, cus);
                if (optPattern.isPresent()) {
                    consumer.consume(optPattern.get());
                }
                methodSelfcallGenerator.analyzeCompilationUnits(cus);
            } catch (final JsonParseException e) {
                log.warn("Entry already exists: %s", e.getMessage());
            } catch (final RuntimeException e) {
                final String msg = String.format("Exception during analyzing %s: %s", superclass.getClassName(),
                        e.getMessage());
                log.error(msg, e);
            }
        }

        final List<MethodSelfcallDirectives> methodSelfcalls = methodSelfcallGenerator.generate();
        for (final MethodSelfcallDirectives methodSelfcallDirectives : methodSelfcalls) {
            consumer.consume(methodSelfcallDirectives);
        }

        log.info("Finished extdoc model generation.");
    }
}

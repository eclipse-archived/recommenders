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

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;

public class Algorithm implements Runnable {

    private final ISuperclassProvider superclassProvider;
    private final ICompilationUnitProvider cuProvider;
    private final IExtdocDirectiveConsumer consumer;

    @Inject
    public Algorithm(final ISuperclassProvider superclassProvider, final ICompilationUnitProvider cuProvider,
            final IExtdocDirectiveConsumer consumer) {
        this.superclassProvider = superclassProvider;
        this.cuProvider = cuProvider;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        for (final ITypeName superclass : superclassProvider.getSuperclasses()) {
            final Iterable<CompilationUnit> cus = cuProvider.getCompilationUnits(superclass);
            final ClassOverrideDirectivesGenerator directivesGenerator = new ClassOverrideDirectivesGenerator(0.05);
            try {
                final ClassOverrideDirectives generatedDirectives = directivesGenerator.generate(superclass, cus);
                consumer.consume(generatedDirectives);
            } catch (final JsonParseException e) {
                System.out.println("warn: entry already exists:");
            } catch (final RuntimeException e) {
                System.out.printf("[err] %s: %s\n", superclass.getClassName(), e.getMessage());
            }
        }
    }
}

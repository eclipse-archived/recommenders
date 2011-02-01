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
package org.eclipse.recommenders.internal.rcp.wala;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Date;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.CompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ibm.wala.classLoader.IClass;

public class WalaCompiliationUnitAnalzyer {
    private final Set<CompilationUnitFinalizer> compilationUnitFinalizers;

    private final Set<ICompilationUnitConsumer> compilationUnitConsumers;

    private ICompilationUnit jdtCompilationUnit;

    private IClass walaClass;

    private CompilationUnit recCompilationUnit;

    private final Provider<WalaTypeAnalyzer> walaTypeAnalyzerProvider;

    @Inject
    public WalaCompiliationUnitAnalzyer(final Provider<WalaTypeAnalyzer> walaTypeAnalyzerProvider,
            final Set<CompilationUnitFinalizer> compilationUnitFinalizers,
            final Set<ICompilationUnitConsumer> compilationUnitConsumers) {
        this.walaTypeAnalyzerProvider = walaTypeAnalyzerProvider;
        this.compilationUnitFinalizers = compilationUnitFinalizers;
        this.compilationUnitConsumers = compilationUnitConsumers;
    }

    public void init(final ICompilationUnit jdtCompilationUnit, final IClass walaClass,
            final CompilationUnit recCompilationUnit) {
        ensureIsNotNull(jdtCompilationUnit);
        ensureIsNotNull(walaClass);
        ensureIsNotNull(recCompilationUnit);
        this.jdtCompilationUnit = jdtCompilationUnit;
        this.walaClass = walaClass;
        this.recCompilationUnit = recCompilationUnit;
    }

    public void run() throws JavaModelException {
        initCompilationUnit();
        analyzePrimaryType();
        finalizeCompilationUnit();
        publishCompilationUnit();
    }

    private void analyzePrimaryType() throws JavaModelException {
        recCompilationUnit.primaryType = TypeDeclaration.create();
        final WalaTypeAnalyzer walaTypeAnalyzer = walaTypeAnalyzerProvider.get();
        walaTypeAnalyzer.init(jdtCompilationUnit.findPrimaryType(), recCompilationUnit.primaryType, walaClass);
        walaTypeAnalyzer.run();
    }

    private void initCompilationUnit() {
        recCompilationUnit.analysedOn = new Date();
        recCompilationUnit.name = walaClass.getName().toString();
    }

    private void finalizeCompilationUnit() {
        for (final CompilationUnitFinalizer cuFinalizer : compilationUnitFinalizers) {
            cuFinalizer.finalizeClass(recCompilationUnit, walaClass);
        }
    }

    private void publishCompilationUnit() {
        for (final ICompilationUnitConsumer cuConsumer : compilationUnitConsumers) {
            cuConsumer.consume(recCompilationUnit);
        }
    }

    @Override
    public String toString() {
        return walaClass.getName().toString();
    }
}

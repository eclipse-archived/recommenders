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
package org.eclipse.recommenders.internal.rcp.analysis;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Date;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitConsumer;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.ICompilationUnitFinalizer;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ibm.wala.classLoader.IClass;

public class WalaCompiliationUnitAnalzyer {
    private final Set<ICompilationUnitFinalizer> compilationUnitFinalizers;

    private final Set<ICompilationUnitConsumer> compilationUnitConsumers;

    private ICompilationUnit jdtCompilationUnit;

    private IClass walaClass;

    private CompilationUnit recCompilationUnit;

    private final Provider<WalaTypeAnalyzer> walaTypeAnalyzerProvider;

    private IProgressMonitor monitor;

    @Inject
    public WalaCompiliationUnitAnalzyer(final Provider<WalaTypeAnalyzer> walaTypeAnalyzerProvider,
            final Set<ICompilationUnitFinalizer> compilationUnitFinalizers,
            final Set<ICompilationUnitConsumer> compilationUnitConsumers) {
        this.walaTypeAnalyzerProvider = walaTypeAnalyzerProvider;
        this.compilationUnitFinalizers = compilationUnitFinalizers;
        this.compilationUnitConsumers = compilationUnitConsumers;
    }

    public void run(final IProgressMonitor monitor) throws JavaModelException {
        this.monitor = monitor;
        initializeCompilationUnit();
        analyzePrimaryType();
        finalizeCompilationUnit();
        publishCompilationUnit();
    }

    private void initializeCompilationUnit() {
        recCompilationUnit.analysedOn = new Date();
        recCompilationUnit.name = walaClass.getName().toString();
    }

    private void analyzePrimaryType() throws JavaModelException {
        recCompilationUnit.primaryType = TypeDeclaration.create();
        final WalaTypeAnalyzer walaTypeAnalyzer = walaTypeAnalyzerProvider.get();
        walaTypeAnalyzer.initialize(jdtCompilationUnit.findPrimaryType(), recCompilationUnit.primaryType, walaClass);
        walaTypeAnalyzer.run(monitor);
    }

    private void finalizeCompilationUnit() {
        for (final ICompilationUnitFinalizer cuFinalizer : compilationUnitFinalizers) {
            cuFinalizer.finalizeClass(recCompilationUnit, walaClass, monitor);
        }
    }

    private void publishCompilationUnit() {
        for (final ICompilationUnitConsumer cuConsumer : compilationUnitConsumers) {
            cuConsumer.consume(recCompilationUnit);
        }
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

    @Override
    public String toString() {
        return walaClass.getName().toString();
    }
}

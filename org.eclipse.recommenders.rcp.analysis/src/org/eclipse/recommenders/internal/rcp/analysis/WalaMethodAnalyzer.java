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

import java.util.Set;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.internal.commons.analysis.analyzers.IMethodAnalyzer;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

public class WalaMethodAnalyzer {
    private final Set<IMethodAnalyzer> methodAnalyzers;

    private Entrypoint walaEntrypoint;

    private MethodDeclaration recMethod;

    private org.eclipse.jdt.core.IMethod jdtMethod;

    private final IClassHierarchyService cha;

    private final Provider<WalaTypeAnalyzer> walaTypeAnalyzerProvider;

    private IProgressMonitor monitor;

    @Inject
    public WalaMethodAnalyzer(final Set<IMethodAnalyzer> methodAnalyzers,
            final Provider<WalaTypeAnalyzer> walaTypeAnalyzer, final IClassHierarchyService cha) {
        this.methodAnalyzers = methodAnalyzers;
        this.walaTypeAnalyzerProvider = walaTypeAnalyzer;
        this.cha = cha;
    }

    public void initalize(final Entrypoint walaMethod, @Nullable final org.eclipse.jdt.core.IMethod jdtMethod,
            final MethodDeclaration recMethod) {
        ensureIsNotNull(walaMethod);
        ensureIsNotNull(recMethod);
        this.walaEntrypoint = walaMethod;
        this.jdtMethod = jdtMethod;
        this.recMethod = recMethod;
    }

    public void run(final IProgressMonitor monitor) throws JavaModelException {
        this.monitor = monitor;
        analyzeMethodInstructions();
        analyzeNestedTypes();
    }

    private void analyzeNestedTypes() throws JavaModelException {
        if (jdtMethod == null) {
            return;
        }
        for (final IJavaElement element : jdtMethod.getChildren()) {
            if (element instanceof IType) {
                final IType anonymousJdtType = (IType) element;
                final IClass anonymousWalaType = cha.getType(anonymousJdtType);
                if (anonymousWalaType == null) {
                    continue;
                }
                final TypeDeclaration anonymousRecType = TypeDeclaration.create();
                recMethod.nestedTypes.add(anonymousRecType);
                final WalaTypeAnalyzer walaTypeAnalyzer2 = walaTypeAnalyzerProvider.get();
                walaTypeAnalyzer2.initialize(anonymousJdtType, anonymousRecType, anonymousWalaType);
                walaTypeAnalyzer2.run(monitor);
            }
        }
    }

    private void analyzeMethodInstructions() {
        for (final IMethodAnalyzer methodAnalyzer : methodAnalyzers) {
            try {
                methodAnalyzer.analyzeMethod(walaEntrypoint, recMethod, monitor);
            } catch (final Exception e) {
                logErrorMessage(e);
            }
        }
    }

    private void logErrorMessage(final Exception e) {
        final Throwable rootCause = Throwables.getRootCause(e);
        if (rootCause instanceof CancellationException) {
            return;
        }
        final IMethod method = walaEntrypoint.getMethod();
        final String signature = method.getSignature();
        System.err.printf("exception in %s: %s\n", signature, e);
    }
}

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

import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.analysis.IClassHierarchyService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

@SuppressWarnings("rawtypes")
public class RcpAnalysisModule extends AbstractModule implements com.google.inject.Module {
    @Override
    protected void configure() {
        bindClassHierarchyService();
        final Multibinder<ICompilationUnitAnalyzer> binder = Multibinder.newSetBinder(binder(),
                ICompilationUnitAnalyzer.class);
        binder.addBinding().to(WalaCompilationUnitAnalyzerService.class).in(Singleton.class);
    }

    private void bindClassHierarchyService() {
        bind(IClassHierarchyService.class).to(WalaClassHierarchyService.class).in(Scopes.SINGLETON);
    }
}

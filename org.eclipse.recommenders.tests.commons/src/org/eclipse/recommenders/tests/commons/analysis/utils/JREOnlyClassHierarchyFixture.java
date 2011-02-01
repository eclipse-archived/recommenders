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
package org.eclipse.recommenders.tests.commons.analysis.utils;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnhandledException;

import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisFixture;
import org.eclipse.recommenders.internal.commons.analysis.fixture.IAnalysisScopeBuilder;
import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultAnalysisScopeBuilder;
import org.eclipse.recommenders.internal.commons.analysis.fixture.DefaultClassHierarchyBuilder;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class JREOnlyClassHierarchyFixture {

    private static IClassHierarchy INSTANCE;

    public static synchronized IClassHierarchy getInstance() {
        if (INSTANCE == null) {
            IAnalysisFixture fixture = JREOnlyAnalysisFixture.create();
            IAnalysisScopeBuilder scopeBuilder = DefaultAnalysisScopeBuilder.buildFromFixture(fixture);
            AnalysisScope scope = scopeBuilder.getAnalysisScope();
            DefaultClassHierarchyBuilder classHierarchyBuilder = new DefaultClassHierarchyBuilder(scope);
            try {
                INSTANCE = classHierarchyBuilder.getClassHierachy();
            } catch (Exception e) {
                throw throwUnhandledException(e);
            }
        }
        return INSTANCE;
    }
}

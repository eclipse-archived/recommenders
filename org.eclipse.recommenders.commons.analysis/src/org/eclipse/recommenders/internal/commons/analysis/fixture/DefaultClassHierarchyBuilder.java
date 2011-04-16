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
package org.eclipse.recommenders.internal.commons.analysis.fixture;

import com.google.inject.Inject;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class DefaultClassHierarchyBuilder {
    private final AnalysisScope scope;

    @Inject
    public DefaultClassHierarchyBuilder(final AnalysisScope scope) {
        this.scope = scope;
    }

    public IClassHierarchy getClassHierachy() throws ClassHierarchyException {
        final ClassLoaderFactory factory = new ClassLoaderFactoryImpl(scope.getExclusions());
        final ClassHierarchy cha = ClassHierarchy.make(scope, factory);
        return cha;
    }
}

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
package org.eclipse.recommenders.internal.rcp.views.recommendations;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.rcp.IRecommendation;

import com.google.common.collect.Multimap;

public interface IRecommendationsViewContentProvider {
    void attachRecommendations(ICompilationUnit jdtCompilationUnit, CompilationUnit recCompilationUnit,
            Multimap<Object, IRecommendation> recommendations);
}

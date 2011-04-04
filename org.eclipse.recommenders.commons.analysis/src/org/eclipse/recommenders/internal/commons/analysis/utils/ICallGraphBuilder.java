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
package org.eclipse.recommenders.internal.commons.analysis.utils;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

public interface ICallGraphBuilder {
    void buildMethodTargetSelector();

    void buildClassTargetSelector();

    void buildContextSelector();

    void buildContextInterpretor();

    void buildCallGraph();

    /*
     * XXX let's try to work without the wala callgraph builder directly. almost everything needed is the callgraph and
     * the pointer analysis.
     * 
     * CallGraphBuilder getCallGraphBuilder();
     */
    CallGraph getCallGraph();

    PointerAnalysis getPointerAnalysis();
}

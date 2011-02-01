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
package org.eclipse.recommenders.internal.commons.analysis.analyzers;

import java.util.Map;

import org.eclipse.recommenders.internal.commons.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.utils.HeapHelper;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public interface ICallGraphAnalyzer {
    void setMethodDeclaration(MethodDeclaration methodDecl);

    void analyzeCallgraph(CallGraph callGraph, HeapHelper heapHelper,
            Map<InstanceKey, ObjectInstanceKey> instanceKeyMapping);
}

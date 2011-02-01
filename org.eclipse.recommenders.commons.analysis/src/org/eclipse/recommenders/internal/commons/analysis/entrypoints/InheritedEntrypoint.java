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
package org.eclipse.recommenders.internal.commons.analysis.entrypoints;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

public class InheritedEntrypoint extends DefaultEntrypoint {
    private static final int THIS_ARGUMENT_INDEX = 0;

    private final IClass realClass;

    public InheritedEntrypoint(final IClass realClass, final IMethod method) {
        super(method, realClass.getClassHierarchy());
        this.realClass = realClass;
        resetThisParameter();
    }

    public InheritedEntrypoint(final IClass realClass, final MethodReference entryMethod) {
        super(entryMethod, realClass.getClassHierarchy());
        this.realClass = realClass;
        resetThisParameter();
    }

    private void resetThisParameter() {
        setParameterTypes(THIS_ARGUMENT_INDEX, new TypeReference[] { realClass.getReference() });
    }

    public IClass getRealClass() {
        return realClass;
    }

    @Override
    public String toString() {
        return String.format("InheritedEntrypoint [realClass=%s, method=%s]", realClass, getMethod());
    }
}

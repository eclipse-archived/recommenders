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
package org.eclipse.recommenders.tests.analysis;

import java.util.List;

import org.eclipse.recommenders.internal.analysis.entrypoints.IEntrypointSelector;
import org.eclipse.recommenders.internal.analysis.entrypoints.RecommendersEntrypoint;
import org.junit.Ignore;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

@Ignore
public class TestMethodEntrypointSelector implements IEntrypointSelector {

    @Override
    public List<Entrypoint> selectEntrypoints(IClass clazz) {
        List<Entrypoint> res = Lists.newArrayList();
        for (IMethod method : clazz.getDeclaredMethods()) {
            if (isTestMethod(method)) {
                RecommendersEntrypoint entrypoint = new RecommendersEntrypoint(method);
                res.add(entrypoint);
            }
        }
        return res;
    }

    private boolean isTestMethod(IMethod method) {
        return method.getName().toString().startsWith("__test");
    }
}

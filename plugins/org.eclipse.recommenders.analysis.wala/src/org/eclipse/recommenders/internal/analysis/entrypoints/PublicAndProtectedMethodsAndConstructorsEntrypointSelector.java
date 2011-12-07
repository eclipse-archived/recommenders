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
package org.eclipse.recommenders.internal.analysis.entrypoints;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;

/**
 * Selects all public and protected methods - including static methods.
 */
public class PublicAndProtectedMethodsAndConstructorsEntrypointSelector implements IEntrypointSelector {
    @Override
    public List<Entrypoint> selectEntrypoints(final IClass clazz) {
        final LinkedList<Entrypoint> entrypoints = Lists.newLinkedList();
        for (final IMethod method : clazz.getDeclaredMethods()) {
            if (method.isAbstract()) {
                continue;
            }
            if (method.isPublic() || method.isProtected()) {
                entrypoints.add(new RecommendersEntrypoint(method));
            }
        }
        return entrypoints;
    }
}

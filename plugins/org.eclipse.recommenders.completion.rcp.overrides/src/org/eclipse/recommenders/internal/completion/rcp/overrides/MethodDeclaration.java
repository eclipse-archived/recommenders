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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import java.util.Set;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.IName;

import com.google.common.collect.Sets;

public class MethodDeclaration {

    public static MethodDeclaration create() {
        final MethodDeclaration res = new MethodDeclaration();
        return res;
    }

    public static MethodDeclaration create(final IMethodName name) {
        final MethodDeclaration res = create();
        res.name = name;
        return res;
    }

    public IName getName() {
        return name;
    }

    public IMethodName name;

    public IMethodName superDeclaration;

    public IMethodName firstDeclaration;

    public int line;

    public Set<TypeDeclaration> nestedTypes = Sets.newHashSet();

    public int modifiers;

}

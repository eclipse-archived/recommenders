/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.rcp.utils.JdtUtils;

/**
 * Represents an {@link IType} in the call chain graph
 */
public class TypeNode {

    public List<MemberEdge> incomingEdges = new LinkedList<MemberEdge>();

    public IType type;

    public TypeNode(final IType type) {
        ensureIsNotNull(type);
        this.type = type;
    }

    @Override
    public boolean equals(final Object obj) {
        return type.equals(obj);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    public boolean isAssignable(final IType lhsType) {
        ensureIsNotNull(lhsType);
        return JdtUtils.isAssignable(lhsType, type);
    }

    @Override
    public String toString() {
        return "type: " + type.getElementName() + ", incoming edges:" + Arrays.deepToString(incomingEdges.toArray());
    }
}

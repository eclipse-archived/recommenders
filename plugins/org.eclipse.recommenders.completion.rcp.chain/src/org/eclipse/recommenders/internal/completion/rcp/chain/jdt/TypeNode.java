package org.eclipse.recommenders.internal.completion.rcp.chain.jdt;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IType;

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
        return InternalAPIsHelper.isAssignable(lhsType, type);
    }

    @Override
    public String toString() {
        return "type: " + type.getElementName() + ", incoming edges:" + Arrays.deepToString(incomingEdges.toArray());
    }
}

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

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findTypeFromSignature;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.recommenders.utils.rcp.JdtUtils;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;

import com.google.common.base.Optional;

/**
 * Represents a transition from Type A to Type B by some chain element ( {@link IField} access, {@link IMethod} call, or
 * {@link ILocalVariable} (as entrypoints only)).
 * 
 * @see TypeNode
 * @see GraphBuilder
 */
public class MemberEdge {

    public enum EdgeType {
        METHOD, FIELD, LOCAL_VARIABLE
    }

    private final IJavaElement element;
    /**
     * the iteration number (==depth) this edge was found in the graph construction phase
     */
    private final int iterationDepth;
    private final Optional<IType> optReceiverType;
    private Optional<IType> optReturnType;
    private int dimension;
    private EdgeType edgeType;

    // TODO I don't like var names sourceType javaelement... too generic
    public MemberEdge(final IType receiverType, final IJavaElement member, final int iterationDepth) {
        ensureIsNotNull(member);
        ensureIsMemberTypeOrLocalVariable(member);
        this.optReceiverType = fromNullable(receiverType);
        this.element = member;
        this.iterationDepth = iterationDepth;
        try {
            initializeReturnType();
        } catch (final JavaModelException e) {
            RecommendersUtilsPlugin.log(e);
        }
    }

    private void ensureIsMemberTypeOrLocalVariable(final IJavaElement element) {

    }

    public MemberEdge(final IJavaElement member) {
        this(null, member, 1);
    }

    private void initializeReturnType() throws JavaModelException {
        String typeSignature = null;
        switch (getEdgeElement().getElementType()) {
        case IJavaElement.FIELD:
            final IField field = getEdgeElement();
            typeSignature = field.getTypeSignature();
            edgeType = EdgeType.FIELD;
            break;
        case IJavaElement.LOCAL_VARIABLE:
            final ILocalVariable local = getEdgeElement();
            typeSignature = local.getTypeSignature();
            edgeType = EdgeType.LOCAL_VARIABLE;
            break;
        case IJavaElement.METHOD:
            final IMethod method = getEdgeElement();
            typeSignature = method.getReturnType();
            edgeType = EdgeType.METHOD;
            break;
        }
        dimension = Signature.getArrayCount(typeSignature.toCharArray());
        optReturnType = findTypeFromSignature(typeSignature, element);
    }

    /**
     * Instance of {@link IMethod}, {@link IField}, or {@link ILocalVariable}
     */
    @SuppressWarnings("unchecked")
    public <T extends IJavaElement> T getEdgeElement() {
        return (T) element;
    }

    @Override
    public String toString() {
        try {
            switch (getEdgeElement().getElementType()) {
            case IJavaElement.FIELD:
                return ((IField) getEdgeElement()).getKey();
            case IJavaElement.LOCAL_VARIABLE:
                return ((ILocalVariable) getEdgeElement()).getHandleIdentifier();
            case IJavaElement.METHOD:
                final IMethod m = getEdgeElement();
                return m.getElementName() + m.getSignature();
            }
        } catch (final JavaModelException e) {
            return e.toString();
        }
        return super.toString();
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public Optional<IType> getReturnType() {
        return optReturnType;
    }

    /**
     * Note, the receiver type is not necessarily identical to the declaring type of {@link #getEdgeElement()}! For
     * instance, a callchain may use a method {@code MyDialog.getWindowManager()} that's actually declared in its
     * superclass {@link Dialog#getWindowManager()}. In that case, the receiver type is MyDialog whereas the declaring
     * type is {@link Dialog}.
     */
    public Optional<IType> getReceiverType() {
        return optReceiverType;
    }

    public int getIterationDepth() {
        return iterationDepth;
    }

    public boolean isArray() {
        return dimension > 0;
    }

    public int getDimension() {
        return dimension;
    }

    public boolean isAssignableTo(final IType lhsType) {
        ensureIsNotNull(lhsType);
        if (optReturnType.isPresent()) {
            return JdtUtils.isAssignable(lhsType, optReturnType.get());
        }
        return false;
    }

    /**
     * A {@link MemberEdge} is an anchor (or root) of a call chain, iff the receiver type is null. This is a convention.
     * We could also store a receiver type (this in most cases) but it's not needed anywhere.
     */
    public boolean isChainAnchor() {
        return !optReceiverType.isPresent();
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MemberEdge) {
            final MemberEdge other = (MemberEdge) obj;
            return element.equals(other.element);
        }
        return super.equals(obj);
    }
}

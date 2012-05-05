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
import static org.eclipse.recommenders.utils.rcp.JdtUtils.findTypeFromSignature;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;

import com.google.common.base.Optional;

/**
 * Represents a transition from Type A to Type B by some chain element ( {@link IField} access, {@link IMethod} call, or
 * {@link ILocalVariable} (as entrypoints only)).
 * 
 * @see GraphBuilder
 */
public class MemberEdge {

    public enum EdgeType {
        METHOD, FIELD, LOCAL_VARIABLE
    }

    private final IJavaElement element;
    private Optional<IType> optReturnType;
    private int dimension;
    private EdgeType edgeType;

    public MemberEdge(final IJavaElement member) {
        element = ensureIsNotNull(member);
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
     * @return Instance of {@link IMethod}, {@link IField}, or {@link ILocalVariable}.
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
        if (optReturnType == null) {
            try {
                initializeReturnType();
            } catch (final JavaModelException e) {
                RecommendersUtilsPlugin.log(e);
            }
        }
        return optReturnType;
    }

    public int getDimension() {
        if (optReturnType == null) {
            getReturnType();
        }
        return dimension;
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

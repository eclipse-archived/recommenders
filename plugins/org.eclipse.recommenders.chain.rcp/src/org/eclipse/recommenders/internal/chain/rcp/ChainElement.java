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
package org.eclipse.recommenders.internal.chain.rcp;

import static org.eclipse.recommenders.internal.chain.rcp.l10n.LogMessages.WARNING_CANNOT_HANDLE_RETURN_TYPE;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Logs.log;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

/**
 * Represents a transition from Type A to Type B by some chain element ( {@link IField} access, {@link IMethod} call, or
 * {@link ILocalVariable} (as entrypoints only)).
 *
 * @see ChainFinder
 */
@SuppressWarnings("restriction")
public class ChainElement {

    public enum ElementType {
        METHOD,
        FIELD,
        LOCAL_VARIABLE
    }

    private final Binding element;
    private TypeBinding returnType;
    private int dimension;
    private ElementType elementType;

    private final boolean requireThis;

    public ChainElement(final Binding binding, final boolean requireThis) {
        element = ensureIsNotNull(binding);
        this.requireThis = requireThis;
        initializeReturnType();
    }

    private void initializeReturnType() {
        switch (element.kind()) {
        case Binding.FIELD:
            returnType = ((FieldBinding) element).type;
            elementType = ElementType.FIELD;
            break;
        case Binding.LOCAL:
            returnType = ((LocalVariableBinding) element).type;
            elementType = ElementType.LOCAL_VARIABLE;
            break;
        case Binding.METHOD:
            returnType = ((MethodBinding) element).returnType;
            elementType = ElementType.METHOD;
            break;
        default:
            log(WARNING_CANNOT_HANDLE_RETURN_TYPE, element);
        }
        dimension = returnType.dimensions();
    }

    @SuppressWarnings("unchecked")
    public <T extends Binding> T getElementBinding() {
        return (T) element;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public TypeBinding getReturnType() {
        return returnType;
    }

    public int getReturnTypeDimension() {
        return dimension;
    }

    public boolean requiresThisForQualification() {
        return requireThis;
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ChainElement) {
            final ChainElement other = (ChainElement) obj;
            return element.equals(other.element);
        }
        return false;
    }

    @Override
    public String toString() {
        if (elementType == ElementType.METHOD) {
            final MethodBinding m = (MethodBinding) element;
            return new StringBuilder().append(m.selector).append(m.signature()).toString();
        }
        return element.toString();
    }
}

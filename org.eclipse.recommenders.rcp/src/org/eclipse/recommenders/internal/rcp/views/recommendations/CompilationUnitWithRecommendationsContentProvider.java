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
package org.eclipse.recommenders.internal.rcp.views.recommendations;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.IRecommendation;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

@SuppressWarnings("unchecked")
public final class CompilationUnitWithRecommendationsContentProvider implements ITreeContentProvider {

    private Tuple<CompilationUnit, Multimap<Object, IRecommendation>> input;

    private Multimap<Object, IRecommendation> recommendations;

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {

        if (newInput instanceof Tuple<?, ?>) {
            input = (Tuple<CompilationUnit, Multimap<Object, IRecommendation>>) newInput;
            recommendations = ensureIsNotNull(input.getSecond());
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean hasChildren(final Object element) {
        if (element == null) {
            return false;
        } else if (element == input) {
            return true;
        } else if (element instanceof TypeDeclaration) {
            return !((TypeDeclaration) element).methods.isEmpty() || recommendations.containsKey(element);
        } else if (element instanceof MethodDeclaration) {
            return !((MethodDeclaration) element).getVariables().isEmpty() || recommendations.containsKey(element);
        } else if (element instanceof Variable) {
            return recommendations.containsKey(element);
        } else if (element instanceof IRecommendation) {
            return false;
        } else {
            throw throwUnexpectedTypeObserved(element);
        }
    }

    private IllegalStateException throwUnexpectedTypeObserved(final Object element) {
        return throwUnreachable("illegal argument type: '%s', value: '%s'", element.getClass(), element);
    }

    @Override
    public Object getParent(final Object element) {
        return null;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return new Object[] { input.getFirst().primaryType };
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof CompilationUnit) {
            return getChildrenCompilationUnit((CompilationUnit) parentElement);
        } else if (parentElement instanceof TypeDeclaration) {
            return getChildrenOfTypeDeclaration((TypeDeclaration) parentElement);
        } else if (parentElement instanceof MethodDeclaration) {
            return getChildrenOfMethodDeclaration((MethodDeclaration) parentElement);
        } else if (parentElement instanceof Variable) {
            return getChildrenOfVariable((Variable) parentElement);
        } else if (parentElement instanceof IRecommendation) {
            return new Object[0];
        } else {
            throw throwUnexpectedTypeObserved(parentElement);
        }
    }

    private Object[] getChildrenCompilationUnit(final CompilationUnit cu) {
        return new Object[] { cu.primaryType };
    }

    private Object[] getChildrenOfTypeDeclaration(final TypeDeclaration type) {
        final Iterable<Object> concat = Iterables.concat(type.memberTypes, type.methods, recommendations.get(type));
        return Iterables.toArray(concat, Object.class);
    }

    private Object[] getChildrenOfMethodDeclaration(final MethodDeclaration method) {
        final Iterable<Object> concat = Iterables.concat(method.nestedTypes, method.getVariables(),
                recommendations.get(method));
        return Iterables.toArray(concat, Object.class);
    }

    private Object[] getChildrenOfVariable(final Variable var) {
        return Iterables.toArray(recommendations.get(var), Object.class);
    }
}
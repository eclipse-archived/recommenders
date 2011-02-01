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
package org.eclipse.recommenders.internal.rcp.codecompletion.resolvers;

import static org.eclipse.recommenders.commons.utils.Checks.ensureIsNotNull;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.codecompletion.IIntelligentCompletionContext;
import org.eclipse.recommenders.rcp.codecompletion.IVariableUsageResolver;

public class StoreBasedVariableUsageResolver implements IVariableUsageResolver {

    private final IArtifactStore store;

    private Variable localVariable;

    private ICompilationUnit jdtCompilationUnit;

    private CompilationUnit recCompilationUnit;

    private org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration enclosingMethod;

    private Variable matchingLocalVariable;

    @Inject
    public StoreBasedVariableUsageResolver(final IArtifactStore store) {
        this.store = store;
    }

    @Override
    public boolean canResolve(final IIntelligentCompletionContext ctx) {
        ensureIsNotNull(ctx);
        this.localVariable = ctx.getVariable();
        this.jdtCompilationUnit = ctx.getCompilationUnit();

        if (!findCompilationUnitInStore()) {
            return false;
        }
        if (!findEnclosingMethodDeclaration()) {
            return false;
        }
        return findUsages();
    }

    private boolean findCompilationUnitInStore() {
        if (!store.hasArtifact(jdtCompilationUnit, CompilationUnit.class)) {
            return false;
        }
        recCompilationUnit = store.loadArtifact(jdtCompilationUnit, CompilationUnit.class);
        return true;
    }

    private boolean findEnclosingMethodDeclaration() {
        ensureIsNotNull(recCompilationUnit);
        enclosingMethod = recCompilationUnit.findMethod(localVariable.referenceContext);
        return enclosingMethod != null;
    }

    private boolean findUsages() {
        ensureIsNotNull(enclosingMethod);
        matchingLocalVariable = enclosingMethod.findVariable(localVariable.name);
        return matchingLocalVariable != null;
    }

    @Override
    public Set<IMethodName> getReceiverMethodInvocations() {
        return matchingLocalVariable.getReceiverCalls();
    }
}

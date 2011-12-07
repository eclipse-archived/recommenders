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
package org.eclipse.recommenders.internal.completion.rcp.resolvers;

import static org.eclipse.recommenders.commons.udc.ObjectUsage.NO_METHOD;
import static org.eclipse.recommenders.commons.udc.ObjectUsage.UNKNOWN_METHOD;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.completion.rcp.IVariableUsageResolver;
import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.analysis.codeelements.DefinitionSite.Kind;
import org.eclipse.recommenders.internal.analysis.codeelements.ObjectInstanceKey;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.utils.names.IMethodName;

import com.google.common.collect.Sets;

public class StoreBasedVariableUsageResolver implements IVariableUsageResolver {

    // private final IArtifactStore store;

    private Variable localVariable;

    private ICompilationUnit jdtCompilationUnit;

    private CompilationUnit recCompilationUnit;

    private org.eclipse.recommenders.internal.analysis.codeelements.MethodDeclaration enclosingMethod;

    private Variable matchingLocalVariable;

    private Kind receiverDefinitionKind;

    private IMethodName receiverDefinition;

    // @Inject
    // public StoreBasedVariableUsageResolver(final IArtifactStore store) {
    // this.store = store;
    // }

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
        // if (!store.hasArtifact(jdtCompilationUnit, CompilationUnit.class)) {
        return false;
        // }
        // recCompilationUnit = store.loadArtifact(jdtCompilationUnit, CompilationUnit.class);
        // return true;
    }

    private boolean findEnclosingMethodDeclaration() {
        ensureIsNotNull(recCompilationUnit);
        enclosingMethod = recCompilationUnit.findMethod(localVariable.getReferenceContext());
        return enclosingMethod != null;
    }

    private boolean findUsages() {
        ensureIsNotNull(enclosingMethod);
        matchingLocalVariable = enclosingMethod.findVariable(localVariable.getNameLiteral());

        final boolean canResolve = matchingLocalVariable != null;

        if (canResolve) {
            initKindAndDefinition();
        }

        return canResolve;
    }

    private void initKindAndDefinition() {
        final IMethodName initCall = getInitCall(matchingLocalVariable.getReceiverCalls());

        if (initCall != null) {
            // TODO set definitionSite in CU and use fuzzyFindDefinition()
            receiverDefinitionKind = Kind.NEW;
            receiverDefinition = initCall;
        } else if (matchingLocalVariable.fuzzyIsDefinedByMethodReturn()) {
            receiverDefinitionKind = Kind.METHOD_RETURN;
            receiverDefinition = fuzzyFindDefinition();
        } else if (matchingLocalVariable.fuzzyIsParameter()) {
            receiverDefinitionKind = Kind.PARAMETER;
            receiverDefinition = NO_METHOD;
        } else {
            // "unknown" is ignored by now, since there is no other way to detect fields
            receiverDefinitionKind = Kind.FIELD;
            receiverDefinition = NO_METHOD;
        }
    }

    private IMethodName fuzzyFindDefinition() {
        for (final ObjectInstanceKey o : matchingLocalVariable.pointsTo) {
            if (o.definitionSite != null && o.definitionSite.definedByMethod != null) {
                return o.definitionSite.definedByMethod;
            }
        }
        return UNKNOWN_METHOD;
    }

    private IMethodName getInitCall(final Set<IMethodName> calls) {
        for (final IMethodName call : calls) {
            if (call.isInit()) {
                return call;
            }
        }
        return null;
    }

    @Override
    public Set<IMethodName> getReceiverMethodInvocations() {
        return filterInitCalls(matchingLocalVariable.getReceiverCalls());
    }

    private Set<IMethodName> filterInitCalls(final Set<IMethodName> calls) {
        final Set<IMethodName> callsWithout = Sets.newLinkedHashSet();
        for (final IMethodName call : calls) {
            if (!call.isInit()) {
                callsWithout.add(call);
            }
        }
        return callsWithout;
    }

    @Override
    public Variable getResolvedVariable() {
        return matchingLocalVariable;
    }

    @Override
    public Kind getResolvedVariableKind() {
        return receiverDefinitionKind;
    }

    @Override
    public IMethodName getResolvedVariableDefinition() {
        return receiverDefinition;
    }
}

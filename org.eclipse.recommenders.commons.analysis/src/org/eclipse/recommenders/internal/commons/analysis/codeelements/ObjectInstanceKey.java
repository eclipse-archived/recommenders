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
package org.eclipse.recommenders.internal.commons.analysis.codeelements;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class ObjectInstanceKey implements ICodeElement {
    public static enum Kind {
        LOCAL, FIELD, PARAMETER, RETURN
    }

    public static ObjectInstanceKey create(final ITypeName varType, final Kind kind) {
        final ObjectInstanceKey recValue = new ObjectInstanceKey();
        recValue.type = varType;
        recValue.kind = kind;
        return recValue;
    }

    public ITypeName type;

    public Set<String> names = Sets.newTreeSet();

    public Kind kind;

    public DefinitionSite definitionSite;

    public Set<ParameterCallSite> parameterCallSites = Sets.newHashSet();

    public Set<ReceiverCallSite> receiverCallSites = Sets.newHashSet();

    public boolean isThis() {
        if (names.contains("this")) {
            return true;
        }
        for (final ReceiverCallSite callsite : receiverCallSites) {
            if (callsite.isThis()) {
                return true;
            }
        }
        return false;
    }

    public Set<IMethodName> getInvokedMethods() {
        final Set<IMethodName> res = Sets.newTreeSet();
        for (final ReceiverCallSite callsite : receiverCallSites) {
            res.add(callsite.targetMethod);
        }
        return res;
    }

    public void clearEmptySets() {
        if (parameterCallSites.isEmpty())
            parameterCallSites = null;
        if (receiverCallSites.isEmpty())
            receiverCallSites = null;
        if (names.isEmpty())
            names = null;

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public void accept(final CompilationUnitVisitor v) {
        if (v.visit(this)) {
            for (final ReceiverCallSite callsite : receiverCallSites) {
                callsite.accept(v);
            }
            for (final ParameterCallSite callsite : parameterCallSites) {
                callsite.accept(v);
            }
        }
    }
}

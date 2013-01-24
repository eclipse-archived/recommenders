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
package org.eclipse.recommenders.internal.utils.codestructs;

import java.util.Set;

import org.eclipse.recommenders.internal.utils.codestructs.DefinitionSite.Kind;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.collect.Sets;

public class ObjectInstanceKey {

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

    public Set<IMethodName> calls;

    public boolean isThis() {

        return kind == Kind.THIS;
    }

    public Set<IMethodName> getInvokedMethods() {
        return calls;
    }

}

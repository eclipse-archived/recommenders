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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.utils.names.IFieldName;
import org.eclipse.recommenders.utils.names.IMethodName;

public class DefinitionSite {

    public static enum Kind {
        METHOD_RETURN, NEW, FIELD, PARAMETER, THIS, UNKNOWN
    }

    public static DefinitionSite newSite(final Kind definitionKind, final IMethodName sourceMethod,
            final int lineNumber, final IMethodName definedByMethod) {
        final DefinitionSite res = new DefinitionSite();
        res.kind = definitionKind;
        res.sourceMethod = sourceMethod;
        res.lineNumber = lineNumber;
        res.definedByMethod = definedByMethod;
        return res;
    }

    public static DefinitionSite newSite(final Kind definitionKind) {
        return newSite(definitionKind, null, -1, null);
    }

    public static DefinitionSite create(final IFieldName definedByField) {
        final DefinitionSite res = new DefinitionSite();
        res.kind = Kind.FIELD;
        res.definedByField = definedByField;
        return res;
    }

    public Kind kind;

    public IMethodName sourceMethod;

    public IFieldName definedByField;

    public int lineNumber;

    /**
     * The method that returned this value
     */
    public IMethodName definedByMethod;

    /**
     * @see #newSite(Kind, IMethodName, int, IMethodName)
     */
    protected DefinitionSite() {
        // use create methods instead
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

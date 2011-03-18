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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.recommenders.commons.utils.names.IFieldName;
import org.eclipse.recommenders.commons.utils.names.IMethodName;

public class DefinitionSite2 {

    public enum Kind {
        THIS, FIELD, PARAMETER, METHOD_RETURN, NEW, UNKNOWN
    }

    /**
     * @param field
     *            something like MyClass.fieldName:LString;
     */
    public DefinitionSite2 forField(final IFieldName field) {
        final DefinitionSite2 res = new DefinitionSite2();
        res.kind = Kind.FIELD;
        res.definedByField = field;
        return res;
    }

    /**
     * @param analysisEntrypointMethod
     *            something like MyDialogPage.createContents(LComposite;)V
     * @param parameterIndex
     *            0 for LComposite in above example
     */
    public DefinitionSite2 forParameter(final IMethodName analysisEntrypointMethod, final int parameterIndex) {
        final DefinitionSite2 res = new DefinitionSite2();
        res.kind = Kind.PARAMETER;
        res.definedByMethod = analysisEntrypointMethod;
        res.parameterIndex = parameterIndex;
        return res;
    }

    /**
     * @param definingMethod
     *            something like "PlatformUI.getWorkbench()LWorkbench;"
     */
    public DefinitionSite2 forMethodReturn(final IMethodName definingMethod) {
        final DefinitionSite2 res = new DefinitionSite2();
        res.kind = Kind.METHOD_RETURN;
        res.definedByMethod = definingMethod;
        return res;
    }

    public DefinitionSite2 forNew() {
        final DefinitionSite2 res = new DefinitionSite2();
        res.kind = Kind.NEW;
        // no definedByMethod. Constructor call can be identified quickly by
        // looking on the receiver call sites.
        return res;
    }

    public DefinitionSite2 forThis() {
        final DefinitionSite2 res = new DefinitionSite2();
        res.kind = Kind.THIS;
        // no definedByMethod. Constructor call can be identified quickly by
        // looking on the receiver call sites.
        return res;
    }

    public DefinitionSite2 forUnknown() {
        final DefinitionSite2 res = new DefinitionSite2();
        res.kind = Kind.UNKNOWN;
        return res;
    }

    public Kind kind;
    public IFieldName definedByField;
    public IMethodName definedByMethod;
    public int parameterIndex;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

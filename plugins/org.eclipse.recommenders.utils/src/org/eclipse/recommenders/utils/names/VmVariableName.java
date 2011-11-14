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
package org.eclipse.recommenders.utils.names;

import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.MapMaker;

public class VmVariableName implements IVariableName {

    private static final long serialVersionUID = 5067244907255465328L;

    private static Map<String /* vmTypeName */, VmVariableName> index = new MapMaker().weakValues().makeMap();

    /**
     * Format: DeclaringType'.'fieldName;FieldType, i.e.,
     * &lt;VmTypeName&gt;.&lt;String&gt;;&lt;VmTypeName&gt;
     * 
     * @param variableName
     * @return
     */
    public static VmVariableName get(final String variableName) {

        VmVariableName res = index.get(variableName);
        if (res == null) {
            res = new VmVariableName(variableName);
            index.put(variableName, res);
        }
        return res;
    }

    private String identifier;

    protected VmVariableName() {
        // no-one should instantiate this class. O
    }

    /**
     * @see #get(String)
     */
    protected VmVariableName(final String vmVariableName) {
        identifier = vmVariableName;
        ensureIsNotNull(identifier);
        ensureIsNotNull(getDeclaringMethod());

    }

    @Override
    public String getName() {
        return StringUtils.substringAfterLast(identifier, "#");
    }

    @Override
    public IMethodName getDeclaringMethod() {
        final String declaringType = StringUtils.substringBeforeLast(identifier, "#");
        return VmMethodName.get(declaringType);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int compareTo(final IVariableName other) {
        identifier.compareTo(other.getIdentifier());
        return 0;
    }

}

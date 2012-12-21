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
import org.eclipse.recommenders.utils.annotations.Testing;

import com.google.common.collect.MapMaker;

public class VmFieldName implements IFieldName {
    private static final long serialVersionUID = 5067244907255465328L;

    private static Map<String /* vmTypeName */, VmFieldName> index = new MapMaker().weakValues().makeMap();

    /**
     * Format: DeclaringType'.'fieldName;FieldType, i.e., &lt;VmTypeName&gt;.&lt;String&gt;;&lt;VmTypeName&gt;
     * 
     * @param fieldName
     * @return
     */
    public static synchronized VmFieldName get(final String fieldName) {
        // typeName = removeGenerics(typeName);
        VmFieldName res = index.get(fieldName);
        if (res == null) {
            res = new VmFieldName(fieldName);
            index.put(fieldName, res);
        }
        return res;
    }

    private String identifier;

    /**
     * @see #get(String)
     */
    @Testing("Outside of tests, VmFieldNames should be canonicalized through VmFieldName#get(String)")
    protected VmFieldName(final String vmFieldName) {
        identifier = vmFieldName;
        ensureIsNotNull(identifier);
        ensureIsNotNull(getDeclaringType());
        ensureIsNotNull(getFieldName());
        ensureIsNotNull(getFieldType());
    }

    @Override
    public ITypeName getDeclaringType() {
        final String declaringType = StringUtils.substringBeforeLast(identifier, ".");
        return VmTypeName.get(declaringType);
    }

    @Override
    public String getFieldName() {
        final String fieldName = StringUtils.substringBetween(identifier, ".", ";");
        return fieldName;
    }

    @Override
    public ITypeName getFieldType() {
        final String fieldType = StringUtils.substringAfter(identifier, ";");
        return VmTypeName.get(fieldType);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int compareTo(final IFieldName other) {
        return identifier.compareTo(other.getIdentifier());
    }

    @Override
    public String toString() {
        return getIdentifier();
    }
}

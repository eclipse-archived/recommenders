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
/**
 * 
 */
package org.eclipse.recommenders.internal.commons.analysis.fixture;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnsupportedOperation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.recommenders.commons.utils.Throws;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.impl.SetOfClasses;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

public final class DefaultExclusionSetOfClass extends SetOfClasses {
    private static final long serialVersionUID = 1L;

    // private final String regex =
    // "(sun/.*)|(sunw/.*)|(com/sunw/.*)|(com/sun/.*)|(javax/.*)";
    private final String regex = "(sun/.*)|(sunw/.*)|(com/sunw/.*)|(com/sun/.*)";

    private final Pattern pattern = Pattern.compile(regex);

    @Override
    public boolean contains(final TypeReference clazz) {
        final TypeName name = clazz.getName();
        String string = null;
        if (name.isClassType()) {
            // remove starting 'L'
            string = typeNameToValidString(name);
        } else if (name.isArrayType()) {
            string = typeNameToValidString(name.getInnermostElementType());
        } else if (name.isPrimitiveType()) {
            return false;
        } else {
            Throws.throwUnreachable("type is unknown yet.");
        }
        return contains(string);
    }

    private String typeNameToValidString(final TypeName name) {
        return name.toString().substring(1);
    }

    @Override
    public boolean contains(final String className) {
        final Matcher m = pattern.matcher(className);
        final boolean matches = m.matches();
        return matches;
    }

    @Override
    public void add(final IClass clazz) {
        throwUnsupportedOperation();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

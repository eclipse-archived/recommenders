/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Darmstadt University of Technology - initial API and implementation.
 */
package org.eclipse.recommenders.internal.commons.analysis.selectors;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;

/**
 * Returns all declared (instance and static) fields of the given class as well as every public OR protected field
 * declared in any superclass of the given base class.
 */
public class AllAccessibleFieldsSelector implements IFieldsSelector {
    @Override
    public Collection<IField> select(final IClass baseclass) {
        final List<IField> accessibleFields = Lists.newLinkedList();
        for (final IField field : baseclass.getAllFields()) {
            if (field.getDeclaringClass() == baseclass) {
                accessibleFields.add(field);
            } else if (field.isProtected() || field.isPublic()) {
                accessibleFields.add(field);
            }
        }
        return accessibleFields;
    }
}

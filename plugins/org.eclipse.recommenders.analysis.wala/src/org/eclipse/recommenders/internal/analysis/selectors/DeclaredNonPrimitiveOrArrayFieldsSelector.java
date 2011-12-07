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
package org.eclipse.recommenders.internal.analysis.selectors;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.types.TypeReference;

public class DeclaredNonPrimitiveOrArrayFieldsSelector implements IFieldsSelector {
    private final Predicate<IField> p = new Predicate<IField>() {
        @Override
        public boolean apply(final IField input) {
            final TypeReference type = input.getFieldTypeReference();
            if (type.isArrayType()) {
                return false;
            } else if (type.isPrimitiveType()) {
                return false;
            }
            return true;
        }
    };

    @Override
    public Collection<IField> select(final IClass clazz) {
        final List<IField> res = Lists.newLinkedList();
        for (final IField field : clazz.getDeclaredInstanceFields()) {
            if (p.apply(field)) {
                res.add(field);
            }
        }
        for (final IField field : clazz.getDeclaredStaticFields()) {
            if (p.apply(field)) {
                res.add(field);
            }
        }
        return res;
    }
}

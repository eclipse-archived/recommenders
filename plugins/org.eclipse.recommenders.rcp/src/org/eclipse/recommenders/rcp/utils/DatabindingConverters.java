/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.utils;

import java.util.UUID;

import org.eclipse.core.databinding.conversion.Converter;

public class DatabindingConverters {

    public static class EnumToBooleanConverter<T extends Enum<T>> extends Converter {

        private final T[] trueValues;

        @SafeVarargs
        public EnumToBooleanConverter(T... trueValues) {
            super(Object.class, Boolean.class);
            this.trueValues = trueValues;
        }

        @Override
        public Object convert(Object fromObject) {
            if (fromObject == null) {
                throw new IllegalArgumentException("Parameter 'fromObject' was null."); //$NON-NLS-1$
            }

            for (T trueValue : trueValues) {
                if (trueValue == fromObject) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class StringToUuidConverter extends Converter {

        public StringToUuidConverter() {
            super(String.class, UUID.class);
        }

        @Override
        public Object convert(Object fromObject) {
            if (fromObject == null) {
                throw new IllegalArgumentException("Parameter 'fromObject' was null."); //$NON-NLS-1$
            }
            String string = (String) fromObject;
            return UUID.fromString(string);
        }
    }

}

/**
 * Copyright (c) 2016 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.news.rcp.command;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class BooleanParameterValueConverter extends AbstractParameterValueConverter {

    @Override
    public Object convertToObject(String parameterValue) throws ParameterValueConversionException {
        return Boolean.valueOf(parameterValue);
    }

    @Override
    public String convertToString(Object parameterValue) throws ParameterValueConversionException {
        if (!(parameterValue instanceof Boolean)) {
            throw new ParameterValueConversionException(
                    String.format("Expected %s, got %s", Boolean.class, parameterValue.getClass())); //$NON-NLS-1$
        }
        return parameterValue.toString();
    }
}

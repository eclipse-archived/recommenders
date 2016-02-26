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

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;
import org.eclipse.recommenders.news.api.NewsItem;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NewsItemsParameterValueConverter extends AbstractParameterValueConverter {

    private static final Type PARAMETER_TYPE = new TypeToken<List<NewsItem>>() {

        private static final long serialVersionUID = 1L;
    }.getType();

    private final Gson gson = new GsonBuilder().create();

    @Override
    public Object convertToObject(String json) throws ParameterValueConversionException {
        return gson.fromJson(json, PARAMETER_TYPE);
    }

    @Override
    public String convertToString(Object parameterValue) throws ParameterValueConversionException {
        return gson.toJson(parameterValue);
    }
}

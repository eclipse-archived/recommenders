/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.commons.client;

import static java.lang.String.format;
import static org.eclipse.recommenders.commons.client.WebServiceClient.encode;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CouchUtils {

    public static <T> List<T> transformValues(final GenericResultObjectView<T> queryResult) {
        final List<T> resultList = Lists.newLinkedList();
        for (final ResultObject<T> row : queryResult.rows) {
            resultList.add(row.value);
        }
        return resultList;
    }

    public static <T> T getFirst(final GenericResultObjectView<T> queryResult, final T defaultValue) {
        final List<T> res = transformValues(queryResult);
        return Iterables.getFirst(res, defaultValue);
    }

    public static String createViewUrl(final String designDocument, final String view) {
        return format("_design/%s/_view/%s", designDocument, view);
    }

    public static String createViewUrlWithKey(final String designDocument, final String view, final String key) {
        return format("_design/%s/_view/%s?key=%s", designDocument, view, encode(quote(key)));
    }

    public static String createViewUrlWithKeyObject(final String designDocument, final String view,
            final Map<String, String> keyValuePairs) {
        final String baseUrl = createViewUrl(designDocument, view);
        final Set<String> keyValueStrings = Sets.newHashSet();
        for (final Entry<String, String> entry : keyValuePairs.entrySet()) {
            keyValueStrings.add(quote(entry.getKey()) + ":" + quote(entry.getValue()));
        }
        final String keyObject = "{" + StringUtils.join(keyValueStrings, ",") + "}";
        return baseUrl + "?key=" + encode(keyObject);
    }

    /**
     * <code>string-value</code> gets quoted to: <code>"string-value"</code>
     */
    public static String quote(final String value) {
        return "\"" + escapeForJson(value) + "\"";
    }

    public static String escapeForJson(final String value) {
        return value.replace("\"", "\\\"");
    }

}

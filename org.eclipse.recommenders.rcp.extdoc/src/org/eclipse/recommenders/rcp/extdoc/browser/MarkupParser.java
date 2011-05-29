/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.rcp.extdoc.browser;

public final class MarkupParser {

    public String parseTextile(final String markup) {
        String result = markup;
        result = result.replaceAll("h([1-9])\\. (.+)\r?\n", "<h$1>$2</h$1>");
        return result.replace("\n", "<br/>");
    }
}

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
package org.eclipse.recommenders.server.extdoc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

public final class WikiServer {

    public String read(final IJavaElement javaElement) {
        final Map<String, Object> document = Server.getDocument(getId(javaElement));
        return document == null ? null : (String) document.get("text");
    }

    public void write(final IJavaElement javaElement, final String text) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("text", text);
        Server.storeOrUpdateDocument(getId(javaElement), map);
    }

    private String getId(final IJavaElement javaElement) {
        return "wiki_" + javaElement.getHandleIdentifier().replace("/", "").replace("\\", "").replace("<", "_");
    }
}

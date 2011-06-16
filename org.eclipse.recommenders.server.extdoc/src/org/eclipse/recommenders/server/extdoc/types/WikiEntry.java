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
package org.eclipse.recommenders.server.extdoc.types;

import com.google.gson.annotations.SerializedName;

import org.eclipse.jdt.core.IJavaElement;

public final class WikiEntry {

    @SerializedName("_id")
    private String id;
    @SerializedName("_rev")
    private String rev;

    private final String providerId = getClass().getSimpleName();
    private String elementId;
    private String text;

    public static WikiEntry create(final IJavaElement element, final String text) {
        final WikiEntry result = new WikiEntry();
        result.elementId = element.getHandleIdentifier();
        result.text = text;
        return result;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }
}

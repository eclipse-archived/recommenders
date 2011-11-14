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
package org.eclipse.recommenders.extdoc.transport.types;

import org.eclipse.recommenders.extdoc.rcp.IServerType;
import org.eclipse.recommenders.utils.Checks;

public final class WikiEntry implements IServerType {

    @SuppressWarnings("unused")
    private final String providerId = getClass().getSimpleName();
    private String type;

    private String text;

    public static WikiEntry create(final String elementId, final String text) {
        final WikiEntry result = new WikiEntry();
        result.type = elementId;
        result.text = text;
        result.validate();
        return result;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public void validate() {
        Checks.ensureIsTrue(!type.isEmpty());
        Checks.ensureIsTrue(!text.isEmpty());
    }
}

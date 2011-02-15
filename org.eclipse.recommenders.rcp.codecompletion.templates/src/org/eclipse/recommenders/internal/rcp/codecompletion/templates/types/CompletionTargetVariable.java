/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.types;

import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

/**
 * Models the variable on which the completion was triggered.
 */
public final class CompletionTargetVariable {

    private final String name;
    private final ITypeName typeName;
    private final boolean needsConstructor;
    private final Region documentRegion;

    public CompletionTargetVariable(final String name, final ITypeName typeName, final Region documentRegion,
            final boolean needsConstructor) {
        this.name = name;
        this.typeName = Checks.ensureIsNotNull(typeName);
        this.documentRegion = documentRegion;
        this.needsConstructor = needsConstructor;
    }

    public String getName() {
        return name;
    }

    public ITypeName getType() {
        return typeName;
    }

    public boolean isNeedsConstructor() {
        return needsConstructor;
    }

    public Region getDocumentRegion() {
        return documentRegion;
    }
}

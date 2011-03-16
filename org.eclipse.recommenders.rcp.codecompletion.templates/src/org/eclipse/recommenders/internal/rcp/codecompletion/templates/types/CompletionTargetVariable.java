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

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.Checks;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;

/**
 * Models the variable on which the completion was triggered.
 */
public final class CompletionTargetVariable {

    private final String name;
    private final ITypeName typeName;
    private final ImmutableSet<IMethodName> receiverCalls;
    private final Region documentRegion;
    private final boolean needsConstructor;

    /**
     * @param name
     *            The name of the variable on which the completion was
     *            triggered.
     * @param typeName
     *            The type of the variable on which the completion was
     *            triggered.
     * @param receiverCalls
     * @param documentRegion
     *            True, if the templates proposals definitely have to contain
     *            constructors, e.g. in "<code>Button b<^Space></code>".
     * @param needsConstructor
     *            The region inside the document which shall be replaced by
     *            completion proposals for this variable.
     */
    public CompletionTargetVariable(final String name, final ITypeName typeName, final Set<IMethodName> receiverCalls,
            final Region documentRegion, final boolean needsConstructor) {
        this.name = name;
        this.typeName = Checks.ensureIsNotNull(typeName);
        this.receiverCalls = ImmutableSet.copyOf(receiverCalls);
        this.documentRegion = documentRegion;
        this.needsConstructor = needsConstructor;
    }

    /**
     * @return The name of the variable on which the completion was triggered.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The type of the variable on which the completion was triggered.
     */
    public ITypeName getType() {
        return typeName;
    }

    public ImmutableSet<IMethodName> getReceiverCalls() {
        return receiverCalls;
    }

    /**
     * @return True, if the templates proposals definitely have to contain
     *         constructors, e.g. in "<code>Button b<^Space></code>".
     */
    public boolean isNeedsConstructor() {
        return needsConstructor;
    }

    /**
     * @return The region inside the document which shall be replaced by
     *         completion proposals for this variable.
     */
    public Region getDocumentRegion() {
        return documentRegion;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).add("type", typeName)
                .add("needsConstructor", needsConstructor).add("documentRegion", documentRegion)
                .add("receiverCalls", receiverCalls).toString();
    }
}

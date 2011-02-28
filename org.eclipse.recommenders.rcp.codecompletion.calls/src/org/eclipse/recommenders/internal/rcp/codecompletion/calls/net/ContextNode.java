/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.calls.net;

import static org.eclipse.recommenders.commons.utils.Throws.throwUnreachable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.recommenders.commons.utils.annotations.Nullable;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;

import smile.DocItemInfo;
import smile.Network;

import com.google.common.collect.Maps;

public class ContextNode extends AbstractNode {

    protected static String ID = "ctx";

    protected static final String PROPERTY_ESCAPED_METHOD_REFERENCES = "escapedMethodReferences";

    private final Map<String, IMethodName> knownMethodContexts;

    protected ContextNode(final Network network) {
        super(network, ID);
        knownMethodContexts = computeKnownMethodContexts();
    }

    private Map<String, IMethodName> computeKnownMethodContexts() {
        final Map<String, IMethodName> res = Maps.newHashMap();
        final DocItemInfo doc = findMethodReferenceToNameUserProperty();
        for (final String line : doc.path.split("\n")) {
            final String[] escapedToVmFullQualifiedMethodName = line.split(":");
            final String escapedMethodName = escapedToVmFullQualifiedMethodName[0];
            final IMethodName methodRef = VmMethodName.get(escapedToVmFullQualifiedMethodName[1]);
            res.put(escapedMethodName, methodRef);
        }
        return res;
    }

    private DocItemInfo findMethodReferenceToNameUserProperty() {
        final DocItemInfo[] docs = network.getNodeDocumentation(ContextNode.ID);
        for (final DocItemInfo doc : docs) {
            if (doc.title.equals(PROPERTY_ESCAPED_METHOD_REFERENCES)) {
                return doc;
            }
        }
        throw throwUnreachable("no mapping between escaped method names and their real names provided.");
    }

    /**
     * Returns all methods known in this context.
     */
    public Collection<IMethodName> getKnownMethodContexts() {
        return Collections.unmodifiableCollection(knownMethodContexts.values());
    }

    /**
     * Sets the method context to the given method - if
     * 
     * @param rootMethodContext
     *            the first declaration of an overridden method, i.e.,
     *            {@link MethodDeclaration#firstDeclaration}
     * @return <code>true</code> iff the method context is known -
     *         <code>false</code> otherwise.
     */
    public boolean setContext(@Nullable final IMethodName rootMethodContext) {
        if (rootMethodContext == null) {
            clearEvidence();
            return false;
        }
        final String outcomeId = ObjectMethodCallsNet.escape(rootMethodContext);
        if (!knownMethodContexts.containsKey(outcomeId)) {
            return false;
        }
        return setEvidence(outcomeId);

    }

    public boolean isContextSet() {
        return isEvidence();
    }

    /**
     * Returns the currently selected method context.
     * <p>
     * <b>NOTE:</b> requires {@link #isContextSet()} to be true. Don't call this
     * method if {@link #isContextSet()} is <code>false</code>.
     */
    public IMethodName getContext() {
        final String evidenceId = getEvidenceId();
        return knownMethodContexts.get(evidenceId);
    }
}

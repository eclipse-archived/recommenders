/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.server.extdoc;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.AbstractFeedbackServer;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.types.CodeExamples;

import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public final class CodeExamplesServer extends AbstractFeedbackServer {

    private static final String PROVIDERID = CodeExamples.class.getSimpleName();

    @Inject
    public CodeExamplesServer(final ICouchDbServer server, final UsernamePreferenceListener usernameListener,
            final JavaElementResolver resolver) {
        super(server, usernameListener, resolver);
    }

    public CodeExamples getOverridenMethodCodeExamples(final IMethod method) {
        final String key = getServer().createKey(method);
        final CodeExamples result = getServer().getProviderContent(PROVIDERID, "method", key,
                new GenericType<GenericResultObjectView<CodeExamples>>() {
                });
        return result;
    }

    public CodeExamples getTypeCodeExamples(final IType type) {
        final String key = getServer().createKey(type);
        final CodeExamples result = getServer().getProviderContent(PROVIDERID, "type", key,
                new GenericType<GenericResultObjectView<CodeExamples>>() {
                });
        return result;
    }

}

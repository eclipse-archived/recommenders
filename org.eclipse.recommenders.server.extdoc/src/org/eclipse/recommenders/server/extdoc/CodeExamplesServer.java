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

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.server.extdoc.AbstractFeedbackServer;
import org.eclipse.recommenders.server.extdoc.types.CodeExamples;

import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public final class CodeExamplesServer extends AbstractFeedbackServer {

    private static final String PROVIDERID = CodeExamples.class.getSimpleName();

    @Inject
    public CodeExamplesServer(final ICouchDbServer server, final UsernameProvider usernameListener) {
        super(server, usernameListener);
    }

    public CodeExamples getOverridenMethodCodeExamples(final IMethodName method) {
        final CodeExamples result = getServer().getProviderContent(PROVIDERID, method,
                new GenericType<GenericResultObjectView<CodeExamples>>() {
                });
        return result;
    }

    public CodeExamples getTypeCodeExamples(final ITypeName type) {
        final CodeExamples result = getServer().getProviderContent(PROVIDERID, type,
                new GenericType<GenericResultObjectView<CodeExamples>>() {
                });
        return result;
    }

}

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
package org.eclipse.recommenders.extdoc.transport;

import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.extdoc.transport.types.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.transport.types.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.transport.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.extdoc.transport.types.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.extdoc.transport.AbstractFeedbackServer;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.inject.Inject;
import com.sun.jersey.api.client.GenericType;

public final class SubclassingServer extends AbstractFeedbackServer {

    @Inject
    public SubclassingServer(final ICouchDbServer server, final UsernameProvider usernameListener) {
        super(server, usernameListener);
    }

    public ClassOverrideDirectives getClassOverrideDirectives(final ITypeName type) {
        return getServer().getProviderContent(ClassOverrideDirectives.class.getSimpleName(), type,
                new GenericType<GenericResultObjectView<ClassOverrideDirectives>>() {
                });
    }

    public ClassSelfcallDirectives getClassSelfcallDirectives(final ITypeName type) {
        return getServer().getProviderContent(ClassSelfcallDirectives.class.getSimpleName(), type,
                new GenericType<GenericResultObjectView<ClassSelfcallDirectives>>() {
                });
    }

    public MethodSelfcallDirectives getMethodSelfcallDirectives(final IMethodName method) {
        return getServer().getProviderContent(MethodSelfcallDirectives.class.getSimpleName(), method,
                new GenericType<GenericResultObjectView<MethodSelfcallDirectives>>() {
                });
    }

    public ClassOverridePatterns getClassOverridePatterns(final ITypeName type) {
        return getServer().getProviderContent(ClassOverridePatterns.class.getSimpleName(), type,
                new GenericType<GenericResultObjectView<ClassOverridePatterns>>() {
                });
    }

}

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

import com.sun.jersey.api.client.GenericType;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;

public final class SubclassingServer extends AbstractRatingsServer {

    public ClassOverrideDirectives getClassOverrideDirective(final IType type) {
        final String key = JavaElementResolver.INSTANCE.toRecType(type).getIdentifier();
        return Server.getProviderContent("ClassOverrideDirectives", "type", key,
                new GenericType<GenericResultObjectView<ClassOverrideDirectives>>() {
                });
    }

    public ClassSelfcallDirectives getClassSelfcallDirective(final IType type) {
        final String key = JavaElementResolver.INSTANCE.toRecType(type).getIdentifier();
        return Server.getProviderContent("ClassSelfcallDirectives", "type", key,
                new GenericType<GenericResultObjectView<ClassSelfcallDirectives>>() {
                });
    }

    public MethodSelfcallDirectives getMethodSelfcallDirective(final IMethod method) {
        final String key = JavaElementResolver.INSTANCE.toRecMethod(method).getIdentifier();
        return Server.getProviderContent("MethodSelfcallDirectives", "method", key,
                new GenericType<GenericResultObjectView<MethodSelfcallDirectives>>() {
                });
    }

}

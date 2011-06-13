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
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;

public final class SubclassingServer extends AbstractRatingsServer {

    public ClassOverrideDirectives getClassOverrideDirective(final IType type) {
        return Server.getProviderContent("ClassOverrideDirectives", "type",
                type.getKey().substring(0, type.getKey().length() - 1),
                new GenericType<GenericResultObjectView<ClassOverrideDirectives>>() {
                });
    }

    public ClassSelfcallDirectives getClassSelfcallDirective(final IType type) {
        return Server.getProviderContent("ClassSelfcallDirectives", "type",
                type.getKey().substring(0, type.getKey().length() - 1),
                new GenericType<GenericResultObjectView<ClassSelfcallDirectives>>() {
                });
    }

    public MethodSelfcallDirectives getMethodSelfcallDirective(final IMethod method) {
        return Server.getProviderContent("MethodSelfcallDirectives", "method", method.getKey().replace(";.", "."),
                new GenericType<GenericResultObjectView<MethodSelfcallDirectives>>() {
                });
    }

    @Override
    protected String getDocumentId(final Object object) {
        // TODO Auto-generated method stub
        return null;
    }

}

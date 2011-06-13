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
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirective;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirective;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirective;

public final class SubclassingServer extends AbstractRatingsServer {

    public ClassOverrideDirective getClassOverrideDirective(final IType type) {
        return Server.getType("ClassOverrideDirectives", type.getKey().substring(0, type.getKey().length() - 1),
                new GenericType<GenericResultObjectView<ClassOverrideDirective>>() {
                });
    }

    public ClassSelfcallDirective getClassSelfcallDirective(final IType type) {
        return Server.getType("ClassSelfcallDirectives", type.getKey().substring(0, type.getKey().length() - 1),
                new GenericType<GenericResultObjectView<ClassSelfcallDirective>>() {
                });
    }

    public MethodSelfcallDirective getMethodSelfcallDirective(final IMethod method) {
        return Server.getMethod("MethodSelfcallDirectives", method.getKey().replace(";.", "."),
                new GenericType<GenericResultObjectView<MethodSelfcallDirective>>() {
                });
    }

    @Override
    protected String getDocumentId(final Object object) {
        // TODO Auto-generated method stub
        return null;
    }

}

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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.commons.client.GenericResultObjectView;
import org.eclipse.recommenders.internal.server.extdoc.AbstractRatingsServer;
import org.eclipse.recommenders.internal.server.extdoc.Server;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.server.extdoc.types.ClassOverrideDirectives;
import org.eclipse.recommenders.server.extdoc.types.ClassOverridePatterns;
import org.eclipse.recommenders.server.extdoc.types.ClassSelfcallDirectives;
import org.eclipse.recommenders.server.extdoc.types.MethodSelfcallDirectives;

import com.sun.jersey.api.client.GenericType;

public final class SubclassingServer extends AbstractRatingsServer {

    private static final String S_METHOD = "method";
    private static final String S_TYPE = "type";

    public ClassOverrideDirectives getClassOverrideDirective(final IType type) {
        return Server.getProviderContent(ClassOverrideDirectives.class.getSimpleName(), S_TYPE, createKey(type),
                new GenericType<GenericResultObjectView<ClassOverrideDirectives>>() {
                });
    }

    public ClassSelfcallDirectives getClassSelfcallDirective(final IType type) {
        return Server.getProviderContent(ClassSelfcallDirectives.class.getSimpleName(), S_TYPE, createKey(type),
                new GenericType<GenericResultObjectView<ClassSelfcallDirectives>>() {
                });
    }

    public MethodSelfcallDirectives getMethodSelfcallDirective(final IMethod method) {
        return Server.getProviderContent(MethodSelfcallDirectives.class.getSimpleName(), S_METHOD, createKey(method),
                new GenericType<GenericResultObjectView<MethodSelfcallDirectives>>() {
                });
    }

    public ClassOverridePatterns getClassOverridePatterns(final IType type) {
        return Server.getProviderContent(ClassOverridePatterns.class.getSimpleName(), S_TYPE, createKey(type),
                new GenericType<GenericResultObjectView<ClassOverridePatterns>>() {
                });
    }

    private String createKey(final IMethod method) {
        return JavaElementResolver.INSTANCE.toRecMethod(method).getIdentifier();
    }

    private String createKey(final IType type) {
        return JavaElementResolver.INSTANCE.toRecType(type).getIdentifier();
    }
}

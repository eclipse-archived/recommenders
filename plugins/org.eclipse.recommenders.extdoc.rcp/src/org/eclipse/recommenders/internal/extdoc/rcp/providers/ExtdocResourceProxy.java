/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import javax.inject.Inject;

import org.eclipse.recommenders.extdoc.ClassOverrideDirectives;
import org.eclipse.recommenders.extdoc.ClassOverridePatterns;
import org.eclipse.recommenders.extdoc.ClassSelfcallDirectives;
import org.eclipse.recommenders.extdoc.CodeExamples;
import org.eclipse.recommenders.extdoc.IExtdocResource;
import org.eclipse.recommenders.extdoc.MethodSelfcallDirectives;
import org.eclipse.recommenders.internal.extdoc.rcp.wiring.ExtdocModule.Extdoc;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.webclient.WebServiceClient;
import org.eclipse.recommenders.webclient.exceptions.NotFoundException;

/**
 * A proxy for remote server interface. Note that all its methods may return {@code null}!
 */
public class ExtdocResourceProxy implements IExtdocResource {

    private final WebServiceClient webclient;

    @Inject
    public ExtdocResourceProxy(@Extdoc final WebServiceClient webclient) {
        this.webclient = webclient;
        // TODO Auto-generated constructor stub
    }

    @Override
    public ClassOverrideDirectives findClassOverrideDirectives(final ITypeName type) {
        ClassOverrideDirectives res = null;
        try {
            res = webclient.doPostRequest(URL_CLASS_OVERRIDES, type, ClassOverrideDirectives.class);
        } catch (final NotFoundException e) {
            // ignore 404s
            System.out.println(e);
        }
        return res;
    }

    @Override
    public ClassSelfcallDirectives findClassSelfcallDirectives(final ITypeName type) {
        ClassSelfcallDirectives res = null;
        try {
            res = webclient.doPostRequest(URL_CLASS_SELF_CALLS, type, ClassSelfcallDirectives.class);
        } catch (final NotFoundException e) {
            // ignore 404s
        }
        return res;
    }

    @Override
    public MethodSelfcallDirectives findMethodSelfcallDirectives(final IMethodName method) {
        MethodSelfcallDirectives res = null;
        try {
            res = webclient.doPostRequest("method-selfcalls", method, MethodSelfcallDirectives.class);
        } catch (final NotFoundException e) {
            // ignore 404s
        }
        return res;
    }

    @Override
    public ClassOverridePatterns findClassOverridePatterns(final ITypeName type) {
        ClassOverridePatterns res = null;
        try {
            res = webclient.doPostRequest(URL_CLASS_OVERRIDE_PATTERNS, type, ClassOverridePatterns.class);
        } catch (final NotFoundException e) {
            // ignore 404s
        }
        return res;
    }

    @Override
    public CodeExamples findCodeExamples(final ITypeName type) {
        CodeExamples res = null;
        try {
            res = webclient.doPostRequest(URL_TYPE_USAGE_SNIPPETS, type, CodeExamples.class);
        } catch (final NotFoundException e) {
            // ignore 404s
        }
        return res;
    }

}

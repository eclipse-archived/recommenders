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
package org.eclipse.recommenders.extdoc;

import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;

public interface IExtdocResource {

    public String URL_CLASS_OVERRIDES = "class-overrides";
    public String URL_CLASS_OVERRIDE_PATTERNS = "class-overrides-patterns";
    public String URL_TYPE_USAGE_SNIPPETS = "type-usage-snippets";
    public String URL_CLASS_SELF_CALLS = "class-selfcalls";
    public String URL_METHOD_SELF_CALLS = "method-selfcalls";

    public ClassOverrideDirectives findClassOverrideDirectives(final ITypeName type);

    public ClassSelfcallDirectives findClassSelfcallDirectives(final ITypeName type);

    public MethodSelfcallDirectives findMethodSelfcallDirectives(final IMethodName method);

    public ClassOverridePatterns findClassOverridePatterns(final ITypeName type);

    public CodeExamples findCodeExamples(final ITypeName type);

}
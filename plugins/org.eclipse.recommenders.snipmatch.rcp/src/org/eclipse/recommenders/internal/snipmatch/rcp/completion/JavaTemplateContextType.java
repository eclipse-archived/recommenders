/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.SNIPMATCH_CONTEXT_ID;

import org.eclipse.jdt.internal.corext.template.java.ElementTypeResolver;
import org.eclipse.jdt.internal.corext.template.java.ExceptionVariableNameResolver;
import org.eclipse.jdt.internal.corext.template.java.FieldResolver;
import org.eclipse.jdt.internal.corext.template.java.ImportsResolver;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.LinkResolver;
import org.eclipse.jdt.internal.corext.template.java.LocalVarResolver;
import org.eclipse.jdt.internal.corext.template.java.NameResolver;
import org.eclipse.jdt.internal.corext.template.java.StaticImportResolver;
import org.eclipse.jdt.internal.corext.template.java.TypeResolver;
import org.eclipse.jdt.internal.corext.template.java.TypeVariableResolver;
import org.eclipse.jdt.internal.corext.template.java.VarResolver;
import org.eclipse.jface.text.templates.TemplateContextType;

@SuppressWarnings("restriction")
public class JavaTemplateContextType {

    private static class Holder {
        static final TemplateContextType INSTANCE = createContextType();
    }

    public static TemplateContextType getInstance() {
        return Holder.INSTANCE;
    }

    private static TemplateContextType createContextType() {

        JavaContextType contextType = new JavaContextType();
        contextType.setId(SNIPMATCH_CONTEXT_ID);
        contextType.initializeContextTypeResolvers();

        ImportsResolver importResolver = new ImportsResolver();
        importResolver.setType("import"); //$NON-NLS-1$
        contextType.addResolver(importResolver);

        StaticImportResolver staticImportResolver = new StaticImportResolver();
        staticImportResolver.setType("importStatic"); //$NON-NLS-1$
        contextType.addResolver(staticImportResolver);

        VarResolver varResolver = new VarResolver();
        varResolver.setType("var"); //$NON-NLS-1$
        contextType.addResolver(varResolver);

        LocalVarResolver localVarResolver = new LocalVarResolver();
        localVarResolver.setType("localVar"); //$NON-NLS-1$
        contextType.addResolver(localVarResolver);

        FieldResolver fieldResolver = new FieldResolver();
        fieldResolver.setType("field"); //$NON-NLS-1$
        contextType.addResolver(fieldResolver);

        TypeResolver typeResolver = new TypeResolver();
        typeResolver.setType("newType"); //$NON-NLS-1$
        contextType.addResolver(typeResolver);

        LinkResolver linkResolver = new LinkResolver();
        linkResolver.setType("link"); //$NON-NLS-1$
        contextType.addResolver(linkResolver);

        NameResolver nameResolver = new NameResolver();
        nameResolver.setType("newName"); //$NON-NLS-1$
        contextType.addResolver(nameResolver);

        ElementTypeResolver elementTypeResolver = new ElementTypeResolver();
        elementTypeResolver.setType("elemType"); //$NON-NLS-1$
        contextType.addResolver(elementTypeResolver);

        TypeVariableResolver typeVariableResolver = new TypeVariableResolver();
        typeVariableResolver.setType("argType"); //$NON-NLS-1$
        contextType.addResolver(typeVariableResolver);

        ExceptionVariableNameResolver exceptionVariableNameResolver = new ExceptionVariableNameResolver();
        exceptionVariableNameResolver.setType("exception_variable_name"); //$NON-NLS-1$
        contextType.addResolver(exceptionVariableNameResolver);

        return contextType;
    }
}

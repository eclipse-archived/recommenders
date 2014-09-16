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
package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.SNIPMATCH_CONTEXT_ID;

import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.internal.snipmatch.rcp.SnipmatchTemplateContextType;

@SuppressWarnings("restriction")
public class SnippetSourceValidator {

    private static final String VALID_SNIPPET = ""; //$NON-NLS-1$

    public static String isSourceValid(String source) {
        TemplateContextType contextType = SnipmatchTemplateContextType.getInstance();
        Template template = new Template("name", "description", SNIPMATCH_CONTEXT_ID, source, true); //$NON-NLS-1$ //$NON-NLS-2$
        JavaContext context = new JavaContext(contextType, new Document(), new Position(0), null);
        context.setForceEvaluation(true);
        try {
            context.evaluate(template);
        } catch (Exception e) {
            return e.getMessage();
        }
        return VALID_SNIPPET;
    }
}

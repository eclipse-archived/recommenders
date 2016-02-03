/**
 * Copyright (c) 2015 Codetrails GmbH.
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

import java.text.MessageFormat;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.internal.snipmatch.rcp.completion.TextTemplateContextType;
import org.eclipse.recommenders.internal.snipmatch.rcp.l10n.Messages;

public final class TextSnippetSourceValidator {

    private static final String VALID_SNIPPET = ""; //$NON-NLS-1$

    private TextSnippetSourceValidator() {
        // Not meant to be instantiated
    }

    public static String isSourceValid(String source) {
        TemplateContextType contextType = TextTemplateContextType.getInstance();
        Template template = new Template("name", "description", SNIPMATCH_CONTEXT_ID, source, true); //$NON-NLS-1$ //$NON-NLS-2$
        DocumentTemplateContext context = new DocumentTemplateContext(contextType, new Document(), new Position(0));
        try {
            context.evaluate(template);
        } catch (Exception e) {
            if (e.getMessage() == null) {
                return MessageFormat.format(Messages.DIALOG_MESSAGE_NO_MESSAGE_EXCEPTION, e.getClass().getName());
            }
            return e.getMessage();
        }
        return VALID_SNIPPET;
    }
}

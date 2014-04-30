/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.swt.graphics.Image;

public class SnippetProposal extends TemplateProposal {

    private final ISnippet snippet;

    private final boolean valid;

    public static SnippetProposal newSnippetProposal(ISnippet snippet, Template template, TemplateContext context,
            IRegion region, Image image) throws BadLocationException, TemplateException {
        boolean valid = false;
        try {
            context.evaluate(template);
            valid = true;
        } catch (Exception e) {
            context = new JavaContext(context.getContextType(), new Document(), new Position(0), null);
            context.evaluate(template);
        }
        return new SnippetProposal(snippet, template, context, region, image, valid);
    }

    private SnippetProposal(ISnippet snippet, Template template, TemplateContext context, IRegion region, Image image,
            boolean valid) {

        super(template, context, region, image);
        this.snippet = snippet;
        this.valid = valid;
    }

    @Override
    public boolean isValidFor(IDocument document, int offset) {
        return valid;
    }

    @Override
    public String getAdditionalProposalInfo() {
        StringBuilder additionalProposalInfo = new StringBuilder();
        if (!valid) {
            additionalProposalInfo.append(format(Messages.WARNING_CANNOT_APPLY_SNIPPET, "// XXX")); //$NON-NLS-2$
            additionalProposalInfo.append(LINE_SEPARATOR);
            additionalProposalInfo.append(format(Messages.WARNING_REPOSITION_CURSOR, "// FIXME")); //$NON-NLS-2$
            additionalProposalInfo.append(LINE_SEPARATOR);
            additionalProposalInfo.append(LINE_SEPARATOR);
        }
        if (!isEmpty(snippet.getDescription())) {
            additionalProposalInfo.append("// "); //$NON-NLS-1$
            additionalProposalInfo.append(snippet.getDescription());
            additionalProposalInfo.append(LINE_SEPARATOR);
            return additionalProposalInfo + super.getAdditionalProposalInfo();
        }
        additionalProposalInfo.append(super.getAdditionalProposalInfo());
        return additionalProposalInfo.toString();
    }

    public ISnippet getSnippet() {
        return snippet;
    }
}

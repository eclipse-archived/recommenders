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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.eclipse.recommenders.internal.snipmatch.rcp.LogMessages.ERROR_SNIPPET_COULD_NOT_BE_EVALUATED;
import static org.eclipse.recommenders.utils.Logs.log;

import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.ui.javaeditor.IndentUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.swt.graphics.Image;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

@SuppressWarnings("restriction")
public class SnippetProposal extends TemplateProposal implements ICompletionProposalExtension6 {

    private final ISnippet snippet;
    private final int repositoryRelevance;
    private TemplateContext context;
    private Boolean valid = null;

    public static SnippetProposal newSnippetProposal(Recommendation<ISnippet> recommendation, int repositoryRelevance,
            Template template, TemplateContext context, IRegion region, Image image) throws BadLocationException,
            TemplateException {
        int relevance = (int) (recommendation.getRelevance() * 100);
        return new SnippetProposal(recommendation.getProposal(), relevance, repositoryRelevance, template, context,
                region, image);
    }

    private SnippetProposal(ISnippet snippet, int relevance, int repositoryRelevance, Template template,
            TemplateContext context, IRegion region, Image image) {
        super(template, context, region, image, relevance);

        this.repositoryRelevance = repositoryRelevance;
        this.context = context;
        this.snippet = snippet;
    }

    @Override
    public boolean isValidFor(IDocument document, int offset) {
        return isValid();
    }

    private boolean isValid() {
        if (valid != null) {
            return valid;
        }

        valid = false;
        try {
            context.evaluate(getTemplate());
            valid = true;
        } catch (Exception e) {
            context = new JavaContext(context.getContextType(), new Document(), new Position(0), null);
            try {
                context.evaluate(getTemplate());
            } catch (Exception e1) {
                log(ERROR_SNIPPET_COULD_NOT_BE_EVALUATED, e, snippet.getName(), snippet.getUuid());
                return false;
            }
        }
        return valid;
    }

    @Override
    public String getAdditionalProposalInfo() {
        StringBuilder header = new StringBuilder();

        if (!isValid()) {
            header.append(format(Messages.WARNING_CANNOT_APPLY_SNIPPET, "// XXX")); //$NON-NLS-1$
            header.append(LINE_SEPARATOR);
            header.append(format(Messages.WARNING_REPOSITION_CURSOR, "// TODO")); //$NON-NLS-1$
            header.append(LINE_SEPARATOR);
            header.append(LINE_SEPARATOR);
        }

        if (!isEmpty(snippet.getDescription())) {
            header.append("// "); //$NON-NLS-1$
            header.append(snippet.getDescription());
            header.append(LINE_SEPARATOR);
        }

        try {
            // Header comments *must* not be included in fixIndentation due to Bug 436490.
            return header + fixIndentation(super.getAdditionalProposalInfo());
        } catch (BadLocationException e) {
            return null;
        }
    }

    private String fixIndentation(String additionalProposalInfo) throws BadLocationException {
        IDocument document = new Document(additionalProposalInfo);
        IndentUtil.indentLines(document, new LineRange(0, document.getNumberOfLines()), null, null);
        return document.get();
    }

    @Override
    public StyledString getStyledDisplayString() {
        StyledString styledString = new StyledString();
        styledString.append(createDisplayString(snippet));
        if (!snippet.getTags().isEmpty()) {
            styledString.append(" - ", StyledString.QUALIFIER_STYLER);
            styledString.append(Joiner.on(", ").join(Ordering.natural().sortedCopy(snippet.getTags())),
                    StyledString.COUNTER_STYLER);
        }
        return styledString;
    }

    @Override
    public String getDisplayString() {
        return getStyledDisplayString().getString();
    }

    public static String createDisplayString(ISnippet snippet) {
        if (isNullOrEmpty(snippet.getDescription())) {
            return snippet.getName();
        } else {
            return format(Messages.SEARCH_DISPLAY_STRING, snippet.getName(), snippet.getDescription());
        }
    }

    public ISnippet getSnippet() {
        return snippet;
    }

    public int getRepositoryRelevance() {
        return repositoryRelevance;
    }

    @VisibleForTesting
    public TemplateContext getTemplateContext() {
        return context;
    }
}

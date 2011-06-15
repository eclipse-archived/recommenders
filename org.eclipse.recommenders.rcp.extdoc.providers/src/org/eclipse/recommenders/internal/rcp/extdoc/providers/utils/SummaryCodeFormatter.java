/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.rcp.extdoc.providers.utils;

import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class SummaryCodeFormatter {

    private CodeFormatter formatter;
    private Map<String, Object> options;

    public SummaryCodeFormatter() {
        initializeCodeFormatterOptions();
        initializeCodeFormatter();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> initializeCodeFormatterOptions() {
        options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
        options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "120");
        //
        // // initialize the compiler settings to be able to format 1.5 code
        // options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
        // options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
        // JavaCore.VERSION_1_5);
        // options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
        //
        // final String alignment =
        // DefaultCodeFormatterConstants.createAlignmentValue(false,
        // DefaultCodeFormatterConstants.WRAP_COMPACT,
        // DefaultCodeFormatterConstants.INDENT_DEFAULT);
        // options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_ALLOCATION_EXPRESSION,
        // alignment);
        // options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ARGUMENTS_IN_METHOD_INVOCATION,
        // alignment);
        // options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ASSIGNMENT,
        // alignment);
        // options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_SELECTOR_IN_METHOD_INVOCATION,
        // alignment);
        return options;
    }

    private void initializeCodeFormatter() {
        formatter = ToolFactory.createCodeFormatter(options);
    }

    public boolean format(final IDocument document) {
        final String sourceCode = document.get();
        final int startPosition = 0;
        final int length = document.getLength();
        final int indentationLevel = 0;
        //
        final TextEdit edit = formatter.format(CodeFormatter.K_STATEMENTS, sourceCode, startPosition, length,
                indentationLevel, SystemUtils.LINE_SEPARATOR);
        if (!couldComputeRequiredTextEdits(edit)) {
            return false;
        }
        return applyTextFormattings(document, edit);
    }

    private boolean applyTextFormattings(final IDocument document, final TextEdit edit) {
        try {
            edit.apply(document);
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean couldComputeRequiredTextEdits(final TextEdit edit) {
        return edit != null;
    }
}

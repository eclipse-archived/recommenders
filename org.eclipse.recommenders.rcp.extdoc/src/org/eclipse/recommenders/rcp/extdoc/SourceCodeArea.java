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
package org.eclipse.recommenders.rcp.extdoc;

import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.TextEdit;

import org.apache.commons.lang3.SystemUtils;

@SuppressWarnings({ "restriction", "unchecked" })
public final class SourceCodeArea extends JavaSourceViewer {

    private static final IPreferenceStore store = JavaPlugin.getDefault().getCombinedPreferenceStore();
    private static final IColorManager colorManager = JavaPlugin.getDefault().getJavaTextTools().getColorManager();
    private static final JavaSourceViewerConfiguration configuration = new JavaSourceViewerConfiguration(colorManager,
            store, null, null);

    private static final Map<String, Object> options;
    private static final CodeFormatter formatter;

    static {
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
        formatter = ToolFactory.createCodeFormatter(options);
    }

    public SourceCodeArea(final Composite parent) {
        super(parent, null, null, false, SWT.READ_ONLY | SWT.WRAP, store);

        configure(configuration);
        getTextWidget().setFont(SwtFactory.CODEFONT);
        setEditable(false);
        getTextWidget().setLayoutData(GridDataFactory.fillDefaults().indent(20, 0).create());
    }

    public void setCode(final String code) {
        final IDocument document = new Document(code);
        format(document);
        setInput(document);
    }

    private boolean format(final IDocument document) {
        final String sourceCode = document.get();
        final int startPosition = 0;
        final int length = document.getLength();
        final int indentationLevel = 0;
        final TextEdit edit = formatter.format(CodeFormatter.K_STATEMENTS, sourceCode, startPosition, length,
                indentationLevel, SystemUtils.LINE_SEPARATOR);
        if (edit == null) {
            return false;
        }
        return applyTextFormattings(document, edit);
    }

    private boolean applyTextFormattings(final IDocument document, final TextEdit edit) {
        try {
            edit.apply(document);
            return true;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

}

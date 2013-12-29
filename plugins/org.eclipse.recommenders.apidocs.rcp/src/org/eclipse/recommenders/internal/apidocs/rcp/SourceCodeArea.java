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
package org.eclipse.recommenders.internal.apidocs.rcp;

import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings({ "restriction", "unchecked" })
public final class SourceCodeArea extends JavaSourceViewer {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

    private static final IPreferenceStore STORE = JavaPlugin.getDefault().getCombinedPreferenceStore();
    private static final IColorManager COLOR_MANAGER = JavaPlugin.getDefault().getJavaTextTools().getColorManager();
    private static final JavaSourceViewerConfiguration CONFIGURATION = new JavaSourceViewerConfiguration(COLOR_MANAGER,
            STORE, null, IJavaPartitions.JAVA_PARTITIONING);;

    private static final Map<String, Object> OPTIONS;
    private static final CodeFormatter FORMATTER;

    static {
        OPTIONS = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
        OPTIONS.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "120"); //$NON-NLS-1$
        FORMATTER = ToolFactory.createCodeFormatter(OPTIONS);
    }

    public SourceCodeArea(final Composite parent) {
        super(parent, null, null, false, SWT.READ_ONLY | SWT.WRAP, STORE);

        configure(CONFIGURATION);
        getTextWidget().setFont(ApidocsViewUtils.CODEFONT);
        setEditable(false);
        getTextWidget().setLayoutData(GridDataFactory.fillDefaults().indent(20, 0).create());
    }

    public void setCode(final String code) {
        final IDocument document = new Document(code);
        format(document);
        JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
        textTools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        setInput(document);
    }

    private static void format(final IDocument document) {
        final String sourceCode = document.get();
        final int length = document.getLength();
        final TextEdit edit = FORMATTER.format(CodeFormatter.K_STATEMENTS, sourceCode, 0, length, 0, LINE_SEPARATOR);
        if (edit != null) {
            applyTextFormattings(document, edit);
        }
    }

    private static void applyTextFormattings(final IDocument document, final TextEdit edit) {
        try {
            edit.apply(document);
        } catch (final MalformedTreeException e) {
            throw new IllegalStateException(e);
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }
}

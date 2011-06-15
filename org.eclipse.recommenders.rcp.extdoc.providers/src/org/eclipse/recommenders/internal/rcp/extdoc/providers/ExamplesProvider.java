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
package org.eclipse.recommenders.internal.rcp.extdoc.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TemplateEditDialog;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.SummaryCodeFormatter;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.CodeExamplesServer;
import org.eclipse.recommenders.server.extdoc.types.CodeExamples;
import org.eclipse.recommenders.server.extdoc.types.CodeSnippet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

@SuppressWarnings("restriction")
public final class ExamplesProvider extends AbstractProviderComposite {

    private Composite container;
    private final CodeExamplesServer server = new CodeExamplesServer();

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return true;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        final Composite container = SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);
        this.container = container;
        return container;
    }

    @Override
    protected boolean updateContent(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayContentForType((IType) element);
        } else if (element instanceof IMethod) {
            return displayContentForMethod((IMethod) element);
        }
        return false;
    }

    private boolean displayContentForMethod(final IMethod element) {
        try {
            disposeContainerChildren();

            final IMethod overriddenMethod = SuperTypeHierarchyCache
                    .getMethodOverrideTester(element.getDeclaringType()).findOverriddenMethod(element, true);
            if (overriddenMethod == null) {
                return false;
            }

            final CodeExamples codeExamples = server.getOverridenMethodCodeExamples(overriddenMethod);
            final CodeSnippet[] snippets = codeExamples.getExamples();
            for (int i = 0; i < snippets.length; i++) {
                createSnippetVisualization(i, element, snippets[i]);
            }
            container.layout(true);
        } catch (final JavaModelException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void disposeContainerChildren() {
        for (final Control children : container.getChildren()) {
            children.dispose();
        }
    }

    private boolean displayContentForType(final IType element) {
        return false;
    }

    private void createSnippetVisualization(final int snippetIndex, final IMethod element, final CodeSnippet snippet) {
        createEditAndRatingHeader(snippetIndex, element);
        createSourceCodeArea(snippet);
    }

    private void createEditAndRatingHeader(final int snippetIndex, final IMethod element) {
        final String text = "Example #" + (snippetIndex + 1) + ":";
        final TextAndFeaturesLine line = new TextAndFeaturesLine(container, text, element, element.getElementName(),
                this, server, new TemplateEditDialog(getShell()));
        line.createStyleRange(0, text.length(), SWT.BOLD, false, false);
    }

    private void createSourceCodeArea(final CodeSnippet snippet) {
        final IPreferenceStore store = JavaPlugin.getDefault().getCombinedPreferenceStore();
        final JavaTextTools javaTextTools = JavaPlugin.getDefault().getJavaTextTools();
        final IColorManager colorManager = javaTextTools.getColorManager();

        final JavaSourceViewer sourceCodeViewer = new JavaSourceViewer(container, null, null, false, SWT.READ_ONLY
                | SWT.WRAP, store);
        final JavaSourceViewerConfiguration configuration = new JavaSourceViewerConfiguration(colorManager, store,
                null, null);

        sourceCodeViewer.configure(configuration);

        final Font font = JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        // final FontData[] fD = font.getFontData();
        // fD[0].setHeight(10);
        // font = new Font(rootControl.getDisplay(), fD[0]);

        sourceCodeViewer.getTextWidget().setFont(font);
        sourceCodeViewer.setEditable(false);
        sourceCodeViewer.getTextWidget().setLayoutData(GridDataFactory.fillDefaults().indent(20, 0).create());

        final IDocument document = new Document(snippet.getCode());
        final SummaryCodeFormatter codeFormatter = new SummaryCodeFormatter();
        codeFormatter.format(document);
        sourceCodeViewer.setInput(document);
    }

}

/**
 * Copyright (c) 2013 Madhuranga Lakjeewa.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Madhuranga Lakjeewa - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.ElementTypeResolver;
import org.eclipse.jdt.internal.corext.template.java.ImportsResolver;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.corext.template.java.LinkResolver;
import org.eclipse.jdt.internal.corext.template.java.NameResolver;
import org.eclipse.jdt.internal.corext.template.java.TypeResolver;
import org.eclipse.jdt.internal.corext.template.java.VarResolver;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateProposal;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * TemplateProcessor process the selected snippet and insert the code into editor
 */
@SuppressWarnings("restriction")
public class TemplateProcessor {

    private TemplateContextType javaContextType;
    private String contextId = "SnipMatch-Java-Context"; //$NON-NLS-1$

    protected TemplateProcessor() {
        javaContextType = createContextType();
    }

    public void insertTemplate(ISnippet snippet) {
        AbstractTextEditor activeEditor = (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActiveEditor();
        activeEditor.setFocus();
        ISourceViewer sourceViewer = (ISourceViewer) activeEditor.getAdapter(ITextOperationTarget.class);
        Point range = sourceViewer.getSelectedRange();
        Template template = new Template("", "", contextId, snippet.getCode(), true); //$NON-NLS-1$ //$NON-NLS-2$
        IRegion region = new Region(range.x, range.y);
        ICompilationUnit cu = (ICompilationUnit) EditorUtility.getEditorInputJavaElement(activeEditor, false);
        Position p = new Position(range.x, range.y);
        TemplateContext ctx = new JavaContext(getJavaContextType(), sourceViewer.getDocument(), p, cu);
        TemplateProposal proposal = new TemplateProposal(template, ctx, region, null);
        proposal.apply(sourceViewer, (char) 0, 0, 0);
    }

    private TemplateContextType createContextType() {
        JavaContextType contextType = new JavaContextType();
        contextType.setId(contextId);
        contextType.initializeContextTypeResolvers();

        TemplateVariableResolver importsResolver = new ImportsResolver();
        importsResolver.setType("import"); //$NON-NLS-1$
        contextType.addResolver(importsResolver);

        TemplateVariableResolver varResolver = new VarResolver();
        varResolver.setType("var"); //$NON-NLS-1$
        contextType.addResolver(varResolver);

        TemplateVariableResolver typeResolver = new TypeResolver();
        typeResolver.setType("newType"); //$NON-NLS-1$
        contextType.addResolver(typeResolver);

        TemplateVariableResolver linkResolver = new LinkResolver();
        linkResolver.setType("link"); //$NON-NLS-1$
        contextType.addResolver(linkResolver);

        TemplateVariableResolver nameResolver = new NameResolver();
        nameResolver.setType("newName"); //$NON-NLS-1$
        contextType.addResolver(nameResolver);

        TemplateVariableResolver elementTypeResolver = new ElementTypeResolver();
        elementTypeResolver.setType("elemType"); //$NON-NLS-1$
        contextType.addResolver(elementTypeResolver);

        return contextType;
    }

    public TemplateContextType getJavaContextType() {
        return javaContextType;
    }
}

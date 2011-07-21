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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.selection.JavaElementLocation;
import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.ElementResolver;
import org.eclipse.recommenders.internal.rcp.extdoc.providers.utils.VariableResolver;
import org.eclipse.recommenders.rcp.extdoc.AbstractProviderComposite;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.server.extdoc.CodeExamplesServer;
import org.eclipse.recommenders.server.extdoc.types.CodeExamples;
import org.eclipse.recommenders.server.extdoc.types.CodeSnippet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.UIJob;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ExamplesProvider extends AbstractProviderComposite {

    private Composite container;
    private final CodeExamplesServer server;

    @Inject
    ExamplesProvider(final CodeExamplesServer server) {
        this.server = server;
    }

    @Override
    public boolean isAvailableForLocation(final JavaElementLocation location) {
        return location != JavaElementLocation.PACKAGE_DECLARATION;
    }

    @Override
    protected Control createContentControl(final Composite parent) {
        container = SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);
        return container;
    }

    @Override
    public boolean selectionChanged(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayContentForType((IType) element);
        } else if (element instanceof IMethod) {
            return displayContentForMethod((IMethod) element);
        } else if (element instanceof ILocalVariable) {
            return displayContentForType(VariableResolver.resolveTypeSignature((ILocalVariable) element));
        } else if (element instanceof SourceField) {
            return displayContentForType(VariableResolver.resolveTypeSignature((SourceField) element));
        }
        return false;
    }

    private boolean displayContentForMethod(final IMethod method) {
        try {
            if (method.isConstructor()) {
                return displayContentForType(method.getDeclaringType());
            }
            final MethodOverrideTester overrideTester = SuperTypeHierarchyCache.getMethodOverrideTester(method
                    .getDeclaringType());
            final IMethod overriddenMethod = overrideTester.findOverriddenMethod(method, true);
            if (overriddenMethod == null) {
                return false;
            }
            return displayCodeSnippets(ElementResolver.toRecMethod(method),
                    server.getOverridenMethodCodeExamples(ElementResolver.toRecMethod(overriddenMethod)));
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean displayContentForType(final IType type) {
        if (type == null) {
            return false;
        }
        final ITypeName name = ElementResolver.toRecType(type);
        return displayCodeSnippets(name, server.getTypeCodeExamples(name));
    }

    private boolean displayCodeSnippets(final IName element, final CodeExamples codeExamples) {
        if (codeExamples == null) {
            return false;
        }
        new UIJob("Updating Examples Provider") {
            @Override
            public IStatus runInUIThread(final IProgressMonitor monitor) {
                if (!container.isDisposed()) {
                    disposeChildren(container);
                    final CodeSnippet[] snippets = codeExamples.getExamples();
                    for (int i = 0; i < snippets.length; ++i) {
                        createSnippetVisualization(i, element, snippets[i].getCode());
                    }
                    container.layout(true);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
        return true;
    }

    private void createSnippetVisualization(final int snippetIndex, final IName element, final String snippet) {
        createEditAndRatingHeader(snippetIndex, element);
        SwtFactory.createSourceCodeArea(container, snippet);
    }

    private void createEditAndRatingHeader(final int snippetIndex, final IName element) {
        final String text = "Example #" + (snippetIndex + 1) + ":";
        final TextAndFeaturesLine line = new TextAndFeaturesLine(container, text, element, this, server);
        line.createStyleRange(0, text.length(), SWT.BOLD, false, false);
    }
}

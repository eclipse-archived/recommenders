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
package org.eclipse.recommenders.internal.extdoc.rcp.providers;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.recommenders.extdoc.rcp.AbstractTitledProvider;
import org.eclipse.recommenders.extdoc.rcp.ProviderUiUpdateJob;
import org.eclipse.recommenders.extdoc.rcp.SwtFactory;
import org.eclipse.recommenders.extdoc.rcp.UiUtils;
import org.eclipse.recommenders.extdoc.rcp.feedback.CommunityFeedback;
import org.eclipse.recommenders.extdoc.rcp.selection.selection.IJavaElementSelection;
import org.eclipse.recommenders.extdoc.transport.CodeExamplesServer;
import org.eclipse.recommenders.extdoc.transport.types.CodeExamples;
import org.eclipse.recommenders.extdoc.transport.types.CodeSnippet;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.swt.TextAndFeaturesLine;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.ElementResolver;
import org.eclipse.recommenders.internal.extdoc.rcp.providers.utils.VariableResolver;
import org.eclipse.recommenders.utils.names.IName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public final class ExamplesProvider extends AbstractTitledProvider {

    private final CodeExamplesServer server;

    @Inject
    public ExamplesProvider(final CodeExamplesServer server) {
        this.server = server;
    }

    @Override
    protected Composite createContentComposite(final Composite parent) {
        return SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);
    }

    @Override
    public ProviderUiUpdateJob updateSelection(final IJavaElementSelection selection) {
        final IJavaElement element = selection.getJavaElement();
        if (element instanceof IType) {
            return displayContentForType(ElementResolver.toRecType((IType) element));
        } else if (element instanceof ITypeRoot) {
            return displayContentForType(ElementResolver.toRecType(((ITypeRoot) element).findPrimaryType()));
        } else if (element instanceof IMethod) {
            return displayContentForMethod((IMethod) element);
        } else if (element instanceof ILocalVariable) {
            return displayContentForType(VariableResolver.resolveTypeSignature((ILocalVariable) element));
        } else if (element instanceof SourceField) {
            return displayContentForType(VariableResolver.resolveTypeSignature((SourceField) element));
        }
        return null;
    }

    private ProviderUiUpdateJob displayContentForMethod(final IMethod method) {
        try {
            if (method.isConstructor()) {
                return displayContentForType(ElementResolver.toRecType(method.getDeclaringType()));
            }
            final MethodOverrideTester overrideTester = SuperTypeHierarchyCache.getMethodOverrideTester(method
                    .getDeclaringType());
            final IMethod overriddenMethod = overrideTester.findOverriddenMethod(method, true);
            if (overriddenMethod == null) {
                return null;
            }
            return displayCodeSnippets(ElementResolver.toRecMethod(method),
                    server.getOverridenMethodCodeExamples(ElementResolver.toRecMethod(overriddenMethod)));
        } catch (final JavaModelException e) {
            throw new IllegalStateException(e);
        }
    }

    private ProviderUiUpdateJob displayContentForType(final ITypeName type) {
        if (type == null) {
            return null;
        }
        return displayCodeSnippets(type, server.getTypeCodeExamples(type));
    }

    private ProviderUiUpdateJob displayCodeSnippets(final IName element, final CodeExamples codeExamples) {
        final CommunityFeedback features = CommunityFeedback.create(element, null, this, server);
        return new ProviderUiUpdateJob() {
            @Override
            public void run(final Composite composite) {
                UiUtils.disposeChildren(composite);
                if (codeExamples == null) {
                    final Label label = new Label(composite, SWT.NONE);
                    label.setText("Sorry, this feature is currently under development. It will follow soon when ready.");
                } else {
                    final CodeSnippet[] snippets = codeExamples.getExamples();
                    for (int i = 0; i < snippets.length; ++i) {
                        createSnippetVisualization(i, features, snippets[i].getCode(), composite);
                    }
                }
            }
        };
    }

    static void createSnippetVisualization(final int snippetIndex, final CommunityFeedback features,
            final String snippet, final Composite composite) {
        createEditAndRatingHeader(snippetIndex, features, composite);
        SwtFactory.createSourceCodeArea(composite, snippet);
    }

    private static void createEditAndRatingHeader(final int snippetIndex, final CommunityFeedback features,
            final Composite composite) {
        final String text = "Example #" + (snippetIndex + 1) + ":";
        final TextAndFeaturesLine line = TextAndFeaturesLine.create(composite, text, features);
        line.createStyleRange(0, text.length(), SWT.BOLD, false, false);
    }
}

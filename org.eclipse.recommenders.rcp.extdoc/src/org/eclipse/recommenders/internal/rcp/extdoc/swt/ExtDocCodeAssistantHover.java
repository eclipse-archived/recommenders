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
package org.eclipse.recommenders.internal.rcp.extdoc.swt;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.internal.rcp.extdoc.UiManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public final class ExtDocCodeAssistantHover {

    private ExtDocCodeAssistantHover() {
    }

    public static void install(final JavaEditor editor, final UiManager uiManager, final ProviderStore providerStore) {
        final JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer();
        final JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
        final IPreferenceStore store = ExtDocCodeAssistantHover.stealPreferenceStore(viewer);
        viewer.unconfigure();
        viewer.configure(new ViewerConfiguration(uiManager, providerStore, textTools.getColorManager(), store, editor,
                IJavaPartitions.JAVA_PARTITIONING));
    }

    // TODO: Find another way to get the right preference store!
    private static IPreferenceStore stealPreferenceStore(final JavaSourceViewer viewer) {
        try {
            final Field privateField = JavaSourceViewer.class.getDeclaredField("fPreferenceStore");
            privateField.setAccessible(true);
            return (IPreferenceStore) privateField.get(viewer);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class ViewerConfiguration extends JavaSourceViewerConfiguration {

        private final UiManager uiManager;
        private final ProviderStore providerStore;

        private final IInformationControlCreator creator = new IInformationControlCreator() {
            @Override
            public IInformationControl createInformationControl(final Shell parent) {
                return new InformationControl(parent);
            }
        };

        private ViewerConfiguration(final UiManager uiManager, final ProviderStore providerStore,
                final IColorManager colorManager, final IPreferenceStore preferenceStore, final ITextEditor editor,
                final String partitioning) {
            super(colorManager, preferenceStore, editor, partitioning);
            this.uiManager = uiManager;
            this.providerStore = providerStore;
        }

        @Override
        public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
            final ContentAssistant assist = (ContentAssistant) super.getContentAssistant(sourceViewer);
            assist.setInformationControlCreator(creator);
            return assist;
        }

        private final class InformationControl extends AbstractExtDocInformationControl {

            private StyledText text;
            private Composite parent;
            private boolean lastWasExtDoc = true;

            public InformationControl(final Shell parent) {
                super(parent, uiManager, providerStore, creator);
            }

            @Override
            protected void createContent(final Composite parentComposite) {
                super.createContent(parentComposite);
                parent = parentComposite;
            }

            @Override
            public void setInput(final Object input) {
                if (input instanceof JavadocBrowserInformationControlInput) {
                    displayExtDocContent(input);
                } else {
                    displayTextContent(input);
                }
            }

            private void displayExtDocContent(final Object input) {
                if (!lastWasExtDoc) {
                    for (final Control child : parent.getChildren()) {
                        child.dispose();
                    }
                    super.createContent(parent);
                    super.setInput(input);
                    parent.layout(true);
                    lastWasExtDoc = true;
                } else {
                    super.setInput(input);
                }
            }

            private void displayTextContent(final Object input) {
                if (lastWasExtDoc) {
                    for (final Control child : parent.getChildren()) {
                        child.dispose();
                    }
                    text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY);
                    text.setForeground(parent.getForeground());
                    text.setBackground(parent.getBackground());
                    text.setFont(JFaceResources.getDialogFont());
                    text.setWordWrap(true);
                    text.setIndent(1);
                    lastWasExtDoc = false;
                }
                text.setText(input.toString());
                parent.layout(true);
            }

            @Override
            protected IJavaElementSelection getSelection(final Object input) {
                final JavadocBrowserInformationControlInput in = (JavadocBrowserInformationControlInput) input;
                final IJavaElement element = in.getElement();
                return uiManager.getLastSelection().copy(element);
            }

        }
    }
}

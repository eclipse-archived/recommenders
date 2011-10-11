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
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.lang.reflect.Field;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.recommenders.commons.internal.selection.JavaElementSelection;
import org.eclipse.recommenders.commons.selection.IJavaElementSelection;
import org.eclipse.recommenders.commons.utils.Option;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
final class ExtDocCodeAssistantHover {

    private ExtDocCodeAssistantHover() {
    }

    static void installToEditor(final JavaEditor editor, final UiManager uiManager, final ProviderStore providerStore,
            final UpdateService updateService) {
        final JavaSourceViewer viewer = (JavaSourceViewer) editor.getViewer();
        final IColorManager colorManager = JavaPlugin.getDefault().getJavaTextTools().getColorManager();
        viewer.unconfigure();
        viewer.configure(new ViewerConfiguration(uiManager, providerStore, updateService, colorManager,
                stealPreferenceStore(viewer), editor, IJavaPartitions.JAVA_PARTITIONING));
    }

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

        private final IInformationControlCreator creator;

        ViewerConfiguration(final UiManager uiManager, final ProviderStore providerStore,
                final UpdateService updateService, final IColorManager colorManager,
                final IPreferenceStore preferenceStore, final ITextEditor editor, final String partitioning) {
            super(colorManager, preferenceStore, editor, partitioning);
            creator = new IInformationControlCreator() {
                @Override
                public IInformationControl createInformationControl(final Shell parent) {
                    return new InformationControl(parent, uiManager, providerStore, updateService, null);
                }
            };
        }

        @Override
        public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
            final ContentAssistant assist = (ContentAssistant) super.getContentAssistant(sourceViewer);
            assist.setInformationControlCreator(creator);
            return assist;
        }

        private final class InformationControl extends AbstractHoverInformationControl {

            private StyledText text;
            private Composite hoverComposite;
            private boolean lastWasExtDoc = true;

            public InformationControl(final Shell parent, final UiManager uiManager, final ProviderStore providerStore,
                    final UpdateService updateService, final InformationControl copy) {
                super(parent, uiManager, providerStore, updateService, copy);
            }

            @Override
            protected void createContent(final Composite parentComposite) {
                super.createContent(parentComposite);
                hoverComposite = parentComposite;
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
                if (lastWasExtDoc) {
                    super.setInput(input);
                } else {
                    for (final Control child : hoverComposite.getChildren()) {
                        child.dispose();
                    }
                    super.createContent(hoverComposite);
                    super.setInput(input);
                    hoverComposite.layout(true);
                    lastWasExtDoc = true;
                }
            }

            private void displayTextContent(final Object input) {
                if (lastWasExtDoc) {
                    for (final Control child : hoverComposite.getChildren()) {
                        child.dispose();
                    }
                    text = new StyledText(hoverComposite, SWT.MULTI | SWT.READ_ONLY);
                    text.setForeground(hoverComposite.getForeground());
                    text.setBackground(hoverComposite.getBackground());
                    text.setFont(JFaceResources.getDialogFont());
                    text.setWordWrap(true);
                    text.setIndent(1);
                    lastWasExtDoc = false;
                }
                text.setText(input.toString());
                hoverComposite.layout(true);
            }

            @Override
            protected IJavaElementSelection getSelection(final Object input) {
                final IJavaElement element = ((JavadocBrowserInformationControlInput) input).getElement();
                final Option<IJavaElementSelection> lastSelection = getUiManager().getLastSelection();
                if (lastSelection.hasValue()) {
                    return lastSelection.get().copy(element);
                }

                return new JavaElementSelection(element);
            }

            @Override
            public IInformationControlCreator getInformationPresenterControlCreator() {
                return new IInformationControlCreator() {
                    @Override
                    public IInformationControl createInformationControl(final Shell parent) {
                        return new InformationControl(parent, getUiManager(), getProviderStore(), getUpdateService(),
                                InformationControl.this);
                    }
                };
            }
        }
    }
}

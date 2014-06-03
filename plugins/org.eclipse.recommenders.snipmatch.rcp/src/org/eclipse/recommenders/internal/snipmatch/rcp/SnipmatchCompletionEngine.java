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

import static org.eclipse.jface.text.IDocument.DEFAULT_CONTENT_TYPE;
import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.PREF_SEARCH_BOX_BACKGROUND;

import javax.inject.Inject;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.ui.text.java.RelevanceSorter;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateInformationControlCreator;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.recommenders.snipmatch.ISnippet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;

/**
 * Snipmatch snippet completion engine.
 * <p>
 * Makes use of JFace content assist infrastructure - but in a bit unusual way. The event handling is probably calling
 * for trouble later.
 */
@SuppressWarnings("restriction")
public class SnipmatchCompletionEngine {

    private static final int SEARCH_BOX_WIDTH = 273;

    private final SnipmatchContentAssistProcessor processor;
    private final EventBus bus;
    private final ColorRegistry colorRegistry;
    private final FontRegistry fontRegistry;
    private final ContentAssistant assistant;

    private Shell searchShell;
    private JavaContentAssistInvocationContext ctx;
    private TemplateProposal selectedProposal;
    private StyledText searchText;
    private Color searchBg;
    private Font searchFont;

    @Inject
    public SnipmatchCompletionEngine(SnipmatchContentAssistProcessor processor, EventBus bus,
            ColorRegistry colorRegistry, FontRegistry fontRegistry) {
        this.processor = processor;
        this.bus = bus;
        this.colorRegistry = colorRegistry;
        this.fontRegistry = fontRegistry;
        assistant = newContentAssistant();
    }

    private ContentAssistant newContentAssistant() {
        ContentAssistant assistant = new ContentAssistant();
        assistant.addCompletionListener(new ICompletionListener() {

            @Override
            public void assistSessionEnded(ContentAssistEvent event) {
                // not sure why we need to set this to invisible
                if (searchShell != null && !searchShell.isDisposed()) {
                    searchShell.setVisible(false);
                    searchShell.dispose();
                }
            }

            @Override
            public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
                if (proposal instanceof TemplateProposal) {
                    selectedProposal = (TemplateProposal) proposal;
                } else {
                    selectedProposal = null;
                }
            }

            @Override
            public void assistSessionStarted(ContentAssistEvent event) {
            }
        });

        assistant.setShowEmptyList(true);
        assistant.enablePrefixCompletion(true);
        assistant.enableColoredLabels(true);
        assistant.setContentAssistProcessor(processor, DEFAULT_CONTENT_TYPE);
        assistant.setInformationControlCreator(new TemplateInformationControlCreator(SWT.LEFT_TO_RIGHT));
        assistant.setEmptyMessage(Messages.COMPLETION_ENGINE_NO_SNIPPETS_FOUND);
        assistant.setRepeatedInvocationMode(true);
        assistant.setStatusLineVisible(true);
        assistant.setSorter(new RelevanceSorter());

        return assistant;
    }

    public void show(final JavaContentAssistInvocationContext ctx) {
        this.ctx = ctx;
        processor.setContext(ctx);
        assistant.install(ctx.getViewer());
        createSearchPopup();
    }

    private void createSearchPopup() {
        Shell parentShell = ctx.getViewer().getTextWidget().getShell();
        searchShell = new Shell(parentShell, SWT.ON_TOP);
        searchBg = colorRegistry.get(PREF_SEARCH_BOX_BACKGROUND);
        searchFont = fontRegistry.get("org.eclipse.recommenders.snipmatch.rcp.searchTextFont"); //$NON-NLS-1$
        searchShell.setLayout(new FillLayout());
        searchShell.addListener(SWT.Traverse, new Listener() {

            @Override
            public void handleEvent(Event e) {
                if (e.detail == SWT.TRAVERSE_ESCAPE) {
                    assistant.uninstall();
                }
            }
        });
        searchText = new StyledText(searchShell, SWT.BORDER);

        searchText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                searchShell.dispose();
            }
        });
        searchText.setBackground(searchBg);
        searchText.setFont(searchFont);
        searchText.addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent e) {
                switch (e.character) {
                case SWT.CR:
                    e.doit = false;
                    if (selectedProposal != null) {
                        assistant.uninstall();
                        if (selectedProposal.isValidFor(ctx.getDocument(), ctx.getInvocationOffset())) {
                            if (selectedProposal instanceof SnippetProposal) {
                                snippetApplied((SnippetProposal) selectedProposal);
                            }
                            selectedProposal.apply(ctx.getViewer(), (char) 0, SWT.NONE, ctx.getInvocationOffset());
                        }
                    }
                    return;
                case SWT.TAB:
                    e.doit = false;
                    return;
                }

                switch (e.keyCode) {
                case SWT.ARROW_UP:
                    execute(ContentAssistant.SELECT_PREVIOUS_PROPOSAL_COMMAND_ID);
                    return;
                case SWT.ARROW_DOWN:
                    execute(ContentAssistant.SELECT_NEXT_PROPOSAL_COMMAND_ID);
                    return;
                }
            }

            private void execute(String commandId) {
                try {
                    assistant.getHandler(commandId).execute(null);
                } catch (ExecutionException e) {
                    Throwables.propagate(e);
                }
            }
        });
        searchText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String query = searchText.getText().trim();
                processor.setTerms(query);
                assistant.showPossibleCompletions();
                assistant.showContextInformation();
            }
        });

        Point anchor = computeSearchShellPosition();
        searchShell.setLocation(anchor.x, anchor.y);
        configureShellSize();
        searchShell.open();
        searchShell.setFocus();
    }

    private void configureShellSize() {
        // Pack the shell so that it is high enough for the text field.
        searchShell.pack();
        // Set the width of the shell. Height is kept.
        searchShell.setSize(SEARCH_BOX_WIDTH, searchShell.getSize().y);
    }

    private Point computeSearchShellPosition() {
        StyledText styledText = ctx.getViewer().getTextWidget();
        Caret caret = styledText.getCaret();
        Point location = caret.getLocation();
        return styledText.toDisplay(location.x, location.y - 2);
    }

    private void snippetApplied(SnippetProposal proposal) {
        ISnippet snippet = proposal.getSnippet();
        String repoUri = null;
        // TODO How to get the repo uri?
        bus.post(new SnippetAppliedEvent(snippet.getUuid(), repoUri));
    }
}

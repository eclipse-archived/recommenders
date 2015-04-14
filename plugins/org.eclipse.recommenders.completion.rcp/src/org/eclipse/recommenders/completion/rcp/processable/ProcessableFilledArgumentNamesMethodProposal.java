/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - Initial API
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.IS_VISIBLE;
import static org.eclipse.recommenders.completion.rcp.processable.Proposals.copyStyledString;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.internal.completion.rcp.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * A method proposal with filled in argument names.
 */
@SuppressWarnings({ "restriction", "unchecked" })
public class ProcessableFilledArgumentNamesMethodProposal extends JavaMethodCompletionProposal
        implements IProcessableProposal {

    private Map<IProposalTag, Object> tags = Maps.newHashMap();
    private IRegion fSelectedRegion; // initialized by apply()
    private int[] fArgumentOffsets;
    private int[] fArgumentLengths;
    private String lastPrefix;
    private String lastPrefixStyled;
    private StyledString initialDisplayString;
    private CompletionProposal coreProposal;
    private ProposalProcessorManager mgr;

    public ProcessableFilledArgumentNamesMethodProposal(final CompletionProposal coreProposal,
            final JavaContentAssistInvocationContext context) {
        super(coreProposal, context);
        this.coreProposal = coreProposal;
    }

    // jdt code ==============================================

    @Override
    public void apply(final IDocument document, final char trigger, final int offset) {
        super.apply(document, trigger, offset);
        final int baseOffset = getReplacementOffset();
        final String replacement = getReplacementString();

        if (fArgumentOffsets != null && getTextViewer() != null) {
            try {
                final LinkedModeModel model = new LinkedModeModel();
                for (int i = 0; i != fArgumentOffsets.length; i++) {
                    final LinkedPositionGroup group = new LinkedPositionGroup();
                    group.addPosition(new LinkedPosition(document, baseOffset + fArgumentOffsets[i],
                            fArgumentLengths[i], LinkedPositionGroup.NO_STOP));
                    model.addGroup(group);
                }

                model.forceInstall();
                final JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }

                final LinkedModeUI ui = new EditorLinkedModeUI(model, getTextViewer());
                ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
                ui.setExitPolicy(new ExitPolicy(')', document));
                ui.setDoContextInfo(true);
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.enter();

                fSelectedRegion = ui.getSelectedRegion();

            } catch (final BadLocationException e) {
                JavaPlugin.log(e);
                openErrorDialog(e);
            }
        } else {
            fSelectedRegion = new Region(baseOffset + replacement.length(), 0);
        }
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#needsLinkedMode()
     */
    @Override
    protected boolean needsLinkedMode() {
        return false; // we handle it ourselves
    }

    /*
     * @see org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal#computeReplacementString()
     */
    @Override
    protected String computeReplacementString() {

        if (!hasParameters() || !hasArgumentList()) {
            return super.computeReplacementString();
        }

        final StringBuffer buffer = new StringBuffer();
        appendMethodNameReplacement(buffer);

        final char[][] parameterNames = fProposal.findParameterNames(null);
        final int count = parameterNames.length;
        fArgumentOffsets = new int[count];
        fArgumentLengths = new int[count];

        final FormatterPrefs prefs = getFormatterPrefs();

        setCursorPosition(buffer.length());

        if (prefs.afterOpeningParen) {
            buffer.append(SPACE);
        }

        for (int i = 0; i != count; i++) {
            if (i != 0) {
                if (prefs.beforeComma) {
                    buffer.append(SPACE);
                }
                buffer.append(COMMA);
                if (prefs.afterComma) {
                    buffer.append(SPACE);
                }
            }

            fArgumentOffsets[i] = buffer.length();
            buffer.append(parameterNames[i]);
            fArgumentLengths[i] = parameterNames[i].length;
        }

        if (prefs.beforeClosingParen) {
            buffer.append(SPACE);
        }

        buffer.append(RPAREN);

        return buffer.toString();
    }

    /**
     * Returns the currently active java editor, or <code>null</code> if it cannot be determined.
     *
     * @return the currently active java editor, or <code>null</code>
     */
    private JavaEditor getJavaEditor() {
        final IEditorPart part = JavaPlugin.getActivePage().getActiveEditor();
        if (part instanceof JavaEditor) {
            return (JavaEditor) part;
        } else {
            return null;
        }
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    @Override
    public Point getSelection(final IDocument document) {
        if (fSelectedRegion == null) {
            return new Point(getReplacementOffset(), 0);
        }

        return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
    }

    private void openErrorDialog(final BadLocationException e) {
        final Shell shell = getTextViewer().getTextWidget().getShell();
        MessageDialog.openError(shell, Messages.DIALOG_TITLE_FAILED_TO_GUESS_PARAMETERS, e.getMessage());
    }

    // ===========

    // getImage() is final, thus we re-implement computeImage()
    @Override
    protected Image computeImage() {
        Image image = super.computeImage();
        return mgr.decorateImage(image);
    }

    @Override
    public StyledString getStyledDisplayString() {
        if (initialDisplayString == null) {
            initialDisplayString = super.getStyledDisplayString();
            StyledString copy = copyStyledString(initialDisplayString);
            StyledString decorated = mgr.decorateStyledDisplayString(copy);
            setStyledDisplayString(decorated);
        }
        if (lastPrefixStyled != lastPrefix) {
            lastPrefixStyled = lastPrefix;
            StyledString copy = copyStyledString(initialDisplayString);
            StyledString decorated = mgr.decorateStyledDisplayString(copy);
            setStyledDisplayString(decorated);
        }
        return super.getStyledDisplayString();
    }

    @Override
    public boolean isPrefix(final String prefix, final String completion) {
        lastPrefix = prefix;
        boolean res = mgr.prefixChanged(prefix) || super.isPrefix(prefix, completion);
        setTag(IS_VISIBLE, res);
        return res;
    }

    @Override
    public String getPrefix() {
        return lastPrefix;
    }

    @Override
    public Optional<CompletionProposal> getCoreProposal() {
        return fromNullable(coreProposal);
    }

    @Override
    public ProposalProcessorManager getProposalProcessorManager() {
        return mgr;
    }

    @Override
    public void setProposalProcessorManager(ProposalProcessorManager mgr) {
        this.mgr = mgr;
    }

    @Override
    public void setTag(IProposalTag key, Object value) {
        ensureIsNotNull(key);
        if (value == null) {
            tags.remove(key);
        } else {
            tags.put(key, value);
        }
    }

    @Override
    public <T> Optional<T> getTag(IProposalTag key) {
        return Optional.fromNullable((T) tags.get(key));
    }

    @Override
    public <T> Optional<T> getTag(String key) {
        return Proposals.getTag(this, key);
    }

    @Override
    public <T> T getTag(IProposalTag key, T defaultValue) {
        T res = (T) tags.get(key);
        return res != null ? res : defaultValue;
    }

    @Override
    public <T> T getTag(String key, T defaultValue) {
        return this.<T>getTag(key).or(defaultValue);
    }

    @Override
    public ImmutableSet<IProposalTag> tags() {
        return ImmutableSet.copyOf(tags.keySet());
    }
}

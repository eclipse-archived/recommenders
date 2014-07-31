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
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.template.java.SignatureUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorHighlightingSynchronizer;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.ParameterGuesser;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.recommenders.internal.completion.rcp.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

@SuppressWarnings({ "restriction", "unchecked" })
public class ProcessableParameterGuessingProposal extends JavaMethodCompletionProposal implements IProcessableProposal {

    private Map<IProposalTag, Object> tags = Maps.newHashMap();
    private ProposalProcessorManager mgr;
    private CompletionProposal coreProposal;
    private String lastPrefix;

    protected ProcessableParameterGuessingProposal(final CompletionProposal proposal,
            final JavaContentAssistInvocationContext context, final boolean fillBestGuess) {
        super(proposal, context);
        coreProposal = proposal;
        fCoreContext = context.getCoreContext();
        fFillBestGuess = fillBestGuess;
    }

    // JDT parts below
    /** Tells whether this class is in debug mode. */
    private static final boolean DEBUG = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jdt.ui/debug/ResultCollector")); //$NON-NLS-1$//$NON-NLS-2$

    private ICompletionProposal[][] fChoices; // initialized by guessParameters()
    private Position[] fPositions; // initialized by guessParameters()

    private IRegion fSelectedRegion; // initialized by apply()
    private IPositionUpdater fUpdater;

    private final boolean fFillBestGuess;

    private final CompletionContext fCoreContext;

    private IJavaElement getEnclosingElement() {
        return fCoreContext.getEnclosingElement();
    }

    private IJavaElement[][] getAssignableElements() {
        final char[] signature = SignatureUtil.fix83600(getProposal().getSignature());
        final char[][] types = Signature.getParameterTypes(signature);

        final IJavaElement[][] assignableElements = new IJavaElement[types.length][];
        for (int i = 0; i < types.length; i++) {
            assignableElements[i] = fCoreContext.getVisibleElements(new String(types[i]));
        }
        return assignableElements;
    }

    /*
     * @see ICompletionProposalExtension#apply(IDocument, char)
     */
    @Override
    public void apply(final IDocument document, final char trigger, final int offset) {
        try {
            super.apply(document, trigger, offset);

            final int baseOffset = getReplacementOffset();
            final String replacement = getReplacementString();

            if (fPositions != null && getTextViewer() != null) {

                final LinkedModeModel model = new LinkedModeModel();

                for (int i = 0; i < fPositions.length; i++) {
                    final LinkedPositionGroup group = new LinkedPositionGroup();
                    final int positionOffset = fPositions[i].getOffset();
                    final int positionLength = fPositions[i].getLength();

                    if (fChoices[i].length < 2) {
                        group.addPosition(new LinkedPosition(document, positionOffset, positionLength,
                                LinkedPositionGroup.NO_STOP));
                    } else {
                        ensurePositionCategoryInstalled(document, model);
                        document.addPosition(getCategory(), fPositions[i]);
                        group.addPosition(new ProposalPosition(document, positionOffset, positionLength,
                                LinkedPositionGroup.NO_STOP, fChoices[i]));
                    }
                    model.addGroup(group);
                }

                model.forceInstall();
                final JavaEditor editor = getJavaEditor();
                if (editor != null) {
                    model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
                }

                final LinkedModeUI ui = new EditorLinkedModeUI(model, getTextViewer());
                ui.setExitPosition(getTextViewer(), baseOffset + replacement.length(), 0, Integer.MAX_VALUE);
                final char exitChar = replacement.charAt(replacement.length() - 1);
                ui.setExitPolicy(new ExitPolicy(exitChar, document) {
                    @Override
                    public ExitFlags doExit(LinkedModeModel model2, VerifyEvent event, int offset2, int length) {
                        if (event.character == ',') {
                            for (int i = 0; i < fPositions.length - 1; i++) { // not for the last one
                                Position position = fPositions[i];
                                if (position.offset <= offset2 && offset2 + length <= position.offset + position.length) {
                                    try {
                                        ITypedRegion partition = TextUtilities.getPartition(document,
                                                IJavaPartitions.JAVA_PARTITIONING, offset2 + length, false);
                                        if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())
                                                || offset2 + length == partition.getOffset() + partition.getLength()) {
                                            event.character = '\t';
                                            event.keyCode = SWT.TAB;
                                            return null;
                                        }
                                    } catch (BadLocationException e) {
                                        // continue; not serious enough to log
                                    }
                                }
                            }
                        } else if (event.character == ')' && exitChar != ')') {
                            // exit from link mode when user is in the last ')' position.
                            Position position = fPositions[fPositions.length - 1];
                            if (position.offset <= offset2 && offset2 + length <= position.offset + position.length) {
                                return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                            }
                        }
                        return super.doExit(model2, event, offset2, length);
                    }
                });
                ui.setCyclingMode(LinkedModeUI.CYCLE_WHEN_NO_PARENT);
                ui.setDoContextInfo(true);
                ui.enter();
                fSelectedRegion = ui.getSelectedRegion();

            } else {
                fSelectedRegion = new Region(baseOffset + replacement.length(), 0);
            }

        } catch (final BadLocationException e) {
            ensurePositionCategoryRemoved(document);
            JavaPlugin.log(e);
            openErrorDialog(e);
        } catch (final BadPositionCategoryException e) {
            ensurePositionCategoryRemoved(document);
            JavaPlugin.log(e);
            openErrorDialog(e);
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
     * @see org.eclipse.jdt.internal.ui.text.java.JavaMethodCompletionProposal#computeReplacementString()
     */
    @Override
    protected String computeReplacementString() {

        if (!hasParameters() || !hasArgumentList()) {
            return super.computeReplacementString();
        }

        final long millis = DEBUG ? System.currentTimeMillis() : 0;
        String replacement;
        try {
            replacement = computeGuessingCompletion();
        } catch (final JavaModelException x) {
            fPositions = null;
            fChoices = null;
            JavaPlugin.log(x);
            openErrorDialog(x);
            return super.computeReplacementString();
        }
        if (DEBUG) {
            System.err.println("Parameter Guessing: " + (System.currentTimeMillis() - millis)); //$NON-NLS-1$
        }

        return replacement;
    }

    /**
     * Creates the completion string. Offsets and Lengths are set to the offsets and lengths of the parameters.
     *
     * @return the completion string
     * @throws JavaModelException
     *             if parameter guessing failed
     */
    private String computeGuessingCompletion() throws JavaModelException {

        final StringBuffer buffer = new StringBuffer();
        appendMethodNameReplacement(buffer);

        final FormatterPrefs prefs = getFormatterPrefs();

        setCursorPosition(buffer.length());

        if (prefs.afterOpeningParen) {
            buffer.append(SPACE);
        }

        final char[][] parameterNames = fProposal.findParameterNames(null);

        fChoices = guessParameters(parameterNames);
        final int count = fChoices.length;
        final int replacementOffset = getReplacementOffset();

        for (int i = 0; i < count; i++) {
            if (i != 0) {
                if (prefs.beforeComma) {
                    buffer.append(SPACE);
                }
                buffer.append(COMMA);
                if (prefs.afterComma) {
                    buffer.append(SPACE);
                }
            }

            final ICompletionProposal proposal = fChoices[i][0];
            final String argument = proposal.getDisplayString();

            final Position position = fPositions[i];
            position.setOffset(replacementOffset + buffer.length());
            position.setLength(argument.length());

            if (proposal instanceof JavaCompletionProposal) {
                ((JavaCompletionProposal) proposal).setReplacementOffset(replacementOffset + buffer.length());
            }
            buffer.append(argument);
        }

        if (prefs.beforeClosingParen) {
            buffer.append(SPACE);
        }

        buffer.append(RPAREN);

        if (canAutomaticallyAppendSemicolon()) {
            buffer.append(SEMICOLON);
        }

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

    private ICompletionProposal[][] guessParameters(final char[][] parameterNames) throws JavaModelException {
        // find matches in reverse order. Do this because people tend to declare the variable meant for the last
        // parameter last. That is, local variables for the last parameter in the method completion are more
        // likely to be closer to the point of code completion. As an example consider a "delegation" completion:
        //
        // public void myMethod(int param1, int param2, int param3) {
        // someOtherObject.yourMethod(param1, param2, param3);
        // }
        //
        // The other consideration is giving preference to variables that have not previously been used in this
        // code completion (which avoids "someOtherObject.yourMethod(param1, param1, param1)";

        final int count = parameterNames.length;
        fPositions = new Position[count];
        fChoices = new ICompletionProposal[count][];

        final String[] parameterTypes = getParameterTypes();
        final ParameterGuesser guesser = new ParameterGuesser(getEnclosingElement());
        final IJavaElement[][] assignableElements = getAssignableElements();

        for (int i = count - 1; i >= 0; i--) {
            final String paramName = new String(parameterNames[i]);
            final Position position = new Position(0, 0);

            final boolean isLastParameter = i == count - 1;
            ICompletionProposal[] argumentProposals = guesser.parameterProposals(parameterTypes[i], paramName,
                    position, assignableElements[i], fFillBestGuess, isLastParameter);
            if (argumentProposals.length == 0) {
                final JavaCompletionProposal proposal = new JavaCompletionProposal(paramName, 0, paramName.length(),
                        null, paramName, 0);
                if (isLastParameter) {
                    proposal.setTriggerCharacters(new char[] { ',' });
                }
                argumentProposals = new ICompletionProposal[] { proposal };
            }

            fPositions[i] = position;
            fChoices[i] = argumentProposals;
        }

        return fChoices;
    }

    private String[] getParameterTypes() {
        final char[] signature = SignatureUtil.fix83600(fProposal.getSignature());
        final char[][] types = Signature.getParameterTypes(signature);

        final String[] ret = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = new String(Signature.toCharArray(types[i]));
        }
        return ret;
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

    private void openErrorDialog(final Exception e) {
        final Shell shell = getTextViewer().getTextWidget().getShell();
        MessageDialog.openError(shell, Messages.DIALOG_TITLE_FAILED_TO_GUESS_PARAMETERS, e.getMessage());
    }

    private void ensurePositionCategoryInstalled(final IDocument document, final LinkedModeModel model) {
        if (!document.containsPositionCategory(getCategory())) {
            document.addPositionCategory(getCategory());
            fUpdater = new InclusivePositionUpdater(getCategory());
            document.addPositionUpdater(fUpdater);

            model.addLinkingListener(new ILinkedModeListener() {

                /*
                 * @see
                 * org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel,
                 * int)
                 */
                @Override
                public void left(final LinkedModeModel environment, final int flags) {
                    ensurePositionCategoryRemoved(document);
                }

                @Override
                public void suspend(final LinkedModeModel environment) {
                }

                @Override
                public void resume(final LinkedModeModel environment, final int flags) {
                }
            });
        }
    }

    private void ensurePositionCategoryRemoved(final IDocument document) {
        if (document.containsPositionCategory(getCategory())) {
            try {
                document.removePositionCategory(getCategory());
            } catch (final BadPositionCategoryException e) {
                // ignore
            }
            document.removePositionUpdater(fUpdater);
        }
    }

    private String getCategory() {
        return "ParameterGuessingProposal_" + toString(); //$NON-NLS-1$
    }

    // ===========

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

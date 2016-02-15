/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.processable;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.completion.rcp.processable.ProposalTag.IS_VISIBLE;
import static org.eclipse.recommenders.completion.rcp.processable.Proposals.copyStyledString;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.recommenders.utils.MethodHandleUtils;
import org.eclipse.swt.graphics.Image;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

@SuppressWarnings({ "restriction", "unchecked" })
public class ProcessableMethodDeclarationCompletionProposal extends MethodDeclarationCompletionProposal
        implements IProcessableProposal {

    public static ProcessableMethodDeclarationCompletionProposal newProposal(CompletionProposal proposal, IType type,
            int relevance) throws CoreException {

        String prefix = String.valueOf(proposal.getName());
        int offset = proposal.getReplaceStart();
        int length = proposal.getReplaceEnd() - offset;

        IMethod[] methods = type.getMethods();
        if (!type.isInterface()) {
            String constructorName = type.getElementName();
            if (constructorName.length() > 0 && constructorName.startsWith(prefix)
                    && !hasMethod(methods, constructorName)) {
                return new ProcessableMethodDeclarationCompletionProposal(proposal, type, constructorName, null, offset,
                        length, relevance + 500);
            }
        }

        if (prefix.length() > 0 && !"main".equals(prefix) && !hasMethod(methods, prefix)) { //$NON-NLS-1$
            if (!JavaConventionsUtil.validateMethodName(prefix, type).matches(IStatus.ERROR)) {
                return new ProcessableMethodDeclarationCompletionProposal(proposal, type, prefix, Signature.SIG_VOID,
                        offset, length, relevance);
            }
        }
        return null;
    }

    private static boolean hasMethod(IMethod[] methods, String name) {
        for (int i = 0; i < methods.length; i++) {
            IMethod curr = methods[i];
            if (curr.getElementName().equals(name) && curr.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    private final Map<IProposalTag, Object> tags = Maps.newHashMap();
    private final CompletionProposal coreProposal;

    private ProposalProcessorManager mgr;
    private String lastPrefix;
    private String lastPrefixStyled;
    private StyledString initialDisplayString;
    private Image decoratedImage;

    public ProcessableMethodDeclarationCompletionProposal(CompletionProposal proposal, IType type, String methodName,
            String returnTypeSig, int start, int length, int relevance) {
        super(type, methodName, returnTypeSig, start, length, relevance);
        coreProposal = proposal;
    }

    @Override
    public Image getImage() {
        if (decoratedImage == null) {
            decoratedImage = mgr.decorateImage(super.getImage());
        }
        return decoratedImage;
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

    // No @Override, as introduced in JDT 3.12 (Neon) only
    protected String getPatternToEmphasizeMatch(IDocument document, int offset) {
        if (getTag(ProposalTag.IS_HIGHLIGHTED, false) || GET_PATTERN_TO_EMPHASIZE_MATCH_SUPER_METHOD == null) {
            return null;
        } else {
            try {
                return (String) GET_PATTERN_TO_EMPHASIZE_MATCH_SUPER_METHOD.invokeExact(this, document, offset);
            } catch (Throwable e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static MethodHandle GET_PATTERN_TO_EMPHASIZE_MATCH_SUPER_METHOD = MethodHandleUtils.getSuperMethodHandle(
            MethodHandles.lookup(), "getPatternToEmphasizeMatch", String.class, IDocument.class, int.class).orNull();
}

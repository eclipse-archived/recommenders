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
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.ui.text.java.MethodDeclarationCompletionProposal;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class ProcessableMethodDeclarationCompletionProposal extends MethodDeclarationCompletionProposal implements
        IProcessableProposal {

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
                return new ProcessableMethodDeclarationCompletionProposal(proposal, type, constructorName, null,
                        offset, length, relevance + 500);
            }
        }

        if (prefix.length() > 0 && !"main".equals(prefix) && !hasMethod(methods, prefix)) {
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

    private Map<String, Object> tags = Maps.newHashMap();
    private CompletionProposal coreProposal;
    private ProposalProcessorManager mgr;
    private String lastPrefix;

    public ProcessableMethodDeclarationCompletionProposal(CompletionProposal proposal, IType type, String methodName,
            String returnTypeSig, int start, int length, int relevance) {
        super(type, methodName, returnTypeSig, start, length, relevance);
        coreProposal = proposal;
    }

    // ===========

    @Override
    public boolean isPrefix(final String prefix, final String completion) {
        lastPrefix = prefix;
        if (mgr.prefixChanged(prefix)) {
            return true;
        }
        return super.isPrefix(prefix, completion);
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
    public void setTag(String key, Object value) {
        ensureIsNotNull(key);
        if (value == null) {
            tags.remove(key);
        } else {
            tags.put(key, value);
        }
    }

    @Override
    public <T> Optional<T> getTag(String key) {
        return Optional.fromNullable((T) tags.get(key));
    }

    @Override
    public <T> T getTag(String key, T defaultValue) {
        T res = (T) tags.get(key);
        return res != null ? res : defaultValue;
    }

}

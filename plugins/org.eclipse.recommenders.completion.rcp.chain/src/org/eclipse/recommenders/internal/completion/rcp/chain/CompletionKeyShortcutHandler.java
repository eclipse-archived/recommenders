/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.chain;

import static org.eclipse.recommenders.utils.Throws.throwIllegalStateException;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.SpecificContentAssistExecutor;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalCategory;
import org.eclipse.jdt.internal.ui.text.java.CompletionProposalComputerRegistry;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;

/**
 * Chain completion can be triggered by a key shortcut. This handler receives this shortcut and triggers completion in
 * the active editor.
 */
@SuppressWarnings("restriction")
public class CompletionKeyShortcutHandler extends AbstractHandler {
    private static final String CHAIN_COMPLETION_CATEGORY = "org.eclipse.recommenders.completion.rcp.chain.category";
    private final CompletionProposalComputerRegistry registry = CompletionProposalComputerRegistry.getDefault();
    private final SpecificContentAssistExecutor executor = new SpecificContentAssistExecutor(registry);

    public CompletionKeyShortcutHandler() {
        validateChainCompletionCategoryExists();
    }

    private void validateChainCompletionCategoryExists() {

        List<CompletionProposalCategory> categories = registry.getProposalCategories();
        for (CompletionProposalCategory c : categories) {
            if (c.getId().equals(CHAIN_COMPLETION_CATEGORY)) {
                return;
            }
        }
        throwIllegalStateException("Chain proposal engine category '%s' no found. Report this as bug.",
                CHAIN_COMPLETION_CATEGORY);

    }

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Optional<JavaEditor> opt = JdtUtils.getActiveJavaEditor();
        if (!opt.isPresent()) {
            return null;
        }
        executor.invokeContentAssist(opt.get(), CHAIN_COMPLETION_CATEGORY);
        return null;
    }
}

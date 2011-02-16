/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.types;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.recommenders.commons.utils.annotations.Provisional;

/**
 * Temporary class to compute the shift amount when a new import is
 * automatically written to the document.
 */
@SuppressWarnings("restriction")
@Provisional
public final class ModifiedJavaContext extends JavaContext {

    private boolean shifted;

    public ModifiedJavaContext(final TemplateContextType type, final IDocument document, final int completionOffset,
            final int completionLength, final ICompilationUnit compilationUnit) {
        super(type, document, completionOffset, completionLength, compilationUnit);
    }

    @Override
    public int getStart() {
        if (!shifted) {
            shiftCompletionOffset();
        }
        return getCompletionOffset();
    }

    /**
     * Inspects how many characters where inserted by the automatically included
     * import and modifies the context's completion offset.
     */
    private void shiftCompletionOffset() {
        final IDocument document = JavaTemplateProposal.getCurrentDocument();
        if (document != null) {
            final int shift = document.getLength() - getDocument().getLength();
            if (shift > 0) {
                setCompletionOffset(getCompletionOffset() + shift);
                shifted = true;
            }
        }
    }
}

/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Dorn - initial API and implementation.
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.completion;

import static org.eclipse.recommenders.internal.snipmatch.rcp.Constants.SNIPMATCH_CONTEXT_ID;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public class TextTemplateContextType {

    private static class Holder {
        static final TemplateContextType INSTANCE = createContextType();
    }

    public static TemplateContextType getInstance() {
        return Holder.INSTANCE;
    }

    private static TemplateContextType createContextType() {
        TemplateContextType contextType = new TemplateContextType();
        contextType.setId(SNIPMATCH_CONTEXT_ID);

        // global
        contextType.addResolver(new GlobalTemplateVariables.Cursor());
        contextType.addResolver(new GlobalTemplateVariables.WordSelection());
        contextType.addResolver(new GlobalTemplateVariables.LineSelection());
        contextType.addResolver(new GlobalTemplateVariables.Dollar());
        contextType.addResolver(new GlobalTemplateVariables.Date());
        contextType.addResolver(new GlobalTemplateVariables.Year());
        contextType.addResolver(new GlobalTemplateVariables.Time());
        contextType.addResolver(new GlobalTemplateVariables.User());

        return contextType;
    }
}

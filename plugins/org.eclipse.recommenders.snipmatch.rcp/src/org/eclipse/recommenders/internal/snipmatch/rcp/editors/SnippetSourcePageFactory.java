/**
 * Copyright (c) 2010, 2014 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Sewe - initial API and implementation
 */
package org.eclipse.recommenders.internal.snipmatch.rcp.editors;

import org.eclipse.recommenders.snipmatch.rcp.ISnippetEditorPageFactory;
import org.eclipse.recommenders.snipmatch.rcp.SnippetEditor;
import org.eclipse.ui.forms.editor.IFormPage;

public class SnippetSourcePageFactory implements ISnippetEditorPageFactory {

    @Override
    public IFormPage createPage(SnippetEditor editor, String id, String name) {
        return new SnippetSourcePage(editor, id, name);
    }
}

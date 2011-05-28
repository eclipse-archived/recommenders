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

import com.google.inject.Inject;

import org.eclipse.recommenders.commons.selection.ExtendedSelectionContext;
import org.eclipse.recommenders.commons.selection.IExtendedSelectionListener;
import org.eclipse.recommenders.internal.rcp.extdoc.views.ExtDocView;

final class UIManager implements IExtendedSelectionListener {

    private final ExtDocView extDocView;

    @Inject
    public UIManager(final ExtDocView extDocView) {
        this.extDocView = extDocView;
    }

    @Override
    public void update(final ExtendedSelectionContext context) {
        extDocView.update(context);
    }

}

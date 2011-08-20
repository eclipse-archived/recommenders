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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.swt;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public final class TableListing extends Composite {

    public TableListing(final Composite parent, final int columns) {
        super(parent, SWT.NONE);
        setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).spacing(12, 0).margins(12, 0).create());
        setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    }

    /**
     * Begins a new line starting with a black block symbol.
     */
    public void startNewRow() {
        SwtFactory.createLabel(this, "\u25AA", true, false, SWT.COLOR_BLACK, false);
    }

    public void addCell(final String text, final boolean bold, final boolean code, final int color) {
        SwtFactory.createLabel(this, text, bold, code, color, false);
    }

}

/**
 * Copyright (c) 2011 Andreas Frankenberger.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
package org.eclipse.recommenders.internal.udc.ui.depersonalisation;

import java.io.File;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class FileFilter extends ViewerFilter {
    public FileFilter(final String filterText) {
        super();
        this.filterText = filterText;
    }

    final String filterText;

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        final File file = (File) element;
        return file.getName().contains(filterText);
    }

}

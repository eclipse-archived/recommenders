/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch- initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import static org.eclipse.recommenders.utils.Checks.cast;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.utils.Pair;

public class PackageFragmentRootLabelProvider extends ColumnLabelProvider {
    @Override
    public String getText(final Object element) {
        Pair<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> e = cast(element);
        return e.getSecond().getLocation().getName();
    }

    @Override
    public String getToolTipText(final Object element) {
        Pair<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> e = cast(element);
        return e.getSecond().getLocation().getAbsolutePath();
    }
}

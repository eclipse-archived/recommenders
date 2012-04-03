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
package org.eclipse.recommenders.internal.completion.rcp.calls.preferences;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.eclipse.recommenders.utils.Checks.cast;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.recommenders.internal.rcp.models.ModelArchiveMetadata;
import org.eclipse.recommenders.rcp.ClasspathEntryInfo;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.swt.graphics.Image;

public class VersionLabelProvider extends ColumnLabelProvider {
    public static final String NLS_UNKNOWN = "Some details are unknown for this dependency";
    public static final String NLS_KNOWN = "Name and version of dependency is known";
    private final Image versionUnknownImage;
    private final Image versionImage;

    public VersionLabelProvider(Image versionUnknownImage, Image versionImage) {
        this.versionUnknownImage = versionUnknownImage;
        this.versionImage = versionImage;
    }

    @Override
    public Image getImage(final Object element) {
        return hasDependencyInformation(element) ? versionImage : versionUnknownImage;
    }

    @Override
    public String getToolTipText(final Object element) {
        return hasDependencyInformation(element) ? NLS_KNOWN : NLS_UNKNOWN;
    }

    private boolean hasDependencyInformation(final Object element) {
        Tuple<ClasspathEntryInfo, ModelArchiveMetadata<?, ?>> e = cast(element);
        ClasspathEntryInfo cpei = e.getFirst();
        if (isEmpty(cpei.getSymbolicName()) || cpei.getVersion().isUnknown()) {
            return false;
        }
        return true;
    }
}
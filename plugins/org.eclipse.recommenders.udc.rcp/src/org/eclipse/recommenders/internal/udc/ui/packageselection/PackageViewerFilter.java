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
package org.eclipse.recommenders.internal.udc.ui.packageselection;

/**
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Frankenberger - initial API and implementation.
 */
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class PackageViewerFilter extends ViewerFilter {
    Pattern[] includePattern, excludePattern;

    public PackageViewerFilter(final String[] includes, final String[] excludes) {
        includePattern = createPatterns(includes);
        excludePattern = createPatterns(excludes);
    }

    private Pattern[] createPatterns(final String[] expressions) {
        final Pattern[] pattern = new Pattern[expressions.length];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Pattern.compile(expressions[i]);
        }
        return pattern;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        final Package packageElement = (Package) element;
        final String identifier = packageElement.getPackageIdentifier();
        if (matches(identifier, excludePattern)) {
            return false;
        }
        if (matches(identifier, includePattern)) {
            return true;
        }
        for (final Package p : getContentProvider(viewer).getChildren(element)) {
            if (select(viewer, element, p)) {
                return true;
            }
        }
        return false;
    }

    private PackageTreeContentProvider getContentProvider(final Viewer viewer) {
        return (PackageTreeContentProvider) ((TreeViewer) viewer).getContentProvider();
    }

    private boolean matches(final String text, final Pattern[] patterns) {
        for (final Pattern pattern : patterns) {
            if (pattern.matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

}

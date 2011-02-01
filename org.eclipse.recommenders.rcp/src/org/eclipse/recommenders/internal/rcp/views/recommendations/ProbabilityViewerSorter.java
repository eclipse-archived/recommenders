/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.views.recommendations;

import static org.eclipse.recommenders.commons.utils.Checks.cast;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.ICodeElement;
import org.eclipse.recommenders.rcp.IRecommendation;

public class ProbabilityViewerSorter extends ViewerSorter {
    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2) {
        if (e1 instanceof ICodeElement) {
            if (e2 instanceof ICodeElement) {
                // sort by name:
                return super.compare(viewer, e1, e2);
            } else {
                // code elements come first:
                return -1;
            }
        } else {
            // e1 *should* be a IRecommendation
            if (e2 instanceof ICodeElement) {
                // sort code elements before recommendations
                return +1;
            } else {
                final IRecommendation r1 = cast(e1);
                final IRecommendation r2 = cast(e2);
                return Double.compare(r2.getProbability(), r1.getProbability());
            }
        }
    }
}

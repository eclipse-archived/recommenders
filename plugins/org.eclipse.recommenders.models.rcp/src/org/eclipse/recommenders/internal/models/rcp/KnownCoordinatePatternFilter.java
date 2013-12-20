/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Olav Lenz - initial API and implementation.
 */
package org.eclipse.recommenders.internal.models.rcp;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.recommenders.internal.models.rcp.ModelRepositoriesView.KnownCoordinate;
import org.eclipse.ui.dialogs.PatternFilter;

public class KnownCoordinatePatternFilter extends PatternFilter {

    @Override
    protected boolean isLeafMatch(final Viewer viewer, final Object element) {
        if (element instanceof KnownCoordinate) {
            final KnownCoordinate coor = (KnownCoordinate) element;
            return wordMatches(coor.pc.toString());
        }
        if (element instanceof String) {
            return true;
        }
        return super.isLeafMatch(viewer, element);
    }
}

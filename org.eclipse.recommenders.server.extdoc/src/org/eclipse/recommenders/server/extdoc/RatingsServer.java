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
package org.eclipse.recommenders.server.extdoc;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.recommenders.rcp.extdoc.features.IStarsRatingsServer;

public final class RatingsServer implements IStarsRatingsServer {

    @Override
    public int getAverageRating(final IJavaElement javaElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUserRating(final IJavaElement javaElement) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addRating(final IJavaElement javaElement, final int stars) {
        // TODO Auto-generated method stub

    }

}

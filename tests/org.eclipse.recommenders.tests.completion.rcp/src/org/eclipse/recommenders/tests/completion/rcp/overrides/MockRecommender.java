/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp.overrides;

import static org.mockito.Mockito.mock;

import org.eclipse.recommenders.internal.completion.rcp.overrides.model.InstantOverridesRecommender;

public class MockRecommender {

    public static InstantOverridesRecommender get() {
        final InstantOverridesRecommender mock = mock(InstantOverridesRecommender.class);
        // when(mock.createRecommendations(type));
        return mock;
    }

}

/**
 * Copyright (c) 2011 Doug Wightman, Zi Ye
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.recommenders.snipmatch.web;

import org.eclipse.recommenders.snipmatch.core.Effect;

public interface ILoadProfileListener {

    void effectLoaded(Effect effect);

    void loadProfileSucceeded();

    void loadProfileFailed(String error);
}

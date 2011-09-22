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
package org.eclipse.recommenders.internal.udc.depersonalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DelegatingDepersonalizerProvider implements IDepersonalisationProvider {
    IDepersonalisationProvider[] providers;

    public DelegatingDepersonalizerProvider(final IDepersonalisationProvider... providers) {
        this.providers = providers;
    }

    @Override
    public ICompilationUnitDepersonalizer[] getDepersonalizers() {
        final List<ICompilationUnitDepersonalizer> depersonalizers = new ArrayList<ICompilationUnitDepersonalizer>();
        for (final IDepersonalisationProvider provider : providers) {
            depersonalizers.addAll(Arrays.asList(provider.getDepersonalizers()));
        }
        return depersonalizers.toArray(new ICompilationUnitDepersonalizer[depersonalizers.size()]);
    }

}

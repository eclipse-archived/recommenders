/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.overrides.rcp;

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.recommenders.models.IBasedName;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.overrides.IOverrideModel;
import org.eclipse.recommenders.overrides.IOverrideModelProvider;
import org.eclipse.recommenders.overrides.PoolingOverrideModelProvider;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

public class RcpOverrideModelProvider extends AbstractIdleService implements IOverrideModelProvider {

    private PoolingOverrideModelProvider delegate;

    @Override
    public Optional<IOverrideModel> acquireModel(IBasedName<ITypeName> key) {
        return delegate.acquireModel(key);
    }

    @Override
    public void releaseModel(IOverrideModel model) {
        delegate.releaseModel(model);
    }

    @Override
    public void open() throws IOException {
        delegate.open();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Inject
    public RcpOverrideModelProvider(IModelRepository repository) {
        delegate = new PoolingOverrideModelProvider(repository);
    }

    @Override
    protected void shutDown() throws Exception {
        close();
    }

    @Override
    protected void startUp() throws Exception {
        open();
    }
}

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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryClosedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryOpenedEvent;
import org.eclipse.recommenders.overrides.IOverrideModel;
import org.eclipse.recommenders.overrides.IOverrideModelProvider;
import org.eclipse.recommenders.overrides.PoolingOverrideModelProvider;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;

public class RcpOverrideModelProvider implements IOverrideModelProvider, IRcpService {

    @Inject
    IModelRepository repository;
    @Inject
    IModelArchiveCoordinateAdvisor index;

    PoolingOverrideModelProvider delegate;

    @Override
    @PostConstruct
    public void open() throws IOException {
        delegate = new PoolingOverrideModelProvider(repository, index);
        delegate.open();
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public Optional<IOverrideModel> acquireModel(IUniqueName<ITypeName> key) {
        return delegate.acquireModel(key);
    }

    @Override
    public void releaseModel(IOverrideModel model) {
        delegate.releaseModel(model);
    }

    @Subscribe
    public void onEvent(ModelRepositoryOpenedEvent e) throws IOException {
        open();
    }

    @Subscribe
    public void onEvent(ModelRepositoryClosedEvent e) throws IOException {
        close();
    }
}

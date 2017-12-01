/**
 * Copyright (c) 2017 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.statics.rcp;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryClosedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryOpenedEvent;
import org.eclipse.recommenders.rcp.IRcpService;
import org.eclipse.recommenders.statics.IStaticsModel;
import org.eclipse.recommenders.statics.IStaticsModelProvider;
import org.eclipse.recommenders.statics.PoolingStaticsModelProvider;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;

public class RcpStaticsModelProvider implements IStaticsModelProvider, IRcpService {

    private final IModelRepository repository;
    private final IModelArchiveCoordinateAdvisor index;
    private final Map<String, IInputStreamTransformer> transformers;

    private IStaticsModelProvider delegate;

    @Inject
    public RcpStaticsModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        this.repository = repository;
        this.index = index;
        this.transformers = transformers;
    }

    @Override
    @PostConstruct
    public void open() throws IOException {
        delegate = new PoolingStaticsModelProvider(repository, index, transformers);
        delegate.open();
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public Optional<IStaticsModel> acquireModel(UniqueTypeName key) {
        return delegate.acquireModel(key);
    }

    @Override
    public void releaseModel(IStaticsModel value) {
        delegate.releaseModel(value);
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

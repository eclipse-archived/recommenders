/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Sewe - initial API and implementation.
 */
package org.eclipse.recommenders.internal.constructors.rcp;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.recommenders.constructors.ConstructorModel;
import org.eclipse.recommenders.constructors.ConstructorModelProvider;
import org.eclipse.recommenders.constructors.IConstructorModelProvider;
import org.eclipse.recommenders.models.IInputStreamTransformer;
import org.eclipse.recommenders.models.IModelArchiveCoordinateAdvisor;
import org.eclipse.recommenders.models.IModelRepository;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryClosedEvent;
import org.eclipse.recommenders.models.rcp.ModelEvents.ModelRepositoryOpenedEvent;
import org.eclipse.recommenders.rcp.IRcpService;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;

public class EclipseConstructorModelProvider implements IConstructorModelProvider, IRcpService {

    private final IModelRepository repository;
    private final IModelArchiveCoordinateAdvisor index;
    private final Map<String, IInputStreamTransformer> transformers;

    private IConstructorModelProvider delegate;

    @Inject
    public EclipseConstructorModelProvider(IModelRepository repository, IModelArchiveCoordinateAdvisor index,
            Map<String, IInputStreamTransformer> transformers) {
        this.repository = repository;
        this.index = index;
        this.transformers = transformers;
    }

    @Override
    @PostConstruct
    public void open() throws IOException {
        delegate = new ConstructorModelProvider(repository, index, transformers);
        delegate.open();
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public Optional<ConstructorModel> acquireModel(UniqueTypeName key) {
        return delegate.acquireModel(key);
    }

    @Override
    public void releaseModel(ConstructorModel value) {
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

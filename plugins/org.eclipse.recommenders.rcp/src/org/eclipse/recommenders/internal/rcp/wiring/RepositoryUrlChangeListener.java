/**
 * Copyright (c) 2010, 2012 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.wiring;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.rcp.repo.IModelRepository;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class RepositoryUrlChangeListener implements IPropertyChangeListener {

    private final IModelRepository repository;

    @Inject
    public RepositoryUrlChangeListener(IModelRepository repository) {
        this.repository = repository;
        hookRegister();
    }

    @VisibleForTesting
    protected void hookRegister() {
        RecommendersPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(RecommendersPlugin.P_REPOSITORY_URL)) {
            String newUrl = event.getNewValue().toString();
            repository.setRemote(newUrl);
        }
    }
}

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
package org.eclipse.recommenders.rcp.repo;

import javax.inject.Inject;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.recommenders.internal.rcp.repo.UpdateModelIndexJob;
import org.eclipse.recommenders.rcp.RecommendersPlugin;

public class ModelRepositoryService implements IPropertyChangeListener {

    private static ModelRepositoryService INSTANCE;
    private final IModelRepositoryIndex index;
    private final IModelRepository repository;

    @Inject
    public ModelRepositoryService(IModelRepository repository, IModelRepositoryIndex index) {
        INSTANCE = this;
        this.repository = repository;
        this.index = index;
        registerPreferenceListener();
        scheduleUpdate();
    }

    private void registerPreferenceListener() {

        IPreferenceStore store = RecommendersPlugin.getDefault().getPreferenceStore();
        store.addPropertyChangeListener(this);

    }

    private void scheduleUpdate() {
        new UpdateModelIndexJob(index, repository).schedule();
    }

    public static IModelRepository getRepository() {
        return INSTANCE.repository;
    }

    public static IModelRepositoryIndex getIndex() {
        return INSTANCE.index;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().contentEquals(RecommendersPlugin.P_REPOSITORY_URL)) {
            Object v = event.getNewValue();
            if (v instanceof String) {
                repository.setRemote((String) v);
            }
        }
    }
}

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
package org.eclipse.recommenders.examples.models;

import static org.eclipse.recommenders.models.ProjectCoordinate.UNKNOWN;

import org.eclipse.recommenders.examples.models.CompletionEngineExample.IJavaElement;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;

public class UsingModelProvider {

    EclipseProjectCoordinateProvider provider;

    RecommendationModel DUMMY = new RecommendationModel();
    IModelProvider<IUniqueName<ITypeName>, RecommendationModel> service;

    void getModelForIDEType(IJavaElement type) {
        ProjectCoordinate pc = provider.map(type).or(UNKNOWN);
        UniqueTypeName name = new UniqueTypeName(pc, toTypeName(type));
        RecommendationModel model = service.acquireModel(name).or(DUMMY);
        model.compute();
        // ...
        service.releaseModel(model);
    }

    //
    // only fake implementations below this point

    private ITypeName toTypeName(IJavaElement type) {
        // fake! replace by real resolution logic
        return VmTypeName.JavaLangString;
    }

    static class RecommendationModel {

        public void compute() {
        }
    }
}

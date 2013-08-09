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

import java.io.File;

import org.eclipse.recommenders.examples.models.UsingModelProvider.RecommendationModel;
import org.eclipse.recommenders.models.IUniqueName;
import org.eclipse.recommenders.models.IModelProvider;
import org.eclipse.recommenders.models.ProjectCoordinate;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;

public class CompletionEngineExample {

    void resolveGavFromPackgeFragmentRoot(IPackageFragementRoot jdtElement, EclipseProjectCoordinateProvider r) {
        if (jdtElement.isjar()) {
            // ignore what type jdtElement is exactly!
        } else if (jdtElement.isSourceFolder()) {
            // src folders are mapped by the mapping service internally.
        }
        Optional<ProjectCoordinate> gav = r.map(jdtElement);
    }

    void resolveGavFromIJavaElement(IJavaElement jdtElement, EclipseProjectCoordinateProvider r) {
        // same for jar, src folder, package etc.:
        Optional<ProjectCoordinate> gav = r.map(jdtElement);
    }

    void resolveGavFromSourceFolder(IPackageFragementRoot srcFolder, EclipseProjectCoordinateProvider r) {
    }

    private static final class CompletionEngine {
        IModelProvider<IUniqueName<ITypeName>, RecommendationModel> modelProvider;
        EclipseProjectCoordinateProvider coordService;

        void computeProposals(IJavaElement e) {
            ProjectCoordinate pc = coordService.map(e).orNull();
            ITypeName type = e.getITypeName(); // convert somehow to ITypeName
            IUniqueName<ITypeName> name = createQualifiedName(pc, type);
            RecommendationModel net = modelProvider.acquireModel(name).orNull();
            // ... do work
            modelProvider.releaseModel(net);

        }

        private IUniqueName<ITypeName> createQualifiedName(ProjectCoordinate pc, ITypeName name) {
            return null;
        }
    }

    interface IJavaElement {

        ITypeName getITypeName();
    }

    interface IPackageFragementRoot extends IJavaElement {

        // it's slightly more complicated but...
        File getFile();

        boolean isjar();

        boolean isSourceFolder();
    }

    interface IJavaProject {
    }

}

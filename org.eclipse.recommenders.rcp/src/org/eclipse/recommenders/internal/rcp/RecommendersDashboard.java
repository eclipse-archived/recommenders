/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp;

import java.util.Set;

import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.IArtifactStoreChangedListener;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.IEditorChangedListener;

import com.google.inject.Inject;

/**
 * Don't know whether this is actually needed. While experimenting with Guice in
 * RCP I had several injction exceptions due to bidirectional initializations.
 * Need to check from time to time whether this still holds.
 * 
 * Last check: 02.01.11
 */
@SuppressWarnings({ "unused", "rawtypes" })
public class RecommendersDashboard {
    private final Set<IArtifactStoreChangedListener> storeChangedListener;

    private final Set<IEditorChangedListener> editorChangeListener;

    private final Set<ICompilationUnitAnalyzer> analyzers;

    private final IArtifactStore artifactStore;

    private final EditorTrackingService editorTrackingService;

    private final IAstProvider astProvider;

    @Inject
    public RecommendersDashboard(final IArtifactStore artifactStore, final Set<ICompilationUnitAnalyzer> analyzers,
            final Set<IEditorChangedListener> editorChangeListener,
            final Set<IArtifactStoreChangedListener> storeChangedListener,
            final EditorTrackingService editorTrackingService, final IAstProvider astProvider) {
        this.artifactStore = artifactStore;
        this.analyzers = analyzers;
        this.editorChangeListener = editorChangeListener;
        this.storeChangedListener = storeChangedListener;
        this.editorTrackingService = editorTrackingService;
        this.astProvider = astProvider;
    }
}

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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.recommenders.internal.rcp.views.cu.CompilationUnitViewPublisher;
import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.eclipse.recommenders.internal.rcp.views.recommendations.RecommendationsViewPublisher;
import org.eclipse.recommenders.rcp.IArtifactStore;
import org.eclipse.recommenders.rcp.IArtifactStoreChangedListener;
import org.eclipse.recommenders.rcp.IAstProvider;
import org.eclipse.recommenders.rcp.ICompilationUnitAnalyzer;
import org.eclipse.recommenders.rcp.IEditorChangedListener;
import org.eclipse.recommenders.rcp.utils.JavaElementResolver;
import org.eclipse.recommenders.rcp.utils.ast.ASTNodeUtils;
import org.eclipse.recommenders.rcp.utils.ast.ASTStringUtils;
import org.eclipse.recommenders.rcp.utils.ast.BindingUtils;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

@SuppressWarnings({ "rawtypes", "unused" })
public class RecommendersModule extends AbstractModule implements Module {

    private Multibinder<IEditorChangedListener> editorChangedListenerBinder;

    private Multibinder<IArtifactStoreChangedListener> artifactStoreChangedListenerBinder;

    private Multibinder<ICompilationUnitAnalyzer> compilationUnitAnalyzerBinder;

    @Override
    protected void configure() {
        configureArtifactStore();
        configureEditorTracker();
        configureCompilationUnitViewPublisher();
        configureRecommendationsViewPublisher();
        configureJavaElementResolver();
        configureAstProvider();
        configureBuilder();
    }

    @Singleton
    @Provides
    public EventBus provideWorkspaceEventBus() {
        final EventBus bus = new EventBus("Code Recommenders Workspace Event Bus");
        return bus;
    }

    private void configureAstProvider() {
        final CachingAstProvider p = new CachingAstProvider();
        JavaCore.addElementChangedListener(p);
        bind(IAstProvider.class).toInstance(p);
    }

    private void configureArtifactStore() {
        bind(JsonArtifactStore.class).in(Scopes.SINGLETON);
        bind(IArtifactStore.class).to(JsonArtifactStore.class).in(Scopes.SINGLETON);
        compilationUnitAnalyzerBinder = Multibinder.newSetBinder(binder(), ICompilationUnitAnalyzer.class);
        artifactStoreChangedListenerBinder = Multibinder.newSetBinder(binder(), IArtifactStoreChangedListener.class);
    }

    private void configureEditorTracker() {
        bind(EditorTrackingService.class).in(Scopes.SINGLETON);
        editorChangedListenerBinder = Multibinder.newSetBinder(binder(), IEditorChangedListener.class);
    }

    private void configureCompilationUnitViewPublisher() {
        bind(CompilationUnitViewPublisher.class).in(Scopes.SINGLETON);
        editorChangedListenerBinder.addBinding().to(CompilationUnitViewPublisher.class);
        artifactStoreChangedListenerBinder.addBinding().to(CompilationUnitViewPublisher.class);
    }

    private void configureRecommendationsViewPublisher() {
        bind(RecommendationsViewPublisher.class).in(Scopes.SINGLETON);
        editorChangedListenerBinder.addBinding().to(RecommendationsViewPublisher.class);
        artifactStoreChangedListenerBinder.addBinding().to(RecommendationsViewPublisher.class);
        Multibinder.newSetBinder(binder(), IRecommendationsViewContentProvider.class);
    }

    private void configureJavaElementResolver() {
        bind(JavaElementResolver.class).in(Scopes.SINGLETON);
        requestStaticInjection(ASTStringUtils.class);
        requestStaticInjection(ASTNodeUtils.class);
        requestStaticInjection(BindingUtils.class);
    }

    private void configureBuilder() {
        // bind(RecommendersBuilder.class);

    }

    @Provides
    public List<IEditorChangedListener> providePrioritySortedEditorChangedListeners(
            final Set<IEditorChangedListener> unsortedListeners) {
        final List<IEditorChangedListener> sorted = Lists.newArrayList(unsortedListeners);
        Collections.sort(sorted, new Comparator<IEditorChangedListener>() {

            @Override
            public int compare(final IEditorChangedListener o1, final IEditorChangedListener o2) {
                return o1.getNotifcationPriority().compareTo(o2.getNotifcationPriority());
            }
        });
        return sorted;
    }

    @Provides
    public List<IArtifactStoreChangedListener> providePrioritySortedArtifactStoreChangedListener(
            final Set<IArtifactStoreChangedListener> unsortedListeners) {
        final List<IArtifactStoreChangedListener> sorted = Lists.newArrayList(unsortedListeners);
        Collections.sort(sorted, new Comparator<IArtifactStoreChangedListener>() {

            @Override
            public int compare(final IArtifactStoreChangedListener o1, final IArtifactStoreChangedListener o2) {
                return o1.getNotifcationPriority().compareTo(o2.getNotifcationPriority());
            }
        });
        return sorted;
    }
}

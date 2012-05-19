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
package org.eclipse.recommenders.internal.completion.rcp.overrides;

import static java.lang.String.format;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.completion.rcp.overrides.ManualModelStoreWiring.OverridesModelArchiveStore;
import org.eclipse.recommenders.internal.rcp.models.IModelArchiveStore;
import org.osgi.framework.FrameworkUtil;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;

public class OverridesModule extends AbstractModule {

    public static final String MODEL_VERSION = "0.1";

    public static TypeLiteral<IModelArchiveStore<IType, ClassOverridesNetwork>> STORE = new TypeLiteral<IModelArchiveStore<IType, ClassOverridesNetwork>>() {
    };

    @Override
    protected void configure() {
        bindCompletionEngine();
    }

    private void bindCompletionEngine() {
        bind(OverridesCompletionProposalComputer.class).in(Scopes.SINGLETON);
        final IPath stateLocation = Platform.getStateLocation(FrameworkUtil.getBundle(getClass()));
        final File index = new File(stateLocation.toFile(), format("ovrm-models-%s.json", MODEL_VERSION));
        bind(File.class).annotatedWith(OverridesModelStore.class).toInstance(index);
        bind(STORE).to(OverridesModelArchiveStore.class).in(Scopes.SINGLETON);
    }

    @BindingAnnotation
    @Target({ PARAMETER, METHOD })
    @Retention(RUNTIME)
    static @interface OverridesModelStore {
    }
}

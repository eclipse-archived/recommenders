/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.mining.extdocs;

import com.google.inject.AbstractModule;

public abstract class AbstractExtdocGuiceModule extends AbstractModule {

    protected final AlgorithmParameters arguments;

    public AbstractExtdocGuiceModule(final AlgorithmParameters arguments) {
        this.arguments = arguments;
    }

    @Override
    protected void configure() {
        bind(AlgorithmParameters.class).toInstance(arguments);
        bind(ClassOverrideDirectivesGenerator.class).toInstance(new ClassOverrideDirectivesGenerator(0.05));
        // bind(OverridesClusterer.class).toInstance(new OverridesClusterer(5));
        bind(MethodSelfcallDirectivesGenerator.class).toInstance(new MethodSelfcallDirectivesGenerator(0.05));
    }

}

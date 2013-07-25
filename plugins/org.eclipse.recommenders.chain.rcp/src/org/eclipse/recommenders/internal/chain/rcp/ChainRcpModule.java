/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henß - initial API and implementation.
 */
package org.eclipse.recommenders.internal.chain.rcp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.jface.preference.IPreferenceStore;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;

public final class ChainRcpModule extends AbstractModule {

    @Override
    protected void configure() {
        final IPreferenceStore prefStore = ChainRcpPlugin.getDefault().getPreferenceStore();
        bind(IPreferenceStore.class).annotatedWith(ChainCompletion.class).toInstance(prefStore);
    }

    @BindingAnnotation
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ChainCompletion {
    }

}

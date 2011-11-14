/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.tests.completion.rcp.templates;

import org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionModule;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;

public final class TemplatesCompletionModuleTest {

    @Test
    public void testTemplatesCompletionModule() {
        final TemplatesCompletionModule module = new TemplatesCompletionModule();
        final Binder binder = Mockito.mock(Binder.class);
        final AnnotatedBindingBuilder builder = Mockito.mock(AnnotatedBindingBuilder.class);
        Mockito.when(binder.bind((Class) Mockito.any())).thenReturn(builder);
        module.configure(binder);
    }
}

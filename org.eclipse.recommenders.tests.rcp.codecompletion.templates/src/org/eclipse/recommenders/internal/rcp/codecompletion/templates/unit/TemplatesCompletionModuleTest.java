package org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit;

import com.google.inject.Binder;
import com.google.inject.binder.AnnotatedBindingBuilder;

import org.eclipse.recommenders.internal.rcp.codecompletion.templates.TemplatesCompletionModule;
import org.junit.Test;
import org.mockito.Mockito;

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

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
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.recommenders.completion.rcp.CompletionContextKey.*;

import javax.inject.Singleton;

import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.CompletionOnTypeContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.CompletionPrefixContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingAstMethodContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingElementContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingMethodContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingMethodFirstDeclarationContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingTypeContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ExpectedTypeContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ExpectedTypeNamesContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.InternalCompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.JavaContentAssistInvocationContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.LookupEnvironmentContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ReceiverNameContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ReceiverTypeBindingContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.VisibleFieldsContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.VisibleLocalsContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.VisibleMethodsContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;

public class CompletionRcpModule extends AbstractModule {

    @Override
    protected void configure() {
        MapBinder<CompletionContextKey, ICompletionContextFunction> functions = MapBinder.newMapBinder(binder(),
                CompletionContextKey.class, ICompletionContextFunction.class);

        functions.addBinding(COMPLETION_PREFIX).to(CompletionPrefixContextFunction.class);
        functions.addBinding(ENCLOSING_AST_METHOD).to(EnclosingAstMethodContextFunction.class);
        functions.addBinding(ENCLOSING_ELEMENT).to(EnclosingElementContextFunction.class);
        functions.addBinding(ENCLOSING_TYPE).to(EnclosingTypeContextFunction.class);
        functions.addBinding(ENCLOSING_METHOD).to(EnclosingMethodContextFunction.class);
        functions.addBinding(ENCLOSING_METHOD_FIRST_DECLARATION).to(
                EnclosingMethodFirstDeclarationContextFunction.class);
        functions.addBinding(EXPECTED_TYPE).to(ExpectedTypeContextFunction.class);
        functions.addBinding(EXPECTED_TYPENAMES).to(ExpectedTypeNamesContextFunction.class);
        functions.addBinding(IS_COMPLETION_ON_TYPE).to(CompletionOnTypeContextFunction.class);
        functions.addBinding(INTERNAL_COMPLETIONCONTEXT).to(InternalCompletionContextFunction.class);
        functions.addBinding(JAVA_PROPOSALS).to(InternalCompletionContextFunction.class);
        functions.addBinding(JAVA_CONTENTASSIST_CONTEXT).to(JavaContentAssistInvocationContextFunction.class);
        functions.addBinding(LOOKUP_ENVIRONMENT).to(LookupEnvironmentContextFunction.class);
        functions.addBinding(RECEIVER_TYPEBINDING).to(ReceiverTypeBindingContextFunction.class);
        functions.addBinding(RECEIVER_NAME).to(ReceiverNameContextFunction.class);
        functions.addBinding(VISIBLE_METHODS).to(VisibleMethodsContextFunction.class);
        functions.addBinding(VISIBLE_FIELDS).to(VisibleFieldsContextFunction.class);
        functions.addBinding(VISIBLE_LOCALS).to(VisibleLocalsContextFunction.class);
    }

    @Provides
    @Singleton
    SessionProcessorDescriptor[] provideSessionProcessorDescriptors() {
        return SessionProcessorDescriptor.parseExtensions();
    }
}

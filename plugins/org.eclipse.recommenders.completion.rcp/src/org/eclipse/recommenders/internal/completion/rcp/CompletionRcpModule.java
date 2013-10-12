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

import static org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.*;

import javax.inject.Singleton;

import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.CompletionPrefixContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingElementContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingMethodContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.EnclosingTypeContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ExpectedTypeContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ExpectedTypeNamesContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.InternalCompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.JavaContentAssistInvocationContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ReceiverNameContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.ReceiverTypeBindingContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.VisibleFieldsContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.VisibleLocalsContextFunction;
import org.eclipse.recommenders.completion.rcp.CompletionContextFunctions.VisibleMethodsContextFunction;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;

public class CompletionRcpModule extends AbstractModule {

    @Override
    protected void configure() {
        MapBinder<String, ICompletionContextFunction> functions = MapBinder.newMapBinder(binder(), String.class,
                ICompletionContextFunction.class);
        functions.addBinding(CCTX_COMPLETION_PREFIX).to(CompletionPrefixContextFunction.class);
        functions.addBinding(CCTX_ENCLOSING_ELEMENT).to(EnclosingElementContextFunction.class);
        functions.addBinding(CCTX_ENCLOSING_TYPE).to(EnclosingTypeContextFunction.class);
        functions.addBinding(CCTX_ENCLOSING_METHOD).to(EnclosingMethodContextFunction.class);
        functions.addBinding(CCTX_EXPECTED_TYPE).to(ExpectedTypeContextFunction.class);
        functions.addBinding(CCTX_EXPECTED_TYPENAMES).to(ExpectedTypeNamesContextFunction.class);
        functions.addBinding(CCTX_INTERNAL_COMPLETION_CONTEXT).to(InternalCompletionContextFunction.class);
        functions.addBinding(CCTX_JAVA_PROPOSALS).to(InternalCompletionContextFunction.class);
        functions.addBinding(CCTX_JAVA_CONTENTASSIST_CONTEXT).to(JavaContentAssistInvocationContextFunction.class);
        functions.addBinding(CCTX_RECEIVER_TYPEBINDING).to(ReceiverTypeBindingContextFunction.class);
        functions.addBinding(CCTX_RECEIVER_NAME).to(ReceiverNameContextFunction.class);
        functions.addBinding(CCTX_VISIBLE_METHODS).to(VisibleMethodsContextFunction.class);
        functions.addBinding(CCTX_VISIBLE_FIELDS).to(VisibleFieldsContextFunction.class);
        functions.addBinding(CCTX_VISIBLE_LOCALS).to(VisibleLocalsContextFunction.class);
    }

    @Provides
    @Singleton
    SessionProcessorDescriptor[] provideSessionProcessorDescriptors() {
        return SessionProcessorDescriptor.parseExtensions();
    }

}

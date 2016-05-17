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
package org.eclipse.recommenders.internal.calls.rcp;

import static org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.*;

import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.CompletionContextKey;
import org.eclipse.recommenders.completion.rcp.ICompletionContextFunction;
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.ReceiverCallsCompletionContextFunction;
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionContextFunctions.ReceiverTypeContextFunction;
import org.eclipse.ui.IWorkbench;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.multibindings.MapBinder;

@SuppressWarnings({ "rawtypes" })
public class CallsRcpModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ICallModelProvider.class).to(RcpCallModelProvider.class).in(Scopes.SINGLETON);

        MapBinder<CompletionContextKey, ICompletionContextFunction> functions = MapBinder.newMapBinder(binder(),
                CompletionContextKey.class, ICompletionContextFunction.class);
        functions.addBinding(RECEIVER_CALLS).to(ReceiverCallsCompletionContextFunction.class);
        functions.addBinding(RECEIVER_DEF_BY).to(ReceiverCallsCompletionContextFunction.class);
        functions.addBinding(RECEIVER_DEF_TYPE).to(ReceiverCallsCompletionContextFunction.class);
        functions.addBinding(RECEIVER_TYPE2).to(ReceiverTypeContextFunction.class);
    }

    @Provides
    @Singleton
    public CallsRcpPreferences provide(IWorkbench wb) {
        IEclipseContext context = (IEclipseContext) wb.getService(IEclipseContext.class);
        return ContextInjectionFactory.make(CallsRcpPreferences.class, context);
    }
}
